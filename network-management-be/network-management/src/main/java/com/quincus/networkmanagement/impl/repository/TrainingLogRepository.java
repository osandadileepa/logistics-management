package com.quincus.networkmanagement.impl.repository;

import com.quincus.networkmanagement.impl.repository.entity.TrainingLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainingLogRepository extends JpaRepository<TrainingLogEntity, String>, JpaSpecificationExecutor<TrainingLogEntity> {
    Optional<TrainingLogEntity> findByUniqueId(String uniqueId);
}
