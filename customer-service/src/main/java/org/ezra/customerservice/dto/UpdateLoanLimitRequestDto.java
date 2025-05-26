package org.ezra.customerservice.dto;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateLoanLimitRequestDto {
    @NotNull(message = "New loan limit cannot be null")
    @DecimalMin(value = "0.0", inclusive = true, message = "New loan limit must be zero or positive")
    private BigDecimal newLoanLimit;

    @NotBlank(message = "Reason for change cannot be blank")
    private String reason;

    private String changedBy;
}
