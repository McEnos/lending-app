package org.ezra.lendingservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplicationRequestDto {
    @NotNull(message = "Customer ID cannot be null")
    private Long customerId;

    @NotNull(message = "Product ID cannot be null")
    private Long productId;

    @NotNull(message = "Loan amount cannot be null")
    @Positive(message = "Loan amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Tenure cannot be null")
    @Positive(message = "Tenure must be positive")
    private Integer tenure;
    @NotNull(message = "isInstallmentLoan flag cannot be null")
    private Boolean isInstallmentLoan;
}