package org.ezra.lendingservice.service.impl;

import org.ezra.lendingservice.entity.*;
import org.ezra.lendingservice.enums.*;
import org.ezra.lendingservice.repository.AppliedFeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeeServiceImplTest {

    @Mock
    private AppliedFeeRepository appliedFeeRepository;

    @InjectMocks
    private FeeServiceImpl feeService;

    private Loan loan;
    private LoanProduct loanProduct;
    private FeeConfiguration fixedServiceFeeConfig;
    private FeeConfiguration percentageLateFeeConfig;
    private FeeConfiguration dailyFeeConfig;

    @BeforeEach
    void setUp() {
        fixedServiceFeeConfig = FeeConfiguration.builder()
                .feeType(FeeType.SERVICE_FEE)
                .calculationType(FeeCalculationType.FIXED)
                .value(BigDecimal.valueOf(50))
                .applicationTime(FeeApplicationTime.ORIGINATION)
                .build();

        percentageLateFeeConfig = FeeConfiguration.builder()
                .feeType(FeeType.LATE_FEE)
                .calculationType(FeeCalculationType.PERCENTAGE)
                .value(BigDecimal.valueOf(5)) // 5%
                .daysAfterDueForLateFee(3)
                .build();

        dailyFeeConfig = FeeConfiguration.builder()
                .feeType(FeeType.DAILY_FEE)
                .calculationType(FeeCalculationType.FIXED)
                .value(BigDecimal.valueOf(0.50)) // 0.50 per day
                .build();

        loanProduct = LoanProduct.builder()
                .id(1L)
                .name("Test Product")
                .feeConfigurations(Arrays.asList(fixedServiceFeeConfig, percentageLateFeeConfig, dailyFeeConfig))
                .build();

        loan = Loan.builder()
                .id(1L)
                .loanProduct(loanProduct)
                .principalAmount(BigDecimal.valueOf(1000))
                .outstandingAmount(BigDecimal.valueOf(1000))
                .status(LoanStatus.OPEN)
                .appliedFees(new ArrayList<>())
                .installments(new ArrayList<>())
                .build();

        when(appliedFeeRepository.save(any(AppliedFee.class))).thenAnswer(invocation -> {
            AppliedFee feeToSave = invocation.getArgument(0);
            if (feeToSave.getId() == null) {
                feeToSave.setId(System.nanoTime());
            }
            return feeToSave;
        });
    }

    @Test
    void calculateFeeAmount_fixed() {
        BigDecimal fee = feeService.calculateFeeAmount(BigDecimal.valueOf(1000), fixedServiceFeeConfig);
        assertEquals(0, BigDecimal.valueOf(50.00).compareTo(fee));
    }

    @Test
    void calculateFeeAmount_percentage() {
        BigDecimal fee = feeService.calculateFeeAmount(BigDecimal.valueOf(200), percentageLateFeeConfig); // 5% of 200
        assertEquals(0, BigDecimal.valueOf(10.00).compareTo(fee));
    }

    @Test
    void applyOriginationFees_appliesCorrectFee() {
        List<AppliedFee> applied = feeService.applyOriginationFees(loan);

        assertEquals(1, applied.size());
        assertEquals(FeeType.SERVICE_FEE, applied.get(0).getFeeType());
        assertEquals(0, BigDecimal.valueOf(50.00).compareTo(applied.get(0).getAmount()));
        assertEquals(0, BigDecimal.valueOf(1050.00).compareTo(loan.getOutstandingAmount())); // 1000 + 50
        verify(appliedFeeRepository, times(1)).save(any(AppliedFee.class));
    }

    @Test
    void applyLateFeeIfNeeded_forInstallment_appliesWhenOverdueAndPastGrace() {
        Installment installment = Installment.builder()
                .id(1L).loan(loan)
                .dueDate(LocalDate.now().minusDays(5))
                .status(InstallmentStatus.OVERDUE)
                .totalAmountDue(BigDecimal.valueOf(200))
                .amountPaid(BigDecimal.ZERO)
                .build();
        loan.getInstallments().add(installment);

        Optional<AppliedFee> appliedFeeOpt = feeService.applyLateFeeIfNeeded(loan, installment);

        assertTrue(appliedFeeOpt.isPresent());
        AppliedFee appliedFee = appliedFeeOpt.get();
        assertEquals(FeeType.LATE_FEE, appliedFee.getFeeType());
        assertEquals(0, BigDecimal.valueOf(10.00).compareTo(appliedFee.getAmount()));
        assertEquals(0, BigDecimal.valueOf(1010.00).compareTo(loan.getOutstandingAmount()));
        verify(appliedFeeRepository, times(1)).save(any(AppliedFee.class));
    }

    @Test
    void applyLateFeeIfNeeded_forInstallment_doesNotApplyIfNotPastGrace() {
        Installment installment = Installment.builder()
                .id(1L).loan(loan)
                .dueDate(LocalDate.now().minusDays(2)) // 2 days overdue, grace is 3
                .status(InstallmentStatus.OVERDUE)
                .totalAmountDue(BigDecimal.valueOf(200))
                .build();
        loan.getInstallments().add(installment);

        Optional<AppliedFee> appliedFeeOpt = feeService.applyLateFeeIfNeeded(loan, installment);
        assertFalse(appliedFeeOpt.isPresent());
        verify(appliedFeeRepository, never()).save(any(AppliedFee.class));
    }

    @Test
    void applyLateFeeIfNeeded_forInstallment_doesNotReapplyIfAlreadyApplied() {
        Installment installment = Installment.builder()
                .id(1L).loan(loan).installmentNumber(1)
                .dueDate(LocalDate.now().minusDays(5))
                .status(InstallmentStatus.OVERDUE)
                .totalAmountDue(BigDecimal.valueOf(200))
                .amountPaid(BigDecimal.ZERO)
                .build();
        loan.getInstallments().add(installment);

        // Simulate already applied fee
        AppliedFee existingLateFee = AppliedFee.builder()
                .loan(loan).feeType(FeeType.LATE_FEE).amount(BigDecimal.TEN)
                .dateApplied(LocalDate.now().minusDays(1))
                .reason("Late Fee - Installment #1")
                .build();
        loan.getAppliedFees().add(existingLateFee);

        Optional<AppliedFee> appliedFeeOpt = feeService.applyLateFeeIfNeeded(loan, installment);

        assertFalse(appliedFeeOpt.isPresent(), "Should not re-apply late fee if one was recently applied for the same reason.");
        verify(appliedFeeRepository, never()).save(any(AppliedFee.class));
    }


    @Test
    void applyDailyFees_appliesWhenConditionsMet() {
        loan.setStatus(LoanStatus.OVERDUE);
        loan.setOutstandingAmount(BigDecimal.valueOf(500));

        feeService.applyDailyFees(loan);

        assertEquals(1, loan.getAppliedFees().size());
        AppliedFee dailyFee = loan.getAppliedFees().getFirst();
        assertEquals(FeeType.DAILY_FEE, dailyFee.getFeeType());
        assertEquals(0, BigDecimal.valueOf(0.50).compareTo(dailyFee.getAmount()));
        assertEquals(0, BigDecimal.valueOf(500.50).compareTo(loan.getOutstandingAmount()));
        verify(appliedFeeRepository, times(1)).save(any(AppliedFee.class));
    }

    @Test
    void applyDailyFees_doesNotApplyIfAlreadyAppliedToday() {
        loan.setStatus(LoanStatus.OVERDUE);
        loan.setOutstandingAmount(BigDecimal.valueOf(500));

        AppliedFee existingDailyFee = AppliedFee.builder()
                .loan(loan).feeType(FeeType.DAILY_FEE).amount(BigDecimal.valueOf(0.50))
                .dateApplied(LocalDate.now())
                .build();
        loan.getAppliedFees().add(existingDailyFee);

        feeService.applyDailyFees(loan);

        long dailyFeeCount = loan.getAppliedFees().stream().filter(f -> f.getFeeType() == FeeType.DAILY_FEE).count();
        assertEquals(1, dailyFeeCount);
        verify(appliedFeeRepository, never()).save(any(AppliedFee.class));
    }
}