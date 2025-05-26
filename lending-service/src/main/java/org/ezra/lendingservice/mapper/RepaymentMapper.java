package org.ezra.lendingservice.mapper;

import org.ezra.lendingservice.dto.RepaymentResponseDto;
import org.ezra.lendingservice.entity.Repayment;
import org.springframework.stereotype.Component;

@Component
public class RepaymentMapper {
    public RepaymentResponseDto toDto(Repayment entity, String message) {
        if (entity == null) return null;
        return RepaymentResponseDto.builder()
                .id(entity.getId())
                .loanId(entity.getLoan().getId())
                .installmentId(entity.getInstallment() != null ? entity.getInstallment().getId() : null)
                .amount(entity.getAmount())
                .paymentDateTime(entity.getPaymentDateTime())
                .paymentMethod(entity.getPaymentMethod())
                .transactionReference(entity.getTransactionReference())
                .message(message)
                .build();
    }
}