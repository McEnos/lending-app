package org.ezra.lendingservice.repository;

import org.ezra.lendingservice.entity.Loan;
import org.ezra.lendingservice.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByCustomerId(Long customerId);
    List<Loan> findByStatusIn(List<LoanStatus> statusList);
}
