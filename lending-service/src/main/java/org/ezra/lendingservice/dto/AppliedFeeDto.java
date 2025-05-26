package org.ezra.lendingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ezra.lendingservice.enums.FeeType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppliedFeeDto {
    private Long id;
    private FeeType feeType;
    private BigDecimal amount;
    private LocalDate dateApplied;
    private String reason;
    private boolean paid;
}