package com.quincus.shipment.impl.repository;

import com.quincus.shipment.impl.repository.entity.JobMetricsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface JobMetricsRepository extends JpaRepository<JobMetricsEntity, String> {
    Optional<JobMetricsEntity> findByIdAndOrganizationId(String jobId, String organizationId);
}
