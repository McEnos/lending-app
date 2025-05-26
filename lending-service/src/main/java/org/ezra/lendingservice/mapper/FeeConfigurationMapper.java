package org.ezra.lendingservice.mapper;


import org.ezra.lendingservice.dto.FeeConfigurationDto;
import org.ezra.lendingservice.entity.FeeConfiguration;
import org.ezra.lendingservice.entity.LoanProduct;
import org.springframework.stereotype.Component;

@Component
public class FeeConfigurationMapper {

    public FeeConfigurationDto toDto(FeeConfiguration entity) {
        if (entity == null) return null;
        return FeeConfigurationDto.builder()
                .id(entity.getId())
                .feeType(entity.getFeeType())
                .calculationType(entity.getCalculationType())
                .value(entity.getFeeAmount())
                .applicationTime(entity.getApplicationTime())
                .daysAfterDueForLateFee(entity.getDaysAfterDueForLateFee())
                .conditions(entity.getConditions())
                .build();
    }

    public FeeConfiguration toEntity(FeeConfigurationDto dto, LoanProduct product) {
        if (dto == null) return null;
        return FeeConfiguration.builder()
                .loanProduct(product)
                .feeType(dto.getFeeType())
                .calculationType(dto.getCalculationType())
                .feeAmount(dto.getValue())
                .applicationTime(dto.getApplicationTime())
                .daysAfterDueForLateFee(dto.getDaysAfterDueForLateFee())
                .conditions(dto.getConditions())
                .build();
    }
}
