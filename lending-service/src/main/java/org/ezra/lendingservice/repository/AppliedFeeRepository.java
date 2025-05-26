package org.ezra.lendingservice.repository;

import org.ezra.lendingservice.entity.AppliedFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AppliedFeeRepository extends JpaRepository<AppliedFee, Long> {
    @Query("SELECT MAX(a.id) FROM AppliedFee a")
    Long findMaxId();
}