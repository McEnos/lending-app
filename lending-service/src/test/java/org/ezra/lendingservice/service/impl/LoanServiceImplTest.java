package org.ezra.lendingservice.service.impl;

import org.ezra.lendingservice.client.CustomerServiceClient;
import org.ezra.lendingservice.dto.*;
import org.ezra.lendingservice.entity.*;
import org.ezra.lendingservice.enums.InstallmentStatus;
import org.ezra.lendingservice.enums.LoanStatus;
import org.ezra.lendingservice.enums.TenureType;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceImplTest {

    @Mock
    private LoanRepository loanRepository;
    @Mock private LoanProductRepository loanProductRepository;
    @Mock private InstallmentRepository installmentRepository;
    @Mock private RepaymentRepository repaymentRepository;
    @Mock private FeeService feeService;
    @Mock private CustomerServiceClient customerServiceClient;
    @Mock private LoanMapper loanMapper;
    @Mock private RepaymentMapper repaymentMapper;

    @InjectMocks
    private LoanServiceImpl loanService;

    @Captor
    private ArgumentCaptor<Loan> loanCaptor;
    @Captor
    private ArgumentCaptor<NotificationRequestDto> notificationCaptor;

    private LoanApplicationRequestDto applicationRequestDto;
    private LoanProduct loanProduct;
    private CustomerResponseDto customerResponseDto;
    private Loan loanEntity;
    private LoanResponseDto loanResponseDto;

    @BeforeEach
    void setUp() {
        applicationRequestDto = LoanApplicationRequestDto.builder()
                .customerId(1L)
                .productId(1L)
                .amount(BigDecimal.valueOf(1000))
                .tenure(12)
                .isInstallmentLoan(true)
                .build();

        loanProduct = LoanProduct.builder()
                .id(1L)
                .name("Monthly Loan")
                .minAmount(BigDecimal.valueOf(100))
                .maxAmount(BigDecimal.valueOf(5000))
                .interestRate(BigDecimal.valueOf(10.0))
                .tenureType(TenureType.MONTHS)
                .minTenure(6)
                .maxTenure(24)
                .feeConfigurations(Collections.emptyList())
                .build();

        customerResponseDto = CustomerResponseDto.builder()
                .id(1L)
                .email("test@example.com")
                .loanLimit(BigDecimal.valueOf(5000))
                .build();

        loanEntity = Loan.builder()
                .id(1L)
                .customerId(1L)
                .loanProduct(loanProduct)
                .principalAmount(applicationRequestDto.getAmount())
                .outstandingAmount(applicationRequestDto.getAmount())
                .status(LoanStatus.PENDING_APPROVAL)
                .originationDate(LocalDate.now())
                .tenure(applicationRequestDto.getTenure())
                .tenureUnit(loanProduct.getTenureType())
                .isInstallmentLoan(true)
                .installments(new ArrayList<>())
                .appliedFees(new ArrayList<>())
                .repayments(new ArrayList<>())
                .build();

        loanResponseDto = LoanResponseDto.builder().id(1L).status(LoanStatus.PENDING_APPROVAL).build(); // Simplified

        lenient().when(feeService.applyOriginationFees(any(Loan.class))).thenReturn(new ArrayList<>());

    }

    @Test
    void applyForLoan_success_installmentLoan() {
        when(customerServiceClient.getCustomerById(1L)).thenReturn(ResponseEntity.ok(customerResponseDto));
        when(customerServiceClient.isCustomerEligible(1L, applicationRequestDto.getAmount())).thenReturn(ResponseEntity.ok(true));
        when(loanProductRepository.findById(1L)).thenReturn(Optional.of(loanProduct));
        when(loanRepository.save(any(Loan.class))).thenReturn(loanEntity);
        when(loanMapper.toDto(any(Loan.class))).thenReturn(loanResponseDto);

        LoanResponseDto result = loanService.applyForLoan(applicationRequestDto);

        assertNotNull(result);
        assertEquals(LoanStatus.PENDING_APPROVAL, result.getStatus());

        verify(loanRepository).save(loanCaptor.capture());
        Loan savedLoan = loanCaptor.getValue();
        assertEquals(applicationRequestDto.getAmount(), savedLoan.getPrincipalAmount());
        assertTrue(savedLoan.isInstallmentLoan());
        assertFalse(savedLoan.getInstallments().isEmpty(), "Installments should be generated");
        assertEquals(12, savedLoan.getInstallments().size());
        assertNotNull(savedLoan.getFinalDueDate());
        assertNotNull(savedLoan.getNextBillingDate());

        verify(feeService).applyOriginationFees(any(Loan.class));
        assertEquals("LOAN_APPLICATION_SUBMITTED", notificationCaptor.getValue().getEventType());
    }

    @Test
    void applyForLoan_success_lumpSumLoan() {
        applicationRequestDto.setIsInstallmentLoan(false);
        applicationRequestDto.setTenure(30);
        loanProduct.setTenureType(TenureType.DAYS);

        when(customerServiceClient.getCustomerById(1L)).thenReturn(ResponseEntity.ok(customerResponseDto));
        when(customerServiceClient.isCustomerEligible(1L, applicationRequestDto.getAmount())).thenReturn(ResponseEntity.ok(true));
        when(loanProductRepository.findById(1L)).thenReturn(Optional.of(loanProduct));
        when(loanRepository.save(any(Loan.class))).thenReturn(loanEntity);
        when(loanMapper.toDto(any(Loan.class))).thenReturn(loanResponseDto);

        LoanResponseDto result = loanService.applyForLoan(applicationRequestDto);

        assertNotNull(result);
        verify(loanRepository).save(loanCaptor.capture());
        Loan savedLoan = loanCaptor.getValue();
        assertFalse(savedLoan.isInstallmentLoan());
        assertTrue(savedLoan.getInstallments().isEmpty());
        assertEquals(LocalDate.now().plusDays(30), savedLoan.getFinalDueDate());
        assertEquals(savedLoan.getFinalDueDate(), savedLoan.getNextBillingDate());
    }


    @Test
    void applyForLoan_customerNotFound_throwsResourceNotFound() {
        when(customerServiceClient.getCustomerById(1L)).thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));

        assertThrows(ResourceNotFoundException.class, () -> loanService.applyForLoan(applicationRequestDto));
    }

    @Test
    void applyForLoan_customerNotEligible_throwsLoanProcessingException() {
        when(customerServiceClient.getCustomerById(1L)).thenReturn(ResponseEntity.ok(customerResponseDto));
        when(customerServiceClient.isCustomerEligible(1L, applicationRequestDto.getAmount())).thenReturn(ResponseEntity.ok(false));
        assertThrows(LoanProcessingException.class, () -> loanService.applyForLoan(applicationRequestDto));
    }

    @Test
    void applyForLoan_productNotFound_throwsResourceNotFound() {
        when(customerServiceClient.getCustomerById(1L)).thenReturn(ResponseEntity.ok(customerResponseDto));
        when(customerServiceClient.isCustomerEligible(1L, applicationRequestDto.getAmount())).thenReturn(ResponseEntity.ok(true));
        when(loanProductRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> loanService.applyForLoan(applicationRequestDto));
    }

    @Test
    void applyForLoan_amountOutOfRange_throwsValidationException() {
        applicationRequestDto.setAmount(BigDecimal.valueOf(10));
        when(customerServiceClient.getCustomerById(1L)).thenReturn(ResponseEntity.ok(customerResponseDto));
        when(customerServiceClient.isCustomerEligible(1L, applicationRequestDto.getAmount())).thenReturn(ResponseEntity.ok(true));
        when(loanProductRepository.findById(1L)).thenReturn(Optional.of(loanProduct));

        assertThrows(ValidationException.class, () -> loanService.applyForLoan(applicationRequestDto));
    }


    @Test
    void disburseLoan_success() {
        loanEntity.setStatus(LoanStatus.PENDING_APPROVAL);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loanEntity));
        when(loanRepository.save(any(Loan.class))).thenReturn(loanEntity);
        when(loanMapper.toDto(loanEntity)).thenReturn(LoanResponseDto.builder().id(1L).status(LoanStatus.OPEN).build());

        LoanResponseDto result = loanService.disburseLoan(1L);

        assertEquals(LoanStatus.OPEN, result.getStatus());
        verify(loanRepository).save(loanCaptor.capture());
        assertEquals(LoanStatus.OPEN, loanCaptor.getValue().getStatus());
        assertNotNull(loanCaptor.getValue().getDisbursementDate());
        assertEquals("LOAN_DISBURSED", notificationCaptor.getValue().getEventType());
    }

    @Test
    void disburseLoan_notPendingApproval_throwsLoanProcessingException() {
        loanEntity.setStatus(LoanStatus.OPEN);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loanEntity));

        assertThrows(LoanProcessingException.class, () -> loanService.disburseLoan(1L));
    }

    @Test
    void processRepayment_fullRepayment_closesLoan() {
        loanEntity.setStatus(LoanStatus.OPEN);
        loanEntity.setOutstandingAmount(BigDecimal.valueOf(100));
        loanEntity.setTotalRepaidAmount(BigDecimal.ZERO);
        RepaymentRequestDto repaymentDto = RepaymentRequestDto.builder().amount(BigDecimal.valueOf(100)).paymentMethod("CARD").build();
        Repayment repaymentEntity = Repayment.builder().id(1L).amount(BigDecimal.valueOf(100)).loan(loanEntity).build();
        RepaymentResponseDto repaymentResponseDto = RepaymentResponseDto.builder().loanStatus(LoanStatus.valueOf(LoanStatus.CLOSED.toString())).build();


        when(loanRepository.findById(1L)).thenReturn(Optional.of(loanEntity));
        when(repaymentRepository.save(any(Repayment.class))).thenReturn(repaymentEntity);
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repaymentMapper.toDto(any(Repayment.class), anyString())).thenReturn(repaymentResponseDto);

        RepaymentResponseDto result = loanService.processRepayment(1L, repaymentDto);

        assertEquals(LoanStatus.CLOSED.toString(), result.getMessage() != null && result.getMessage().contains("closed") ? LoanStatus.CLOSED.toString() : "NOT_CLOSED");
        verify(loanRepository).save(loanCaptor.capture());
        assertEquals(LoanStatus.CLOSED, loanCaptor.getValue().getStatus());
        assertEquals(0, BigDecimal.ZERO.compareTo(loanCaptor.getValue().getOutstandingAmount()));
        assertEquals(0, BigDecimal.valueOf(100).compareTo(loanCaptor.getValue().getTotalRepaidAmount()));
    }

    @Test
    void processRepayment_partialRepayment_updatesOutstanding() {
        loanEntity.setStatus(LoanStatus.OPEN);
        loanEntity.setOutstandingAmount(BigDecimal.valueOf(200));
        RepaymentRequestDto repaymentDto = RepaymentRequestDto.builder().amount(BigDecimal.valueOf(50)).paymentMethod("CARD").build();
        Repayment repaymentEntity = Repayment.builder().id(1L).amount(BigDecimal.valueOf(50)).loan(loanEntity).build();
        RepaymentResponseDto repaymentResponseDto = RepaymentResponseDto.builder().loanStatus(LoanStatus.valueOf(LoanStatus.OPEN.toString())).build();


        when(loanRepository.findById(1L)).thenReturn(Optional.of(loanEntity));
        when(repaymentRepository.save(any(Repayment.class))).thenReturn(repaymentEntity);
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repaymentMapper.toDto(any(Repayment.class), anyString())).thenReturn(repaymentResponseDto);

        loanService.processRepayment(1L, repaymentDto);

        verify(loanRepository).save(loanCaptor.capture());
        assertEquals(LoanStatus.OPEN, loanCaptor.getValue().getStatus());
        assertEquals(0, BigDecimal.valueOf(150).compareTo(loanCaptor.getValue().getOutstandingAmount()));
    }

    @Test
    void processOverdueLoans_marksInstallmentOverdueAndAppliesLateFee() {
        Installment pendingInstallment = Installment.builder()
                .id(1L).loan(loanEntity)
                .dueDate(LocalDate.now().minusDays(5))
                .status(InstallmentStatus.PENDING)
                .totalAmountDue(BigDecimal.valueOf(100))
                .amountPaid(BigDecimal.ZERO)
                .build();
        loanEntity.getInstallments().add(pendingInstallment);
        loanEntity.setStatus(LoanStatus.OPEN);

        when(loanRepository.findByStatusIn(Arrays.asList(LoanStatus.OPEN, LoanStatus.OVERDUE)))
                .thenReturn(Collections.singletonList(loanEntity));
        when(feeService.applyLateFeeIfNeeded(eq(loanEntity), eq(pendingInstallment)))
                .thenAnswer(invocation -> {
                    loanEntity.setOutstandingAmount(loanEntity.getOutstandingAmount().add(BigDecimal.TEN));
                    return Optional.of(AppliedFee.builder().amount(BigDecimal.TEN).build());
                });
        when(loanRepository.save(any(Loan.class))).thenReturn(loanEntity);


        loanService.processOverdueLoans();

        assertEquals(InstallmentStatus.OVERDUE, pendingInstallment.getStatus());
        assertEquals(LoanStatus.OVERDUE, loanEntity.getStatus());
        verify(feeService).applyLateFeeIfNeeded(loanEntity, pendingInstallment);
        verify(feeService).applyDailyFees(loanEntity);
        assertEquals("LOAN_OVERDUE", notificationCaptor.getValue().getEventType());
        verify(loanRepository).save(loanEntity);
    }
}