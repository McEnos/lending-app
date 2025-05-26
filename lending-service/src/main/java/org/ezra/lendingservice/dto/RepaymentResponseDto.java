package org.ezra.lendingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ezra.lendingservice.enums.LoanStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepaymentResponseDto {
    private Long id;
    private Long loanId;
    private Long installmentId;
    private BigDecimal amount;
    private LocalDateTime paymentDateTime;
    private String paymentMethod;
    private String transactionReference;
    private String message;
    private LoanStatus loanStatus;
}