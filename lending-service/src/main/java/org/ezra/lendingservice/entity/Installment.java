package org.ezra.lendingservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ezra.lendingservice.enums.InstallmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Installment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(nullable = false)
    private Integer installmentNumber;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false)
    private BigDecimal principalComponent;

    @Column(nullable = false)
    private BigDecimal interestComponent;

    @Builder.Default
    private BigDecimal feeComponent = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal totalAmountDue;

    @Builder.Default
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstallmentStatus status;

    private LocalDate paymentDate;
}