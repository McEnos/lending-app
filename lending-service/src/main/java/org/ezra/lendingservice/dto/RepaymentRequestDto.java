package org.ezra.lendingservice.dto;

import jakarta.validation.constraints.NotBlank;
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
public class RepaymentRequestDto {
    @NotNull(message = "Repayment amount cannot be null")
    @Positive(message = "Repayment amount must be positive")
    private BigDecimal amount;
    @NotBlank(message = "Payment method cannot be blank")
    private String paymentMethod;
    private String transactionReference;
}