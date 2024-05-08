package com.quincus.shipment.kafka.producers.mapper;

import com.quincus.shipment.api.domain.Package;
import com.quincus.shipment.api.domain.PackageDimension;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.kafka.producers.message.PackageDimensionsMessage;
import org.springframework.stereotype.Component;

@Component
public class ShipmentToPackageDimensionsMapperImpl implements ShipmentToPackageDimensionsMapper {

    @Override
    public PackageDimensionsMessage mapShipmentToPackageDimensionsMessage(Shipment shipment) {
        PackageDimensionsMessage packageDimensionsMessage = new PackageDimensionsMessage();
        Package shipmentPackage = shipment.getShipmentPackage();
        PackageDimension packageDimension = shipmentPackage.getDimension();
        packageDimensionsMessage.setPackageId(shipmentPackage.getId());
        packageDimensionsMessage.setRefId(shipmentPackage.getRefId());
        packageDimensionsMessage.setHeight(packageDimension.getHeight());
        packageDimensionsMessage.setWidth(packageDimension.getWidth());
        packageDimensionsMessage.setLength(packageDimension.getLength());
        packageDimensionsMessage.setMeasurement(packageDimension.getMeasurementUnit().getLabel());
        packageDimensionsMessage.setGrossWeight(packageDimension.getGrossWeight());
        packageDimensionsMessage.setOrgId(shipment.getOrganization().getId());
        packageDimensionsMessage.setCustom(packageDimension.isCustom());
        packageDimensionsMessage.setPackageTypeId(shipmentPackage.getTypeRefId());
        return packageDimensionsMessage;
    }
}
