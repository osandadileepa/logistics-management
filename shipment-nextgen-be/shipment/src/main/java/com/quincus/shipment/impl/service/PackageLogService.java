package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.domain.Package;
import com.quincus.shipment.impl.mapper.PackageLogMapper;
import com.quincus.shipment.impl.repository.PackageLogRepository;
import com.quincus.shipment.impl.repository.entity.PackageLogEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Slf4j
@AllArgsConstructor
public class PackageLogService {

    private final PackageLogRepository packageLogRepository;
    private final PackageLogMapper packageLogMapper;

    @Transactional
    public void createPackageLogForShipmentPackage(String shipmentId, Package shipmentPackage) {
        PackageLogEntity packageLogEntity = packageLogMapper.createEntityFromShipmentPackage(shipmentId, shipmentPackage);
        if (packageLogEntity == null) {
            log.warn("Cannot create PackageLog for ShipmentId: {}", shipmentId);
            return;
        }
        packageLogRepository.save(packageLogEntity);
    }

    @Transactional
    public void upsertPackageLogForShipmentPackage(String shipmentId, Package shipmentPackage) {
        PackageLogEntity packageLogEntity = packageLogRepository.findByShipmentIdAndSource(shipmentId, shipmentPackage.getSource()).orElse(null);
        if (packageLogEntity == null) {
            createPackageLogForShipmentPackage(shipmentId, shipmentPackage);
            return;
        }
        PackageLogEntity updatedPackageLogEntity = packageLogMapper.mapShipmentPackageToExistingEntity(packageLogEntity, shipmentPackage);
        packageLogRepository.save(updatedPackageLogEntity);
    }
}