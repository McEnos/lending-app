package org.ezra.lendingservice.mapper;

import lombok.RequiredArgsConstructor;
import org.ezra.lendingservice.dto.LoanProductRequestDto;
import org.ezra.lendingservice.dto.LoanProductResponseDto;
import org.ezra.lendingservice.entity.LoanProduct;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LoanProductMapper {

    private final FeeConfigurationMapper feeConfigurationMapper;

    public LoanProductResponseDto toDto(LoanProduct entity) {
        if (entity == null) return null;
        return LoanProductResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .minAmount(entity.getMinAmount())
                .maxAmount(entity.getMaxAmount())
                .interestRate(entity.getInterestRate())
                .tenureType(entity.getTenureType())
                .minTenure(entity.getMinTenure())
                .maxTenure(entity.getMaxTenure())
                .feeConfigurations(entity.getFeeConfigurations().stream()
                        .map(feeConfigurationMapper::toDto)
                        .collect(Collectors.toList()))
                .build();
    }

    public LoanProduct toEntity(LoanProductRequestDto dto) {
        if (dto == null) return null;
        LoanProduct product = LoanProduct.builder()
                .name(dto.getName())
                .minAmount(dto.getMinAmount())
                .maxAmount(dto.getMaxAmount())
                .interestRate(dto.getInterestRate())
                .tenureType(dto.getTenureType())
                .minTenure(dto.getMinTenure())
                .maxTenure(dto.getMaxTenure())
                .build();
        if (dto.getFeeConfigurations() != null) {
            product.setFeeConfigurations(dto.getFeeConfigurations().stream()
                    .map(feeDto -> feeConfigurationMapper.toEntity(feeDto, product))
                    .collect(Collectors.toList()));
        }
        return product;
    }
}