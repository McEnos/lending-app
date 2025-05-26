package org.ezra.lendingservice.repository;

import org.ezra.lendingservice.entity.Loan;
import org.ezra.lendingservice.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LoanJpaRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByCustomerId(Long customerId);

    List<Loan> findByStatusInAndNextBillingDateBefore(List<LoanStatus> statuses, LocalDate date);

    List<Loan> findByStatusIn(List<LoanStatus> statuses);
}
