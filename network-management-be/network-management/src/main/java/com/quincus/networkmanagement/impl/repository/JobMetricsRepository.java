package com.quincus.networkmanagement.impl.repository;

import com.quincus.networkmanagement.impl.repository.entity.JobMetricsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobMetricsRepository extends JpaRepository<JobMetricsEntity, String>, JpaSpecificationExecutor<JobMetricsEntity> {
    Optional<JobMetricsEntity> findByIdAndOrganizationId(String jobId, String organizationId);
}
