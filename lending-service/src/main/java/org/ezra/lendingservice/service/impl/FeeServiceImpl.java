package org.ezra.lendingservice.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ezra.lendingservice.entity.AppliedFee;
import org.ezra.lendingservice.entity.FeeConfiguration;
import org.ezra.lendingservice.entity.Installment;
import org.ezra.lendingservice.entity.Loan;
import org.ezra.lendingservice.enums.*;
import org.ezra.lendingservice.repository.AppliedFeeRepository;
import org.ezra.lendingservice.service.FeeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class FeeServiceImpl implements FeeService {

    private final AppliedFeeRepository appliedFeeRepository;

    @Override
    public BigDecimal calculateFeeAmount(BigDecimal baseAmount, FeeConfiguration feeConfig) {
        if (feeConfig.getCalculationType() == FeeCalculationType.FIXED) {
            return feeConfig.getFeeAmount().setScale(2, RoundingMode.HALF_UP);
        } else if (feeConfig.getCalculationType() == FeeCalculationType.PERCENTAGE) {
            BigDecimal percentage = feeConfig.getFeeAmount().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            return baseAmount.multiply(percentage).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    @Override
    @Transactional
    public List<AppliedFee> applyOriginationFees(Loan loan) {
        List<AppliedFee> appliedFees = new ArrayList<>();
        BigDecimal totalOriginationFees = BigDecimal.ZERO;

        for (FeeConfiguration feeConfig : loan.getLoanProduct().getFeeConfigurations()) {
            if (feeConfig.getFeeType() == FeeType.SERVICE_FEE &&
                    (feeConfig.getApplicationTime() == FeeApplicationTime.ORIGINATION ||
                            feeConfig.getApplicationTime() == FeeApplicationTime.POST_DISBURSEMENT)) {

                BigDecimal feeAmount = calculateFeeAmount(loan.getPrincipalAmount(), feeConfig);
                AppliedFee appliedFee = AppliedFee.builder()
                        .loan(loan)
                        .feeType(feeConfig.getFeeType())
                        .amount(feeAmount)
                        .dateApplied(LocalDate.now())
                        .reason("Origination Service Fee")
                        .paid(false)
                        .build();
                appliedFees.add(appliedFeeRepository.save(appliedFee));
                totalOriginationFees = totalOriginationFees.add(feeAmount);
            }
        }
        loan.setOutstandingAmount(loan.getOutstandingAmount().add(totalOriginationFees));
        return appliedFees;
    }


    private Optional<AppliedFee> applyLateFeeInternal(Loan loan, BigDecimal overdueAmount, String reasonSuffix, FeeConfiguration feeConfig) {
        BigDecimal lateFeeAmount = calculateFeeAmount(overdueAmount, feeConfig);
        if (lateFeeAmount.compareTo(BigDecimal.ZERO) > 0) {
            AppliedFee lateFee = AppliedFee.builder()
                    .loan(loan)
                    .feeType(FeeType.LATE_FEE)
                    .amount(lateFeeAmount)
                    .dateApplied(LocalDate.now())
                    .reason("Late Fee - " + reasonSuffix)
                    .paid(false)
                    .build();
            appliedFeeRepository.save(lateFee);
            loan.setOutstandingAmount(loan.getOutstandingAmount().add(lateFeeAmount));
            loan.getAppliedFees().add(lateFee);
            return Optional.of(lateFee);
        }
        return Optional.empty();
    }


    @Override
    @Transactional
    public Optional<AppliedFee> applyLateFeeIfNeeded(Loan loan, Installment installment) {
        if (installment.getStatus() != InstallmentStatus.OVERDUE) {
            return Optional.empty();
        }
        for (FeeConfiguration feeConfig : loan.getLoanProduct().getFeeConfigurations()) {
            if (feeConfig.getFeeType() == FeeType.LATE_FEE && feeConfig.getDaysAfterDueForLateFee() != null) {
                long daysOverdue = ChronoUnit.DAYS.between(installment.getDueDate(), LocalDate.now());
                if (daysOverdue >= feeConfig.getDaysAfterDueForLateFee()) {
                    boolean alreadyApplied = loan.getAppliedFees().stream()
                            .anyMatch(af -> af.getFeeType() == FeeType.LATE_FEE &&
                                    af.getReason() != null &&
                                    af.getReason().contains("Installment #" + installment.getInstallmentNumber()) &&
                                    af.getDateApplied().isAfter(installment.getDueDate().plusDays(feeConfig.getDaysAfterDueForLateFee() -1 )) // Simple check
                            );
                    if(!alreadyApplied) {
                        BigDecimal overdueAmount = installment.getTotalAmountDue().subtract(installment.getAmountPaid());
                        return applyLateFeeInternal(loan, overdueAmount, "Installment #" + installment.getInstallmentNumber(), feeConfig);
                    } else {
                        log.debug("Late fee for installment {} already applied recently.", installment.getId());
                    }
                }
            }
        }
        return Optional.empty();
    }

    @Override
    @Transactional
    public Optional<AppliedFee> applyLateFeeIfNeeded(Loan loan) {
        if (loan.getStatus() != LoanStatus.OVERDUE || loan.isInstallmentLoan()) {
            return Optional.empty();
        }
        log.debug("Checking late fee for overdue lump sum loan ID: {}", loan.getId());

        for (FeeConfiguration feeConfig : loan.getLoanProduct().getFeeConfigurations()) {
            if (feeConfig.getFeeType() == FeeType.LATE_FEE && feeConfig.getDaysAfterDueForLateFee() != null) {
                long daysOverdue = ChronoUnit.DAYS.between(loan.getFinalDueDate(), LocalDate.now());
                if (daysOverdue >= feeConfig.getDaysAfterDueForLateFee()) {
                    boolean alreadyApplied = loan.getAppliedFees().stream()
                            .anyMatch(af -> af.getFeeType() == FeeType.LATE_FEE &&
                                    af.getReason() != null &&
                                    af.getReason().contains("Lump Sum") && // Be more specific if needed
                                    af.getDateApplied().isAfter(loan.getFinalDueDate().plusDays(feeConfig.getDaysAfterDueForLateFee() -1 ))
                            );
                    if(!alreadyApplied) {
                        BigDecimal overdueAmount = loan.getOutstandingAmount(); // Or principal + interest due if calculated differently
                        return applyLateFeeInternal(loan, overdueAmount, "Lump Sum", feeConfig);
                    } else {
                        log.debug("Late fee for lump sum loan {} already applied recently.", loan.getId());
                    }
                }
            }
        }
        return Optional.empty();
    }


    @Override
    @Transactional
    public void applyDailyFees(Loan loan) {
        if (loan.getStatus() != LoanStatus.OVERDUE && loan.getStatus() != LoanStatus.OPEN) {
            return;
        }
        log.debug("Checking daily fees for loan ID: {}", loan.getId());

        for (FeeConfiguration feeConfig : loan.getLoanProduct().getFeeConfigurations()) {
            if (feeConfig.getFeeType() == FeeType.DAILY_FEE) {
                if (loan.getOutstandingAmount().compareTo(BigDecimal.ZERO) > 0) {
                    LocalDate today = LocalDate.now();
                    boolean alreadyAppliedToday = loan.getAppliedFees().stream()
                            .anyMatch(af -> af.getFeeType() == FeeType.DAILY_FEE && af.getDateApplied().equals(today));
                    if (!alreadyAppliedToday) {
                        BigDecimal dailyFeeAmount = calculateFeeAmount(loan.getOutstandingAmount(), feeConfig);
                        if (dailyFeeAmount.compareTo(BigDecimal.ZERO) > 0) {
                            AppliedFee dailyFee = AppliedFee.builder()
                                    .loan(loan)
                                    .feeType(FeeType.DAILY_FEE)
                                    .amount(dailyFeeAmount)
                                    .dateApplied(today)
                                    .reason("Daily Accrued Fee")
                                    .paid(false)
                                    .build();
                            appliedFeeRepository.save(dailyFee);
                            loan.setOutstandingAmount(loan.getOutstandingAmount().add(dailyFeeAmount));
                            loan.getAppliedFees().add(dailyFee);
                        }
                    } else {
                        log.debug("Daily fee for loan {} already applied today.", loan.getId());
                    }
                }
            }
        }
    }
}