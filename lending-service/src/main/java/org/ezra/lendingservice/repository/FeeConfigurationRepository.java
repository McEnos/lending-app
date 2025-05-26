package org.ezra.lendingservice.repository;

import org.ezra.lendingservice.entity.FeeConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeeConfigurationRepository extends JpaRepository<FeeConfiguration, Long> {
}