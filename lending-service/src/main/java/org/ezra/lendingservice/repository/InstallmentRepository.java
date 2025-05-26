package org.ezra.lendingservice.repository;

import org.ezra.lendingservice.entity.Installment;
import org.ezra.lendingservice.enums.InstallmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface InstallmentRepository extends JpaRepository<Installment, Long> {
    List<Installment> findByLoanIdAndStatusOrderByDueDateAsc(Long loanId, InstallmentStatus status);
    List<Installment> findByStatusAndDueDateBeforeOrderByDueDateAsc(InstallmentStatus status, LocalDate date);
}