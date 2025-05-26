package org.ezra.lendingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ezra.lendingservice.enums.InstallmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstallmentDto {
    private Long id;
    private Integer installmentNumber;
    private LocalDate dueDate;
    private BigDecimal principalComponent;
    private BigDecimal interestComponent;
    private BigDecimal feeComponent;
    private BigDecimal totalAmountDue;
    private BigDecimal amountPaid;
    private InstallmentStatus status;
    private LocalDate paymentDate;
}