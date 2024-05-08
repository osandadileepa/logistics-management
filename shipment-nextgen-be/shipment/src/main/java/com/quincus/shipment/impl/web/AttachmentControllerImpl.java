package com.quincus.shipment.impl.web;

import com.quincus.shipment.AttachmentController;
import com.quincus.shipment.api.constant.AttachmentType;
import com.quincus.shipment.api.dto.JobStatusResponse;
import com.quincus.shipment.api.filter.ExportFilter;
import com.quincus.shipment.impl.aspect.AttachmentDownloadCsvTemplatePermission;
import com.quincus.shipment.impl.attachment.AbstractAttachmentService;
import com.quincus.shipment.impl.attachment.AttachmentServiceFactory;
import com.quincus.shipment.impl.attachment.ExportableAttachmentService;
import com.quincus.shipment.impl.helper.CsvValidator;
import com.quincus.web.common.exception.model.QuincusException;
import com.quincus.web.common.model.Request;
import com.quincus.web.common.model.Response;
import com.quincus.web.common.utility.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.time.Clock;
import java.time.LocalDateTime;

@AllArgsConstructor
@RestController
@Slf4j
public class AttachmentControllerImpl implements AttachmentController {

    private final AttachmentServiceFactory attachmentServiceFactory;

    @Override
    @AttachmentDownloadCsvTemplatePermission
    @LogExecutionTime
    public ResponseEntity<Resource> downloadCsvTemplate(final String attachmentType) {
        AbstractAttachmentService<?> attachmentService = attachmentServiceFactory.getAttachmentServiceByType(AttachmentType.fromValue(attachmentType));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + attachmentService.getCsvTemplate());
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new ClassPathResource(attachmentService.getCsvTemplate()));
    }

    @Override
    @LogExecutionTime
    public Response<JobStatusResponse> uploadCsv(final String attachmentType, final MultipartFile file) {
        CsvValidator.validate(file);
        AbstractAttachmentService<?> attachmentService = attachmentServiceFactory.getAttachmentServiceByType(AttachmentType.fromValue(attachmentType));
        return new Response<>(new JobStatusResponse(attachmentService.uploadCsv(file)));
    }

    @Override
    @LogExecutionTime
    public Response<JobStatusResponse> checkUploadStatus(final String attachmentType, final String jobId) {
        AbstractAttachmentService<?> attachmentService = attachmentServiceFactory.getAttachmentServiceByType(AttachmentType.fromValue(attachmentType));
        return new Response<>(attachmentService.checkUploadStatus(jobId));
    }

    @Override
    @LogExecutionTime
    public Response<JobStatusResponse> cancelUpload(final String attachmentType, final String jobId) {
        AbstractAttachmentService<?> attachmentService = attachmentServiceFactory.getAttachmentServiceByType(AttachmentType.fromValue(attachmentType));
        return new Response<>(attachmentService.cancelUpload(jobId));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SHIPMENTS_EXPORT','SHIPMENTS_VIEW')")
    @LogExecutionTime
    public void export(final String attachmentType, final Request<ExportFilter> request, final HttpServletResponse servletResponse) {
        ExportableAttachmentService attachmentService = attachmentServiceFactory.getExportableAttachmentServiceByType(AttachmentType.fromValue(attachmentType));
        String filename = attachmentType + "-export-" + LocalDateTime.now(Clock.systemUTC());
        servletResponse.setContentType("text/csv");
        servletResponse.addHeader("Content-Disposition", "attachment; filename=\"" + filename + ".csv\"");
        try {
            attachmentService.export(request.getData(), servletResponse.getWriter());
        } catch (Exception e) {
            log.error("Error while exporting shipments to CSV", e);
            throw new QuincusException("Failed to export shipments to CSV", e);
        }
    }

}
