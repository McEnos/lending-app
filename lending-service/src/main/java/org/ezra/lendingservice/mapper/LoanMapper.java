package org.ezra.lendingservice.mapper;

import lombok.RequiredArgsConstructor;
import org.ezra.lendingservice.dto.LoanResponseDto;
import org.ezra.lendingservice.entity.Loan;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LoanMapper {

    private final LoanProductMapper loanProductMapper;
    private final InstallmentMapper installmentMapper;
    private final AppliedFeeMapper appliedFeeMapper;

    public LoanResponseDto toDto(Loan entity) {
        if (entity == null) return null;
        LoanResponseDto dto = LoanResponseDto.builder()
                .id(entity.getId())
                .customerId(entity.getCustomerId())
                .loanProduct(loanProductMapper.toDto(entity.getLoanProduct()))
                .principalAmount(entity.getPrincipalAmount())
                .interestRate(entity.getInterestRate())
                .totalRepaidAmount(entity.getTotalRepaidAmount())
                .outstandingAmount(entity.getOutstandingAmount())
                .tenure(entity.getTenure())
                .tenureUnit(entity.getTenureUnit())
                .originationDate(entity.getOriginationDate())
                .disbursementDate(entity.getDisbursementDate())
                .finalDueDate(entity.getFinalDueDate())
                .status(entity.getStatus())
                .isInstallmentLoan(entity.isInstallmentLoan())
                .nextBillingDate(entity.getNextBillingDate())
                .build();

        if (entity.getInstallments() != null) {
            dto.setInstallments(entity.getInstallments().stream()
                    .map(installmentMapper::toDto)
                    .collect(Collectors.toList()));
        }
        if (entity.getAppliedFees() != null) {
            dto.setAppliedFees(entity.getAppliedFees().stream()
                    .map(appliedFeeMapper::toDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }
}