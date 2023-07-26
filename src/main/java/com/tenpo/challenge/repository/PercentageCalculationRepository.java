package com.tenpo.challenge.repository;

import com.tenpo.challenge.model.PercentageCalculation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PercentageCalculationRepository extends JpaRepository<PercentageCalculation, Long> {
    PercentageCalculation findFirstByOrderByIdDesc();

}

