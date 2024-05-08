package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.domain.Package;
import com.quincus.shipment.impl.mapper.PackageLogMapper;
import com.quincus.shipment.impl.repository.PackageLogRepository;
import com.quincus.shipment.impl.repository.entity.PackageLogEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackageLogServiceTest {

    @InjectMocks
    private PackageLogService packageLogService;
    @Mock
    private PackageLogRepository packageLogRepository;
    @Mock
    private PackageLogMapper packageLogMapper;

    @Test
    void testCreatePackageLog_whenValid_thenTriggerSaveInRepository() {
        String shipmentId = "shipmentId";
        Package shipmentPackage = new Package();

        PackageLogEntity mappedEntity = new PackageLogEntity();
        when(packageLogMapper.createEntityFromShipmentPackage(shipmentId, shipmentPackage)).thenReturn(mappedEntity);
        packageLogService.createPackageLogForShipmentPackage(shipmentId, shipmentPackage);

        verify(packageLogRepository, times(1)).save(mappedEntity);

    }
}