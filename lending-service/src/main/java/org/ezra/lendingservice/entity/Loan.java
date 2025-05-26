package org.ezra.lendingservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ezra.lendingservice.enums.LoanStatus;
import org.ezra.lendingservice.enums.TenureType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_product_id", nullable = false)
    private LoanProduct loanProduct;

    @Column(nullable = false)
    private BigDecimal principalAmount;

    @Column(nullable = false)
    private BigDecimal interestRate;

    @Builder.Default
    private BigDecimal totalRepaidAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal outstandingAmount;

    @Column(nullable = false)
    private Integer tenure;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TenureType tenureUnit;

    private LocalDate originationDate;
    private LocalDate disbursementDate;
    private LocalDate finalDueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

    private boolean isInstallmentLoan;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("dueDate ASC")
    @Builder.Default
    private List<Installment> installments = new ArrayList<>();

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Repayment> repayments = new ArrayList<>();

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AppliedFee> appliedFees = new ArrayList<>();

    private LocalDate nextBillingDate;
    private String consolidatedBillingCycleId;
}