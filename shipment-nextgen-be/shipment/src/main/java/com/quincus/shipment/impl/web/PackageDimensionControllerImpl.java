package com.quincus.shipment.impl.web;

import com.quincus.shipment.PackageDimensionController;
import com.quincus.shipment.api.PackageDimensionApi;
import com.quincus.shipment.api.dto.BulkPackageDimensionUpdateRequest;
import com.quincus.shipment.api.dto.BulkPackageDimensionUpdateResponse;
import com.quincus.shipment.api.dto.PackageDimensionErrorRecord;
import com.quincus.shipment.api.dto.PackageDimensionUpdateRequestFromApiG;
import com.quincus.shipment.api.dto.PackageDimensionUpdateResponse;
import com.quincus.web.common.model.Request;
import com.quincus.web.common.model.Response;
import com.quincus.web.common.utility.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.quincus.web.common.utility.FileConverter.convertMultipartToFile;

@AllArgsConstructor
@RestController
public class PackageDimensionControllerImpl implements PackageDimensionController {

    private final PackageDimensionApi packageDimensionApi;

    @Override
    @PreAuthorize("hasAuthority('DIMS_AND_WEIGHT_VIEW')")
    @LogExecutionTime
    public void getPackageUpdateFileTemplate(final HttpServletResponse servletResponse) throws IOException {
        servletResponse.setContentType("text/csv");
        servletResponse.addHeader("Content-Disposition", "attachment; filename=\"bulk-package-update-template.csv\"");
        packageDimensionApi.getPackageUpdateFileTemplate(servletResponse.getWriter());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.writeTo(servletResponse.getOutputStream());
    }

    @Override
    @PreAuthorize("hasAuthority('DIMS_AND_WEIGHT_EDIT')")
    @LogExecutionTime
    public Response<BulkPackageDimensionUpdateResponse> bulkPackageDimensionUpdateImport(final MultipartFile file) {
        List<BulkPackageDimensionUpdateRequest> bulkResult = packageDimensionApi.bulkPackageDimensionUpdateImport(convertMultipartToFile(file));
        List<PackageDimensionErrorRecord> records = new ArrayList<>();
        if (bulkResult.stream().anyMatch(BulkPackageDimensionUpdateRequest::isError)) {
            records = packageDimensionApi.getAllErrorsFromBulkPackageDimensionUpdate(bulkResult);
        }
        long successNum = bulkResult.stream().filter(res -> !res.isError()).count();
        return new Response<>(new BulkPackageDimensionUpdateResponse(bulkResult.size(), successNum, records));
    }

    @Override
    @LogExecutionTime
    public Response<List<PackageDimensionUpdateResponse>> updateShipmentsPackageDimension(
            final Request<List<PackageDimensionUpdateRequestFromApiG>> shipmentPackageDimensionUpdateRequest) {
        return new Response<>(packageDimensionApi.updateShipmentsPackageDimension(shipmentPackageDimensionUpdateRequest.getData()));
    }

}
