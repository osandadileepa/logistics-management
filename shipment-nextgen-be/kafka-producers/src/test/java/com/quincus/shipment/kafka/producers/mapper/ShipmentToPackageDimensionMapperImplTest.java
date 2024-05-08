package com.quincus.shipment.kafka.producers.mapper;

import com.quincus.shipment.api.domain.PackageDimension;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.kafka.producers.message.PackageDimensionsMessage;
import com.quincus.shipment.kafka.producers.test_utils.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ShipmentToPackageDimensionMapperImplTest {

    private final TestUtil testUtil = TestUtil.getInstance();
    @InjectMocks
    private ShipmentToPackageDimensionsMapperImpl mapper;

    @Test
    void mapShipmentToPackageDimensionsMessage_validArguments_shouldReturnPackageDimensionMessage() {
        Shipment shipmentDomain = testUtil.createSingleShipmentFromOrderOnePackageMultiSegmentsJson();
        PackageDimensionsMessage packageDimensionsMessage = mapper.mapShipmentToPackageDimensionsMessage(shipmentDomain);
        PackageDimension packageDimension = shipmentDomain.getShipmentPackage().getDimension();

        assertThat(shipmentDomain.getShipmentPackage().getRefId()).isEqualTo(packageDimensionsMessage.getRefId());
        assertThat(shipmentDomain.getShipmentPackage().getId()).isEqualTo(packageDimensionsMessage.getPackageId());
        assertThat(shipmentDomain.getOrganization().getId()).isEqualTo(packageDimensionsMessage.getOrgId());
        assertThat(shipmentDomain.getShipmentPackage().getTypeRefId()).isEqualTo(packageDimensionsMessage.getPackageTypeId());
        assertThat(packageDimension.getLength()).isEqualTo(packageDimensionsMessage.getLength());
        assertThat(packageDimension.getWidth()).isEqualTo(packageDimensionsMessage.getWidth());
        assertThat(packageDimension.getMeasurementUnit().getLabel()).isEqualTo(packageDimensionsMessage.getMeasurement());
        assertThat(packageDimension.getGrossWeight()).isEqualTo(packageDimensionsMessage.getGrossWeight());
        assertThat(packageDimension.isCustom()).isEqualTo(packageDimensionsMessage.isCustom());
    }
}
