package org.ezra.lendingservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.ezra.lendingservice.client.CustomerServiceClient;
import org.ezra.lendingservice.dto.*;
import org.ezra.lendingservice.entity.*;
import org.ezra.lendingservice.enums.FeeType;
import org.ezra.lendingservice.enums.InstallmentStatus;
import org.ezra.lendingservice.enums.LoanStatus;
import org.ezra.lendingservice.enums.TenureType;
import org.ezra.lendingservice.events.NotificationEventDto;
import org.ezra.lendingservice.events.NotificationEventProducer;
import org.ezra.lendingservice.exception.LoanProcessingException;
import org.ezra.lendingservice.exception.ResourceNotFoundException;
import org.ezra.lendingservice.exception.ValidationException;
import org.ezra.lendingservice.mapper.LoanMapper;
import org.ezra.lendingservice.mapper.RepaymentMapper;
import org.ezra.lendingservice.repository.InstallmentRepository;
import org.ezra.lendingservice.repository.LoanProductRepository;
import org.ezra.lendingservice.repository.LoanRepository;
import org.ezra.lendingservice.repository.RepaymentRepository;
import org.ezra.lendingservice.service.FeeService;
import org.ezra.lendingservice.service.LoanService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final LoanProductRepository loanProductRepository;
    private final InstallmentRepository installmentRepository;
    private final RepaymentRepository repaymentRepository;
    private final FeeService feeService;
    private final CustomerServiceClient customerServiceClient;
    private final LoanMapper loanMapper;
    private final RepaymentMapper repaymentMapper;
    private final NotificationEventProducer notificationEventProducer;


    @Override
    @Transactional
    public LoanResponseDto applyForLoan(LoanApplicationRequestDto applicationDto) {
        ResponseEntity<CustomerResponseDto> customerResponse = customerServiceClient.getCustomerById(applicationDto.getCustomerId());
        if (!customerResponse.getStatusCode().is2xxSuccessful() || customerResponse.getBody() == null) {
            throw new ResourceNotFoundException("Customer not found with ID: " + applicationDto.getCustomerId() + " or customer service unavailable.");
        }
        CustomerResponseDto customer = customerResponse.getBody();

        ResponseEntity<Boolean> eligibilityResponse = customerServiceClient.isCustomerEligible(applicationDto.getCustomerId(), applicationDto.getAmount());
        if (!eligibilityResponse.getStatusCode().is2xxSuccessful() || eligibilityResponse.getBody() == null || !eligibilityResponse.getBody()) {
            throw new LoanProcessingException("Customer is not eligible for the requested loan amount or eligibility check failed.");
        }
        LoanProduct product = loanProductRepository.findById(applicationDto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("LoanProduct not found with ID: " + applicationDto.getProductId()));

        if (applicationDto.getAmount().compareTo(product.getMinAmount()) < 0 ||
                applicationDto.getAmount().compareTo(product.getMaxAmount()) > 0) {
            throw new ValidationException("Requested loan amount is outside the product's allowed range.");
        }
        if (applicationDto.getTenure() < product.getMinTenure() ||
                applicationDto.getTenure() > product.getMaxTenure()) {
            throw new ValidationException("Requested tenure is outside the product's allowed range.");
        }

        Loan loan = Loan.builder()
                .customerId(applicationDto.getCustomerId())
                .loanProduct(product)
                .principalAmount(applicationDto.getAmount())
                .interestRate(product.getInterestRate())
                .tenure(applicationDto.getTenure())
                .tenureUnit(product.getTenureType())
                .originationDate(LocalDate.now())
                .status(LoanStatus.PENDING_APPROVAL)
                .isInstallmentLoan(applicationDto.getIsInstallmentLoan())
                .outstandingAmount(applicationDto.getAmount())
                .totalRepaidAmount(BigDecimal.ZERO)
                .installments(new ArrayList<>())
                .appliedFees(new ArrayList<>())
                .repayments(new ArrayList<>())
                .build();

        List<AppliedFee> originationFees = feeService.applyOriginationFees(loan);
        loan.getAppliedFees().addAll(originationFees);

        if (loan.isInstallmentLoan()) {
            generateInstallmentSchedule(loan);
        } else {
            if (product.getTenureType() == TenureType.DAYS) {
                loan.setFinalDueDate(loan.getOriginationDate().plusDays(loan.getTenure()));
            } else {
                loan.setFinalDueDate(loan.getOriginationDate().plusMonths(loan.getTenure()));
            }
            loan.setNextBillingDate(loan.getFinalDueDate());
        }
        Loan savedLoan = loanRepository.save(loan);
        sendLoanCreationNotification(savedLoan, customer.getEmail());
        return loanMapper.toDto(savedLoan);
    }

    private void generateInstallmentSchedule(Loan loan) {
        int numberOfInstallments = loan.getTenure();
        BigDecimal totalLoanAmount = loan.getOutstandingAmount(); // Principal + Origination Fees
        BigDecimal monthlyPrincipal = loan.getPrincipalAmount().divide(BigDecimal.valueOf(numberOfInstallments), 2, RoundingMode.HALF_UP);
        BigDecimal annualInterestRate = loan.getInterestRate().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal loanTermInYears;
        if (loan.getTenureUnit() == TenureType.MONTHS) {
            loanTermInYears = BigDecimal.valueOf(loan.getTenure()).divide(BigDecimal.valueOf(12), 4, RoundingMode.HALF_UP);
        } else {
            loanTermInYears = BigDecimal.valueOf(loan.getTenure()).divide(BigDecimal.valueOf(365), 4, RoundingMode.HALF_UP); // Approx
        }
        BigDecimal totalSimpleInterest = loan.getPrincipalAmount().multiply(annualInterestRate).multiply(loanTermInYears);
        BigDecimal perInstallmentInterest = totalSimpleInterest.divide(BigDecimal.valueOf(numberOfInstallments), 2, RoundingMode.HALF_UP);
        BigDecimal totalOriginationFees = loan.getAppliedFees().stream()
                .filter(af -> af.getFeeType() == FeeType.SERVICE_FEE)
                .map(AppliedFee::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal perInstallmentFee = totalOriginationFees.divide(BigDecimal.valueOf(numberOfInstallments), 2, RoundingMode.HALF_UP);
        LocalDate installmentDueDate = loan.getOriginationDate();
        List<Installment> installments = new ArrayList<>();
        for (int i = 1; i <= numberOfInstallments; i++) {
            if (loan.getTenureUnit() == TenureType.MONTHS) {
                installmentDueDate = loan.getOriginationDate().plusMonths(i);
            } else {
                installmentDueDate = loan.getOriginationDate().plusDays(i);
            }

            BigDecimal currentPrincipal = (i == numberOfInstallments) ?
                    loan.getPrincipalAmount().subtract(monthlyPrincipal.multiply(BigDecimal.valueOf(numberOfInstallments - 1))) : monthlyPrincipal;
            BigDecimal currentInterest = (i == numberOfInstallments) ?
                    totalSimpleInterest.subtract(perInstallmentInterest.multiply(BigDecimal.valueOf(numberOfInstallments - 1))) : perInstallmentInterest;
            BigDecimal currentFee = (i == numberOfInstallments) ?
                    totalOriginationFees.subtract(perInstallmentFee.multiply(BigDecimal.valueOf(numberOfInstallments - 1))) : perInstallmentFee;
            Installment installment = Installment.builder()
                    .loan(loan)
                    .installmentNumber(i)
                    .dueDate(installmentDueDate)
                    .principalComponent(currentPrincipal)
                    .interestComponent(currentInterest)
                    .feeComponent(currentFee)
                    .totalAmountDue(currentPrincipal.add(currentInterest).add(currentFee))
                    .amountPaid(BigDecimal.ZERO)
                    .status(InstallmentStatus.PENDING)
                    .build();
            installments.add(installment);
        }
        loan.setInstallments(installments);
        loan.setFinalDueDate(installments.getLast().getDueDate());
        loan.setNextBillingDate(installments.getFirst().getDueDate());
    }

    private void sendLoanCreationNotification(Loan loan, String customerEmail) {
        NotificationEventDto event = NotificationEventDto.builder()
                .eventType("LOAN_APPLICATION_SUBMITTED")
                .customerId(loan.getCustomerId())
                .payload(Map.of(
                        "loanId", loan.getId().toString(),
                        "productName", loan.getLoanProduct().getName(),
                        "amount", loan.getPrincipalAmount().toPlainString(),
                        "status", loan.getStatus().toString()
                ))
                .build();
        notificationEventProducer.sendNotificationEvent(event);
    }


    @Override
    @Transactional(readOnly = true)
    public LoanResponseDto getLoanById(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with ID: " + loanId));
        return loanMapper.toDto(loan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanResponseDto> getLoansByCustomerId(Long customerId) {
        return loanRepository.findByCustomerId(customerId).stream()
                .map(loanMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LoanResponseDto disburseLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with ID: " + loanId));
        if (loan.getStatus() != LoanStatus.PENDING_APPROVAL) {
            throw new LoanProcessingException("Loan is not in PENDING_APPROVAL state. Current state: " + loan.getStatus());
        }
        loan.setStatus(LoanStatus.OPEN);
        loan.setDisbursementDate(LocalDate.now());
        Loan savedLoan = loanRepository.save(loan);
        NotificationEventDto event = NotificationEventDto.builder()
                .eventType("LOAN_DISBURSED")
                .customerId(loan.getCustomerId())
                .payload(Map.of("loanId", loan.getId().toString(), "disbursementDate", loan.getDisbursementDate().toString()))
                .build();
        notificationEventProducer.sendNotificationEvent(event);

        return loanMapper.toDto(savedLoan);
    }

    @Override
    @Transactional
    public LoanResponseDto cancelLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with ID: " + loanId));
        if (loan.getStatus() == LoanStatus.PENDING_APPROVAL ||
                (loan.getStatus() == LoanStatus.OPEN && loan.getDisbursementDate() == null)) {
            loan.setStatus(LoanStatus.CANCELLED);
            Loan savedLoan = loanRepository.save(loan);
            NotificationEventDto event = NotificationEventDto.builder()
                    .eventType("LOAN_CANCELLED")
                    .customerId(loan.getCustomerId())
                    .payload(Map.of("loanId", loan.getId().toString()))
                    .build();
            notificationEventProducer.sendNotificationEvent(event);
            return loanMapper.toDto(savedLoan);
        } else {
            throw new LoanProcessingException("Loan cannot be cancelled. Current status: " + loan.getStatus() +
                    (loan.getDisbursementDate() != null ? ", Disbursed on: " + loan.getDisbursementDate() : ""));
        }
    }

    @Override
    @Transactional
    public RepaymentResponseDto processRepayment(Long loanId, RepaymentRequestDto repaymentDto) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with ID: " + loanId));
        if (loan.getStatus() == LoanStatus.CLOSED || loan.getStatus() == LoanStatus.WRITTEN_OFF || loan.getStatus() == LoanStatus.CANCELLED) {
            throw new LoanProcessingException("Cannot process repayment for loan in status: " + loan.getStatus());
        }
        if (repaymentDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Repayment amount must be positive.");
        }
        if (repaymentDto.getAmount().compareTo(loan.getOutstandingAmount()) > 0) {
            repaymentDto.setAmount(loan.getOutstandingAmount());
        }
        Repayment repayment = Repayment.builder()
                .loan(loan)
                .amount(repaymentDto.getAmount())
                .paymentDateTime(LocalDateTime.now())
                .paymentMethod(repaymentDto.getPaymentMethod())
                .transactionReference(repaymentDto.getTransactionReference())
                .build();
        BigDecimal remainingRepaymentAmount = repaymentDto.getAmount();
        List<AppliedFee> unpaidAdhocFees = loan.getAppliedFees().stream()
                .filter(fee -> !fee.isPaid() && fee.getFeeType() != FeeType.SERVICE_FEE)
                .toList();

        for (AppliedFee fee : unpaidAdhocFees) {
            if (remainingRepaymentAmount.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal amountToPayFee = fee.getAmount().min(remainingRepaymentAmount);
            fee.setPaid(true);
            remainingRepaymentAmount = remainingRepaymentAmount.subtract(amountToPayFee);
        }
        if (loan.isInstallmentLoan()) {
            List<Installment> pendingInstallments = installmentRepository.findByLoanIdAndStatusOrderByDueDateAsc(loanId, InstallmentStatus.PENDING);
            List<Installment> overdueInstallments = installmentRepository.findByLoanIdAndStatusOrderByDueDateAsc(loanId, InstallmentStatus.OVERDUE);
            List<Installment> dueInstallments = new ArrayList<>(overdueInstallments);
            dueInstallments.addAll(pendingInstallments);
            for (Installment inst : dueInstallments) {
                if (remainingRepaymentAmount.compareTo(BigDecimal.ZERO) <= 0) break;
                BigDecimal dueForInstallment = inst.getTotalAmountDue().subtract(inst.getAmountPaid());
                BigDecimal paymentForThisInstallment = remainingRepaymentAmount.min(dueForInstallment);
                inst.setAmountPaid(inst.getAmountPaid().add(paymentForThisInstallment));
                remainingRepaymentAmount = remainingRepaymentAmount.subtract(paymentForThisInstallment);
                if (inst.getAmountPaid().compareTo(inst.getTotalAmountDue()) >= 0) {
                    inst.setStatus(InstallmentStatus.PAID);
                    inst.setPaymentDate(LocalDate.now());
                }
                if (repayment.getInstallment() == null && paymentForThisInstallment.compareTo(BigDecimal.ZERO) > 0) {
                    repayment.setInstallment(inst);
                }
            }
        }
        loan.setTotalRepaidAmount(loan.getTotalRepaidAmount().add(repaymentDto.getAmount()));
        loan.setOutstandingAmount(loan.getOutstandingAmount().subtract(repaymentDto.getAmount()));

        if (loan.getOutstandingAmount().compareTo(BigDecimal.valueOf(0.01)) < 0) {
            loan.setOutstandingAmount(BigDecimal.ZERO);
            loan.setStatus(LoanStatus.CLOSED);
            loan.setNextBillingDate(null);
        } else {
            if (loan.getStatus() == LoanStatus.OVERDUE) {
                boolean hasOverdueInstallments = loan.getInstallments().stream()
                        .anyMatch(i -> i.getStatus() == InstallmentStatus.OVERDUE && i.getDueDate().isBefore(LocalDate.now()));
                if (!hasOverdueInstallments) {
                    loan.setStatus(LoanStatus.OPEN);
                }
            }
            loan.getInstallments().stream()
                    .filter(i -> i.getStatus() == InstallmentStatus.PENDING || i.getStatus() == InstallmentStatus.OVERDUE)
                    .map(Installment::getDueDate)
                    .min(LocalDate::compareTo)
                    .ifPresent(loan::setNextBillingDate);
        }

        repaymentRepository.save(repayment);
        loan.getRepayments().add(repayment);
        Loan updatedLoan = loanRepository.save(loan);
        NotificationEventDto event = NotificationEventDto.builder()
                .eventType("REPAYMENT_RECEIVED")
                .customerId(loan.getCustomerId())
                .payload(Map.of(
                        "loanId", loan.getId().toString(),
                        "amountPaid", repaymentDto.getAmount().toPlainString(),
                        "outstandingAmount", updatedLoan.getOutstandingAmount().toPlainString(),
                        "loanStatus", updatedLoan.getStatus().toString()
                ))
                .build();
        notificationEventProducer.sendNotificationEvent(event);
        String message = "Repayment of " + repaymentDto.getAmount() + " processed successfully.";
        if (updatedLoan.getStatus() == LoanStatus.CLOSED) {
            message += " Loan is now closed.";
        }
        return repaymentMapper.toDto(repayment, message);
    }


    @Override
    @Transactional
    public void processOverdueLoans() {
        LocalDate today = LocalDate.now();
        List<LoanStatus> statusesToProcess = Arrays.asList(LoanStatus.OPEN, LoanStatus.OVERDUE);
        List<Loan> loansToCheck = loanRepository.findByStatusIn(statusesToProcess);
        for (Loan loan : loansToCheck) {
            boolean madeOverdue = false;
            if (loan.isInstallmentLoan()) {
                for (Installment installment : loan.getInstallments()) {
                    if ((installment.getStatus() == InstallmentStatus.PENDING || installment.getStatus() == InstallmentStatus.OVERDUE) &&
                            installment.getDueDate().isBefore(today) &&
                            installment.getAmountPaid().compareTo(installment.getTotalAmountDue()) < 0) {
                        if (installment.getStatus() != InstallmentStatus.OVERDUE) {
                            installment.setStatus(InstallmentStatus.OVERDUE);
                        }
                        feeService.applyLateFeeIfNeeded(loan, installment);
                        madeOverdue = true;
                    }
                }
            } else {
                if (loan.getFinalDueDate() != null && loan.getFinalDueDate().isBefore(today) &&
                        loan.getOutstandingAmount().compareTo(BigDecimal.ZERO) > 0) {
                    madeOverdue = true;
                    feeService.applyLateFeeIfNeeded(loan);
                }
            }

            if (madeOverdue && loan.getStatus() != LoanStatus.OVERDUE) {
                loan.setStatus(LoanStatus.OVERDUE);
                NotificationEventDto event = NotificationEventDto.builder()
                        .eventType("LOAN_OVERDUE")
                        .customerId(loan.getCustomerId())
                        .payload(Map.of(
                                "loanId", loan.getId().toString(),
                                "outstandingAmount", loan.getOutstandingAmount().toPlainString()
                        ))
                        .build();
                notificationEventProducer.sendNotificationEvent(event);
            }

            feeService.applyDailyFees(loan);
            loanRepository.save(loan);
        }
    }
}