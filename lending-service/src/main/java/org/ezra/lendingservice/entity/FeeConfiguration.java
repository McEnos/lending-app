package org.ezra.lendingservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ezra.lendingservice.enums.FeeApplicationTime;
import org.ezra.lendingservice.enums.FeeCalculationType;
import org.ezra.lendingservice.enums.FeeType;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_product_id", nullable = false)
    private LoanProduct loanProduct;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeeType feeType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeeCalculationType calculationType;

    @Column(nullable = false)
    private BigDecimal feeAmount;

    @Enumerated(EnumType.STRING)
    private FeeApplicationTime applicationTime;

    private Integer daysAfterDueForLateFee;
    private String conditions;
}