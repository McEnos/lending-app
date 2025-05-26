package org.ezra.lendingservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ezra.lendingservice.enums.TenureType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private BigDecimal minAmount;
    private BigDecimal maxAmount;

    @Column(nullable = false)
    private BigDecimal interestRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TenureType tenureType;

    @Column(nullable = false)
    private Integer minTenure;

    @Column(nullable = false)
    private Integer maxTenure;

    @OneToMany(mappedBy = "loanProduct", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<FeeConfiguration> feeConfigurations = new ArrayList<>();
}