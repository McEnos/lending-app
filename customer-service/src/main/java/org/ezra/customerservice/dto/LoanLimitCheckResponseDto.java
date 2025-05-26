package org.ezra.customerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanLimitCheckResponseDto {
    private Long customerId;
    private boolean eligible;
    private BigDecimal currentLoanLimit;
    private BigDecimal requestedAmount;
}
