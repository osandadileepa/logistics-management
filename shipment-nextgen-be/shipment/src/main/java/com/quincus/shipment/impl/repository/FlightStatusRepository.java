package com.quincus.shipment.impl.repository;

import com.quincus.shipment.impl.repository.entity.FlightStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlightStatusRepository extends JpaRepository<FlightStatusEntity, String> {
}
