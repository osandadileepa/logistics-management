package com.quincus.shipment.api;

import com.quincus.shipment.api.dto.BulkPackageDimensionUpdateRequest;
import com.quincus.shipment.api.dto.PackageDimensionErrorRecord;
import com.quincus.shipment.api.dto.PackageDimensionUpdateRequestFromApiG;
import com.quincus.shipment.api.dto.PackageDimensionUpdateResponse;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public interface PackageDimensionApi {
    void getPackageUpdateFileTemplate(PrintWriter writer) throws IOException;

    List<BulkPackageDimensionUpdateRequest> bulkPackageDimensionUpdateImport(File file);

    List<PackageDimensionErrorRecord> getAllErrorsFromBulkPackageDimensionUpdate(List<BulkPackageDimensionUpdateRequest> bulkPackageDimensionUpdates);

    List<PackageDimensionUpdateResponse> updateShipmentsPackageDimension(List<PackageDimensionUpdateRequestFromApiG> shipmentPackageDimensionUpdateRequests);

}
