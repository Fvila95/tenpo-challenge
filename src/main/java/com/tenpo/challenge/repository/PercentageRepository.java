package com.tenpo.challenge.repository;

import com.tenpo.challenge.model.PercentageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PercentageRepository extends JpaRepository<PercentageEntity, Long> {
    PercentageEntity findFirstByOrderByIdDesc();

}

