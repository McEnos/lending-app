package org.ezra.customerservice.repository;

import org.ezra.customerservice.entity.LoanLimitChange;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanLimitHistoryRepository extends JpaRepository<LoanLimitChange, Long> {
    List<LoanLimitChange> findByCustomerIdOrderByChangeTimestampDesc(Long customerId);
}