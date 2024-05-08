package com.quincus.shipment.impl.api;

import com.quincus.shipment.api.PackageDimensionApi;
import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.dto.BulkPackageDimensionUpdateRequest;
import com.quincus.shipment.api.dto.PackageDimensionErrorRecord;
import com.quincus.shipment.api.dto.PackageDimensionUpdateRequest;
import com.quincus.shipment.api.dto.PackageDimensionUpdateRequestFromApiG;
import com.quincus.shipment.api.dto.PackageDimensionUpdateResponse;
import com.quincus.shipment.api.dto.ValueOfGoods;
import com.quincus.shipment.impl.service.PackageDimensionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Service
@AllArgsConstructor
public class PackageDimensionApiImpl implements PackageDimensionApi {

    private final PackageDimensionService packageDimensionService;

    @Override
    public void getPackageUpdateFileTemplate(PrintWriter writer) throws IOException {
        packageDimensionService.getPackageUpdateFileTemplate(writer);
    }

    @Override
    public List<BulkPackageDimensionUpdateRequest> bulkPackageDimensionUpdateImport(File file) {
        return packageDimensionService.bulkPackageDimensionUpdate(file);
    }

    @Override
    public List<PackageDimensionErrorRecord> getAllErrorsFromBulkPackageDimensionUpdate(List<BulkPackageDimensionUpdateRequest> bulkPackageDimensionUpdates) {
        return packageDimensionService.getAllErrorsFromBulkPackageDimensionUpdate(bulkPackageDimensionUpdates);
    }

    @Override
    public List<PackageDimensionUpdateResponse> updateShipmentsPackageDimension(List<PackageDimensionUpdateRequestFromApiG> shipmentPackageDimensionUpdateRequests) {
        final List<PackageDimensionUpdateRequest> packageDimensionUpdateRequestList = shipmentPackageDimensionUpdateRequests.stream()
                .map(this::convertToPackageDimensionUpdateRequest)
                .toList();
        return packageDimensionService.updateShipmentsPackageDimension(packageDimensionUpdateRequestList);
    }

    private PackageDimensionUpdateRequest convertToPackageDimensionUpdateRequest(
            final PackageDimensionUpdateRequestFromApiG requestFromApiG) {
        final PackageDimensionUpdateRequest request = new PackageDimensionUpdateRequest();
        request.setShipmentTrackingId(requestFromApiG.getShipmentTrackingId());
        request.setPackageTypeName(requestFromApiG.getPackageTypeName());

        final ValueOfGoods vog = requestFromApiG.getValueOfGoods();
        request.setLength(vog.getLength());
        request.setWidth(vog.getWidth());
        request.setHeight(vog.getHeight());
        request.setGrossWeight(vog.getGrossWeight());
        request.setSource(TriggeredFrom.APIG);
        request.setMeasurementUnit(vog.getMeasurementUnit());

        return request;
    }
}
