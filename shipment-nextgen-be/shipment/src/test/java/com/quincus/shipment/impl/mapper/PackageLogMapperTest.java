package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.domain.Package;
import com.quincus.shipment.api.domain.PackageDimension;
import com.quincus.shipment.impl.repository.entity.PackageLogEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PackageLogMapperTest {

    @InjectMocks
    private PackageLogMapper packageLogMapper;

    @Test
    void givenShipmentIdWithShipmentPackage_whenCreateEntityFromShipmentPackage_shouldMapShipmentPackageToPackageLogEntity() {
        Package shipmentPackage = new Package();
        shipmentPackage.setId("packageId-1");
        shipmentPackage.setSource(TriggeredFrom.OM);

        PackageDimension packageDimension = new PackageDimension();
        packageDimension.setHeight(BigDecimal.valueOf(100));
        packageDimension.setWidth(BigDecimal.valueOf(51));
        packageDimension.setLength(BigDecimal.valueOf(21));
        packageDimension.setGrossWeight(BigDecimal.valueOf(300));
        packageDimension.setVolumeWeight(BigDecimal.valueOf(250));
        packageDimension.setChargeableWeight(BigDecimal.valueOf(245));
        shipmentPackage.setDimension(packageDimension);

        PackageLogEntity packageLogEntity = packageLogMapper.createEntityFromShipmentPackage("shipmentId-111", shipmentPackage);
        assertThat(packageLogEntity).isNotNull();
        assertThat(packageLogEntity.getShipmentId()).isEqualTo("shipmentId-111");
        assertThat(packageLogEntity.getPackageId()).isEqualTo("packageId-1");
        assertThat(packageLogEntity.getLength()).isEqualTo(packageDimension.getLength());
        assertThat(packageLogEntity.getGrossWeight()).isEqualTo(packageDimension.getGrossWeight());
        assertThat(packageLogEntity.getChargeableWeight()).isEqualTo(packageDimension.getChargeableWeight());
        assertThat(packageLogEntity.getVolumeWeight()).isEqualTo(packageDimension.getVolumeWeight());
    }

}
