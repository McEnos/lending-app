package org.ezra.lendingservice.dto;

import lombok.Builder;
import lombok.Data;
import org.ezra.lendingservice.entity.FeeConfiguration;
import org.ezra.lendingservice.enums.TenureType;

import java.util.List;

@Data
@Builder
public class ProductDto {
    private String name;
    private Integer tenureValue;
    private TenureType tenureType;
    private List<FeeConfiguration> feeConfigurations;
    private Integer daysAfterDueForFeeApplication;
}
