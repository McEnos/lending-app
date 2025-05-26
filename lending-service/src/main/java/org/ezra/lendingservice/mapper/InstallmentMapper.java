package org.ezra.lendingservice.mapper;

import org.ezra.lendingservice.dto.InstallmentDto;
import org.ezra.lendingservice.entity.Installment;
import org.springframework.stereotype.Component;

@Component
public class InstallmentMapper {
    public InstallmentDto toDto(Installment entity) {
        if (entity == null) return null;
        return InstallmentDto.builder()
                .id(entity.getId())
                .installmentNumber(entity.getInstallmentNumber())
                .dueDate(entity.getDueDate())
                .principalComponent(entity.getPrincipalComponent())
                .interestComponent(entity.getInterestComponent())
                .feeComponent(entity.getFeeComponent())
                .totalAmountDue(entity.getTotalAmountDue())
                .amountPaid(entity.getAmountPaid())
                .status(entity.getStatus())
                .paymentDate(entity.getPaymentDate())
                .build();
    }
}