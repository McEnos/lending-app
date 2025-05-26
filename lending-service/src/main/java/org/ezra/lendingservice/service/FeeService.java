package org.ezra.lendingservice.service;

import org.ezra.lendingservice.entity.AppliedFee;
import org.ezra.lendingservice.entity.FeeConfiguration;
import org.ezra.lendingservice.entity.Installment;
import org.ezra.lendingservice.entity.Loan;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface FeeService {
    BigDecimal calculateFeeAmount(BigDecimal baseAmount, FeeConfiguration feeConfig);
    List<AppliedFee> applyOriginationFees(Loan loan);
    Optional<AppliedFee> applyLateFeeIfNeeded(Loan loan, Installment installment);
    Optional<AppliedFee> applyLateFeeIfNeeded(Loan loan);
    void applyDailyFees(Loan loan);
}