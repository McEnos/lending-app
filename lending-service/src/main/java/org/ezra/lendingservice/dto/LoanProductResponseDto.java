package org.ezra.lendingservice.dto;

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
public class LoanProductResponseDto {
    private Long id;
    private String name;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal interestRate;
    private TenureType tenureType;
    private Integer minTenure;
    private Integer maxTenure;
    private List<FeeConfigurationDto> feeConfigurations;
}