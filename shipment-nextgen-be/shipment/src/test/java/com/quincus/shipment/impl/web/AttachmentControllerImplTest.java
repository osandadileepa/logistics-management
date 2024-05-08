package com.quincus.shipment.impl.web;

import com.quincus.shipment.api.constant.AttachmentType;
import com.quincus.shipment.api.constant.JobState;
import com.quincus.shipment.api.dto.JobStatusResponse;
import com.quincus.shipment.impl.attachment.AbstractAttachmentService;
import com.quincus.shipment.impl.attachment.AttachmentServiceFactory;
import com.quincus.web.common.exception.model.QuincusValidationException;
import com.quincus.web.common.model.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttachmentControllerImplTest {
    @InjectMocks
    private AttachmentControllerImpl attachmentController;
    @Mock
    private AttachmentServiceFactory attachmentServiceFactory;

    @Test
    void givenAttachmentTypeWhenDownloadCsvTemplateThenVerifyCorrectAttachmentServiceInvokeGetCsvFile() {
        // Mock AttachmentService and AttachmentType
        String attachmentType = "network-lane";
        AbstractAttachmentService<?> attachmentService = mock(AbstractAttachmentService.class);
        when(attachmentService.getCsvTemplate()).thenReturn("network-lane-template.csv");
        when(attachmentServiceFactory.getAttachmentServiceByType(AttachmentType.fromValue(attachmentType)))
                .thenReturn(attachmentService);

        // Invoke the method
        ResponseEntity<Resource> response = attachmentController.downloadCsvTemplate(attachmentType);

        // Verify the response
        String mediaType = "text/csv";
        MediaType expectedMediaType = MediaType.parseMediaType(mediaType);
        Resource expectedBody = new ClassPathResource("network-lane-template.csv");

        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=network-lane-template.csv");
        expectedHeaders.add(HttpHeaders.CONTENT_TYPE,mediaType);

        assertThat(response.getHeaders()).isEqualTo(expectedHeaders);
        assertThat(response.getHeaders().getContentType()).isEqualTo(expectedMediaType);
        assertThat(response.getBody()).isEqualTo(expectedBody);

        // Verify interactions
        verify(attachmentServiceFactory).getAttachmentServiceByType(AttachmentType.fromValue(attachmentType));
        verify(attachmentService, times(2)).getCsvTemplate();
    }

    @Test
    void givenCsvMultipartWhenUploadCsvThenShouldReturnJobIdInResponse() {
        // Mock AttachmentService and AttachmentType
        String attachmentType = "network-lane";
        AbstractAttachmentService<?> attachmentService = mock(AbstractAttachmentService.class);
        when(attachmentService.uploadCsv(any())).thenReturn("job123");
        when(attachmentServiceFactory.getAttachmentServiceByType(AttachmentType.fromValue(attachmentType)))
                .thenReturn(attachmentService);

        // Create a mock MultipartFile
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", "test,csv".getBytes());

        // Invoke the method
        Response<JobStatusResponse> response = attachmentController.uploadCsv(attachmentType, file);

        // Verify the response
        assertThat(response.getData().getJobId()).isEqualTo("job123");

        // Verify interactions
        verify(attachmentServiceFactory).getAttachmentServiceByType(AttachmentType.fromValue(attachmentType));
        verify(attachmentService).uploadCsv(file);
    }

    @Test
    void givenNonCsvMultipartWhenUploadCsvThenThrowError() {
        //GIVEN:
        MockMultipartFile file = new MockMultipartFile("file", "test.text", "text", "test,csv".getBytes());

        //WHEN: THEN:
        assertThatThrownBy(() -> attachmentController.uploadCsv("network-lane", file)).isInstanceOf(QuincusValidationException.class)
                .hasMessage("Invalid file or file format. Only CSV files are allowed.");
    }

    @Test
    void givenAttachmentTypeAndJobIdWhenCheckUploadStatusThenShouldReturnJobStatus() {
        // Mock AttachmentService and AttachmentType
        String attachmentType = "network-lane";
        String jobId = "job123";
        AbstractAttachmentService<?> attachmentService = mock(AbstractAttachmentService.class);
        JobStatusResponse expectedJobStatus = new JobStatusResponse(jobId);
        expectedJobStatus.setStatus(JobState.IN_PROGRESS);
        expectedJobStatus.setJobId(jobId);
        expectedJobStatus.setTotalRecords(15L);
        expectedJobStatus.setFailedRecords(1L);
        expectedJobStatus.setProcessedRecords(11L);
        when(attachmentService.checkUploadStatus(jobId)).thenReturn(expectedJobStatus);
        when(attachmentServiceFactory.getAttachmentServiceByType(AttachmentType.fromValue(attachmentType)))
                .thenReturn(attachmentService);

        // Invoke the method
        Response<JobStatusResponse> response = attachmentController.checkUploadStatus(attachmentType, jobId);

        // Verify the response
        assertThat(response.getData().getStatus()).isEqualTo(expectedJobStatus.getStatus());
        assertThat(response.getData().getJobId()).isEqualTo(expectedJobStatus.getJobId());
        assertThat(response.getData().getProcessedRecords()).isEqualTo(expectedJobStatus.getProcessedRecords());
        assertThat(response.getData().getSuccessfulRecords()).isEqualTo(expectedJobStatus.getSuccessfulRecords());
        assertThat(response.getData().getFailedRecords()).isEqualTo(expectedJobStatus.getFailedRecords());

        // Verify interactions
        verify(attachmentServiceFactory).getAttachmentServiceByType(AttachmentType.fromValue(attachmentType));
        verify(attachmentService).checkUploadStatus(jobId);
    }

    @Test
    void givenAttachmentTypeAndJobIdWhenCancelUploadThenShouldReturnJobId() {
        // Mock AttachmentService and AttachmentType
        String attachmentType = "network-lane";
        String jobId = "job123";
        AbstractAttachmentService<?> attachmentService = mock(AbstractAttachmentService.class);
        JobStatusResponse expectedJobStatus = new JobStatusResponse(jobId);
        when(attachmentService.cancelUpload(jobId)).thenReturn(expectedJobStatus);
        when(attachmentServiceFactory.getAttachmentServiceByType(AttachmentType.fromValue(attachmentType)))
                .thenReturn(attachmentService);

        // Invoke the method
        Response<JobStatusResponse> response = attachmentController.cancelUpload(attachmentType, jobId);

        // Verify the response
        assertThat(response.getData().getJobId()).isEqualTo(expectedJobStatus.getJobId());

        // Verify interactions
        verify(attachmentServiceFactory).getAttachmentServiceByType(AttachmentType.fromValue(attachmentType));
        verify(attachmentService).cancelUpload(jobId);
    }
}
