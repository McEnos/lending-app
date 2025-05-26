package org.ezra.customerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanLimitChangeDto {
    private Long id;
    private BigDecimal previousLimit;
    private BigDecimal newLimit;
    private LocalDateTime changeTimestamp;
    private String reason;
    private String changedBy;
}