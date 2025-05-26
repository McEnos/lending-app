package org.ezra.lendingservice.repository;

import org.ezra.lendingservice.entity.Repayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepaymentRepository extends JpaRepository<Repayment, Long> {
}