package org.ezra.customerservice.repository;

import org.ezra.customerservice.entity.LoanLimitChange;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanLimitHistoryRepository extends JpaRepository<LoanLimitChange, Long> {
}