package org.ezra.lendingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ezra.lendingservice.enums.LoanStatus;
import org.ezra.lendingservice.enums.TenureType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanResponseDto {
    private Long id;
    private Long customerId;
    private LoanProductResponseDto loanProduct;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private BigDecimal totalRepaidAmount;
    private BigDecimal outstandingAmount;
    private Integer tenure;
    private TenureType tenureUnit;
    private LocalDate originationDate;
    private LocalDate disbursementDate;
    private LocalDate finalDueDate;
    private LoanStatus status;
    private boolean isInstallmentLoan;
    private List<InstallmentDto> installments;
    private List<AppliedFeeDto> appliedFees;
    private LocalDate nextBillingDate;
}