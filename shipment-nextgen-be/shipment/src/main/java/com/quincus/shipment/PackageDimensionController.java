package com.quincus.shipment;

import com.quincus.shipment.api.dto.BulkPackageDimensionUpdateResponse;
import com.quincus.shipment.api.dto.PackageDimensionUpdateRequestFromApiG;
import com.quincus.shipment.api.dto.PackageDimensionUpdateResponse;
import com.quincus.web.common.model.Request;
import com.quincus.web.common.model.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@RequestMapping("/package-dimensions")
@Tag(name = "package-dimensions", description = "This endpoint allows to manage package dimension related transactions.")
public interface PackageDimensionController {


    @GetMapping("/bulk-update-file-template")
    @Operation(summary = "download file template", description = "Download a CSV template for bulk package dimension update", tags = "package-dimensions")
    void getPackageUpdateFileTemplate(final HttpServletResponse servletResponse) throws IOException;


    @PostMapping("/bulk-package-dimension-update-import")
    @Operation(summary = "Import Bulk Package Dimension Update File", description = "Accepts File to update package dimensions in bulk", tags = "package-dimensions")
    Response<BulkPackageDimensionUpdateResponse> bulkPackageDimensionUpdateImport(final @RequestParam("file") MultipartFile file);

    @PostMapping("/shipments")
    @Operation(
            summary = "Updating dimensions & weight on multiple shipments.",
            description = "Accepts an array of shipment w/ their updated package information.",
            tags = "package-dimensions"
    )
    Response<List<PackageDimensionUpdateResponse>> updateShipmentsPackageDimension(@Valid @RequestBody final Request<List<PackageDimensionUpdateRequestFromApiG>> shipmentPackageDimensionUpdateRequest);

}
