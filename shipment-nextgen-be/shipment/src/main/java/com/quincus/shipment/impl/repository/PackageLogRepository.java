package com.quincus.shipment.impl.repository;

import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.impl.repository.entity.PackageLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PackageLogRepository extends JpaRepository<PackageLogEntity, String> {

    Optional<PackageLogEntity> findByShipmentIdAndSource(String shipmentId, TriggeredFrom source);
}
