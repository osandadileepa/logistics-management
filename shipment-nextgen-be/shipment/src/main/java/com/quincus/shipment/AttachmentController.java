package com.quincus.shipment;

import com.quincus.ext.annotation.UUID;
import com.quincus.shipment.api.dto.JobStatusResponse;
import com.quincus.shipment.api.filter.ExportFilter;
import com.quincus.web.common.model.Request;
import com.quincus.web.common.model.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@RestController
@RequestMapping("/attachments")
@Tag(name = "attachments", description = "This controller provides endpoints for uploading and downloading attachments in the form of MultipartFiles. It also includes endpoints for checking the status or progress of a job and canceling a job if necessary.")
@Validated
public interface AttachmentController {

    @GetMapping("/{attachmentType}/download-csv-template")
    @Operation(summary = "Download CSV Template", description = "This endpoint allows users to download a CSV template for a specified attachmentType. The CSV template can be used as a guide when creating a new CSV file to be uploaded using the uploadCsv endpoint.", tags = "attachments")
    ResponseEntity<Resource> downloadCsvTemplate(@NotBlank @Size(max = 30) @PathVariable("attachmentType") final String attachmentType);

    @PostMapping("/{attachmentType}/upload-csv")
    @Operation(summary = "Upload CSV file", description = "This API endpoint is used to upload a CSV (Comma Separated Values) file for a specific attachment type. The CSV file should be sent in a Multipart file format. The method returns a response containing the status of the job initiated by the uploaded CSV file.", tags = "attachments")
    Response<JobStatusResponse> uploadCsv(@NotBlank @Size(max = 30) @PathVariable("attachmentType") final String attachmentType, final @RequestParam("file") MultipartFile file);

    @GetMapping("/{attachmentType}/upload-status/{jobId}")
    @Operation(summary = "Check Upload Status", description = "This endpoint retrieves the status of a previously initiated upload job based on job id and attachment type.", tags = "attachments")
    Response<JobStatusResponse> checkUploadStatus(@NotBlank @Size(max = 30) @PathVariable("attachmentType") final String attachmentType, @UUID @PathVariable("jobId") final String jobId);

    @PutMapping("/{attachmentType}/cancel-upload/{jobId}")
    @Operation(summary = "Cancel Upload", description = "This endpoint cancels a previously uploaded CSV based on job id and attachment type.", tags = "attachments")
    Response<JobStatusResponse> cancelUpload(@NotBlank @Size(max = 30) @PathVariable("attachmentType") final String attachmentType, @UUID @PathVariable("jobId") final String jobId);

    @PostMapping("/{attachmentType}/export/pre-populated")
    @Operation(summary = "Export a pre-populated filtered data as CSV", description = "Generate and export a CSV containing pre-populated filtered data based on the provided request.", tags = "attachments")
    void export(@NotBlank @Size(max = 30) @PathVariable("attachmentType") final String attachmentType, @Valid @RequestBody final Request<ExportFilter> request, final HttpServletResponse servletResponse);
}
