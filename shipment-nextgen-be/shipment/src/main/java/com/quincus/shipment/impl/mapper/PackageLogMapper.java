package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.domain.Package;
import com.quincus.shipment.impl.repository.entity.PackageLogEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

@Component
public class PackageLogMapper {

    public PackageLogEntity createEntityFromShipmentPackage(String shipmentId, Package shipmentPackage) {
        if (StringUtils.isBlank(shipmentId) || shipmentPackage == null) {
            return null;
        }

        PackageLogEntity packageLogEntity = new PackageLogEntity();
        packageLogEntity.setShipmentId(shipmentId);
        return mapShipmentPackageToShipmentLogEntity(packageLogEntity, shipmentPackage);
    }

    public PackageLogEntity mapShipmentPackageToExistingEntity(@NotNull PackageLogEntity packageLogEntity, @NotNull Package shipmentPackage) {
        return mapShipmentPackageToShipmentLogEntity(packageLogEntity, shipmentPackage);
    }

    private PackageLogEntity mapShipmentPackageToShipmentLogEntity(PackageLogEntity packageLogEntity, Package shipmentPackage) {
        packageLogEntity.setPackageId(shipmentPackage.getId());
        packageLogEntity.setSource(shipmentPackage.getSource());
        packageLogEntity.setMeasurementUnit(shipmentPackage.getDimension().getMeasurementUnit());
        packageLogEntity.setVolumeWeight(shipmentPackage.getDimension().getVolumeWeight());
        packageLogEntity.setGrossWeight(shipmentPackage.getDimension().getGrossWeight());
        packageLogEntity.setChargeableWeight(shipmentPackage.getDimension().getChargeableWeight());
        packageLogEntity.setLength(shipmentPackage.getDimension().getLength());
        packageLogEntity.setWidth(shipmentPackage.getDimension().getWidth());
        packageLogEntity.setHeight(shipmentPackage.getDimension().getHeight());
        packageLogEntity.setCustom(shipmentPackage.getDimension().isCustom());
        return packageLogEntity;
    }
}
