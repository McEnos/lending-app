package org.ezra.lendingservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ezra.lendingservice.enums.TenureType;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanProductRequestDto {
    @NotBlank(message = "Product name cannot be blank")
    private String name;

    @NotNull(message = "Minimum amount cannot be null")
    @Positive(message = "Minimum amount must be positive")
    private BigDecimal minAmount;

    @NotNull(message = "Maximum amount cannot be null")
    @Positive(message = "Maximum amount must be positive")
    private BigDecimal maxAmount;

    @NotNull(message = "Interest rate cannot be null")
    @Positive(message = "Interest rate must be positive")
    private BigDecimal interestRate;

    @NotNull(message = "Tenure type cannot be null")
    private TenureType tenureType;

    @NotNull(message = "Minimum tenure cannot be null")
    @Positive(message = "Minimum tenure must be positive")
    private Integer minTenure;

    @NotNull(message = "Maximum tenure cannot be null")
    @Positive(message = "Maximum tenure must be positive")
    private Integer maxTenure;

    @NotEmpty(message = "Fee configurations cannot be empty")
    @Valid
    private List<FeeConfigurationDto> feeConfigurations;
}