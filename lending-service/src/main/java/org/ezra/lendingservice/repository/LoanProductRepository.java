package org.ezra.lendingservice.repository;

import org.ezra.lendingservice.entity.LoanProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoanProductRepository extends JpaRepository<LoanProduct, Long> {
    Optional<LoanProduct> findByName(String name);
}