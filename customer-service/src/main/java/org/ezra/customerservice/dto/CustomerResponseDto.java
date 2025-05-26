package org.ezra.customerservice.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String financialSummary;
    private BigDecimal currentLoanLimit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<LoanLimitChangeDto> loanLimitHistory;
}
