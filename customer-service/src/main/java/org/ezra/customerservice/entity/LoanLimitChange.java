package org.ezra.customerservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_limit_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanLimitChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal previousLimit;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal newLimit;

    @Column(nullable = false)
    private LocalDateTime changeTimestamp;

    @Column(length = 500)
    private String reason;

    private String changedBy;
}