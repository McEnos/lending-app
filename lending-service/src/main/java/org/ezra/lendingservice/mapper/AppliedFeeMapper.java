package org.ezra.lendingservice.mapper;

import org.ezra.lendingservice.dto.AppliedFeeDto;
import org.ezra.lendingservice.entity.AppliedFee;
import org.springframework.stereotype.Component;

@Component
public class AppliedFeeMapper {
    public AppliedFeeDto toDto(AppliedFee entity) {
        if (entity == null) return null;
        return AppliedFeeDto.builder()
                .id(entity.getId())
                .feeType(entity.getFeeType())
                .amount(entity.getAmount())
                .dateApplied(entity.getDateApplied())
                .reason(entity.getReason())
                .paid(entity.isPaid())
                .build();
    }
}