package com.quincus.shipment.impl.repository;

import com.quincus.shipment.impl.repository.entity.PackageDimensionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PackageDimensionRepository extends JpaRepository<PackageDimensionEntity, String> {
}
