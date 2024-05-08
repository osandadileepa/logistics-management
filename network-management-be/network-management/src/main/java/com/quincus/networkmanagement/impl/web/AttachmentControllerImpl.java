package com.quincus.networkmanagement.impl.web;

import com.quincus.networkmanagement.AttachmentController;
import com.quincus.networkmanagement.api.constant.AttachmentType;
import com.quincus.networkmanagement.api.dto.JobStatusResponse;
import com.quincus.networkmanagement.api.validator.FileTypeValidator;
import com.quincus.networkmanagement.impl.attachment.AbstractAttachmentService;
import com.quincus.networkmanagement.impl.attachment.AttachmentServiceFactory;
import com.quincus.web.common.model.Response;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@AllArgsConstructor
public class AttachmentControllerImpl implements AttachmentController {
    private final AttachmentServiceFactory attachmentServiceFactory;

    @Override
    public ResponseEntity<Resource> downloadTemplateFile(String attachmentType) {
        AbstractAttachmentService<?> attachmentService = attachmentServiceFactory
                .getAttachmentServiceByType(AttachmentType.fromValue(attachmentType));
        String templatePath = attachmentService.getUploadFileTemplate();
        Resource resource = new ClassPathResource(templatePath);
        String filename = resource.getFilename();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .body(resource);
    }

    @Override
    public Response<JobStatusResponse> upload(String attachmentType, MultipartFile file, boolean overwrite) {
        FileTypeValidator.validate(file);
        AbstractAttachmentService<?> attachmentService = attachmentServiceFactory
                .getAttachmentServiceByType(AttachmentType.fromValue(attachmentType));
        return new Response<>(new JobStatusResponse(attachmentService.uploadFile(file, overwrite)));
    }

    @Override
    public Response<JobStatusResponse> checkUploadStatus(String attachmentType, String jobId) {
        AbstractAttachmentService<?> attachmentService = attachmentServiceFactory
                .getAttachmentServiceByType(AttachmentType.fromValue(attachmentType));
        return new Response<>(attachmentService.getJobMetrics(jobId));
    }

    @Override
    public Response<JobStatusResponse> cancelUpload(String attachmentType, String jobId) {
        AbstractAttachmentService<?> attachmentService = attachmentServiceFactory
                .getAttachmentServiceByType(AttachmentType.fromValue(attachmentType));
        return new Response<>(attachmentService.cancelUpload(jobId));
    }
}
