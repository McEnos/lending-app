package org.ezra.lendingservice.repository;

import org.ezra.lendingservice.entity.AppliedFee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppliedFeeRepository extends JpaRepository<AppliedFee, Long> {
}