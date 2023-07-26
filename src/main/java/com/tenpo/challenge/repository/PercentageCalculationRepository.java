package com.tenpo.challenge.repository;

import com.tenpo.challenge.model.PercentageCalculation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PercentageCalculationRepository extends JpaRepository<PercentageCalculation, Long>, PagingAndSortingRepository<PercentageCalculation, Long> {
    PercentageCalculation findFirstByOrderByIdDesc();
    Page<PercentageCalculation> findAll(Pageable pageable);


}

