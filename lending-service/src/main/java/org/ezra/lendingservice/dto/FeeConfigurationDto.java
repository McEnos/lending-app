package org.ezra.lendingservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ezra.lendingservice.enums.FeeApplicationTime;
import org.ezra.lendingservice.enums.FeeCalculationType;
import org.ezra.lendingservice.enums.FeeType;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeConfigurationDto {
    private Long id;
    @NotNull(message = "Fee type cannot be null")
    private FeeType feeType;
    @NotNull(message = "Calculation type cannot be null")
    private FeeCalculationType calculationType;
    @NotNull(message = "Fee value cannot be null")
    @Positive(message = "Fee value must be positive")
    private BigDecimal value;
    private FeeApplicationTime applicationTime;
    private Integer daysAfterDueForLateFee;
    private String conditions;
}
