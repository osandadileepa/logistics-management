package com.quincus.shipment.impl.api;

import com.quincus.shipment.api.constant.MeasurementUnit;
import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.dto.PackageDimensionUpdateRequest;
import com.quincus.shipment.api.dto.PackageDimensionUpdateRequestFromApiG;
import com.quincus.shipment.api.dto.PackageDimensionUpdateResponse;
import com.quincus.shipment.api.dto.ValueOfGoods;
import com.quincus.shipment.impl.service.PackageDimensionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackageDimensionApiImplTest {
    @InjectMocks
    private PackageDimensionApiImpl packageDimensionApi;
    @Mock
    private PackageDimensionService packageDimensionService;

    @Test
    void testUpdateShipmentsPackageDimension() {
        String packageTypeName = "Large Carton Box";
        String shipmentTrackingId = "QC1023092700021-001";
        ValueOfGoods vog = createValueOfGoods();

        PackageDimensionUpdateRequestFromApiG request = new PackageDimensionUpdateRequestFromApiG();
        request.setPackageTypeName(packageTypeName);
        request.setShipmentTrackingId(shipmentTrackingId);
        request.setValueOfGoods(vog);
        List<PackageDimensionUpdateRequestFromApiG> requests = List.of(request);

        PackageDimensionUpdateResponse response = mock(PackageDimensionUpdateResponse.class);
        List<PackageDimensionUpdateResponse> expectedResponses = Collections.singletonList(response);

        ArgumentCaptor<List<PackageDimensionUpdateRequest>> captor = ArgumentCaptor.forClass(List.class);
        when(packageDimensionService.updateShipmentsPackageDimension(captor.capture())).thenReturn(expectedResponses);

        List<PackageDimensionUpdateResponse> actualResponses = packageDimensionApi.updateShipmentsPackageDimension(requests);

        assertThat(actualResponses).isEqualTo(expectedResponses);
        captor.getValue().forEach(updateRequest -> {
            assertThat(updateRequest.getPackageTypeName()).isEqualTo(request.getPackageTypeName());
            assertThat(updateRequest.getShipmentTrackingId()).isEqualTo(request.getShipmentTrackingId());
            assertThat(updateRequest.getLength()).isEqualTo(vog.getLength());
            assertThat(updateRequest.getWidth()).isEqualTo(vog.getWidth());
            assertThat(updateRequest.getHeight()).isEqualTo(vog.getHeight());
            assertThat(updateRequest.getGrossWeight()).isEqualTo(vog.getGrossWeight());
            assertThat(updateRequest.getMeasurementUnit()).isEqualTo(vog.getMeasurementUnit());
            assertThat(updateRequest.getSource()).isEqualTo(TriggeredFrom.APIG);
        });
    }

    private ValueOfGoods createValueOfGoods() {
        ValueOfGoods valueOfGoods = new ValueOfGoods();

        valueOfGoods.setLength(new BigDecimal("1.00"));
        valueOfGoods.setWidth(new BigDecimal("2.00"));
        valueOfGoods.setHeight(new BigDecimal("3.00"));
        valueOfGoods.setGrossWeight(new BigDecimal("4.00"));
        valueOfGoods.setMeasurementUnit(MeasurementUnit.IMPERIAL);

        return valueOfGoods;
    }
}