package com.quincus.networkmanagement;

import com.quincus.networkmanagement.api.dto.JobStatusResponse;
import com.quincus.web.common.model.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/attachments")
@Tag(name = "attachments")
public interface AttachmentController {

    @GetMapping("/{attachmentType}/template")
    @Operation(summary = "Download template file based on the Attachment Type", tags = "attachments")
    ResponseEntity<Resource> downloadTemplateFile(@PathVariable("attachmentType") final String attachmentType);

    @PostMapping("/{attachmentType}/upload")
    @Operation(summary = "Upload a file", tags = "attachments")
    Response<JobStatusResponse> upload(
            @PathVariable("attachmentType") final String attachmentType,
            @RequestParam("file") final MultipartFile file,
            @RequestParam(value = "overwrite", defaultValue = "false") final boolean overwrite
    );

    @GetMapping("/{attachmentType}/status/{jobId}")
    @Operation(summary = "Check Upload Status", tags = "attachments")
    Response<JobStatusResponse> checkUploadStatus(@PathVariable("attachmentType") final String attachmentType,
                                                  @PathVariable("jobId") final String jobId);

    @PutMapping("/{attachmentType}/cancel/{jobId}")
    @Operation(summary = "Cancel Upload", tags = "attachments")
    Response<JobStatusResponse> cancelUpload(@PathVariable("attachmentType") final String attachmentType,
                                             @PathVariable("jobId") final String jobId);
}
