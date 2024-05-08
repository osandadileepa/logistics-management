package e2e.web;

import com.quincus.shipment.api.constant.AttachmentType;
import com.quincus.shipment.api.filter.ExportFilter;
import com.quincus.shipment.impl.attachment.AttachmentServiceFactory;
import com.quincus.shipment.impl.attachment.milestone.MilestoneAttachmentService;
import com.quincus.shipment.impl.attachment.packagejourneyairsegment.PackageJourneyAirSegmentAttachmentService;
import com.quincus.shipment.impl.web.AttachmentControllerImpl;
import com.quincus.shipment.impl.web.exception.ShipmentExceptionHandler;
import com.quincus.web.common.exception.CommonExceptionHandler;
import com.quincus.web.common.exception.model.QuincusError;
import com.quincus.web.common.model.Request;
import com.quincus.web.common.model.Response;
import com.quincus.web.common.web.BaseShipmentControllerWebIT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcConfigurer;

import java.util.UUID;

import static com.quincus.shipment.api.constant.ShipmentErrorCode.VALIDATION_ERROR;
import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(controllers = {AttachmentControllerImpl.class})
@ContextConfiguration(classes = {AttachmentControllerImpl.class, ShipmentExceptionHandler.class, CommonExceptionHandler.class})
class AttachmentControllerWebIT extends BaseShipmentControllerWebIT {

    private static final String ATTACHMENT_URL = "/attachments";

    @MockBean
    private AttachmentServiceFactory attachmentServiceFactory;

    @MockBean
    private PackageJourneyAirSegmentAttachmentService packageJourneyAirSegmentAttachmentService;

    @Override
    protected MockMvcConfigurer applySpringSecurity() {
        return SecurityMockMvcConfigurers.springSecurity();
    }

    void stubAttachmentService() {
        MilestoneAttachmentService atService = Mockito.mock(MilestoneAttachmentService.class);
        Mockito.when(attachmentServiceFactory.getAttachmentServiceByType(Mockito.any()))
                .thenReturn(atService);
        Mockito.when(atService.getCsvTemplate()).thenReturn("");
    }

    @ParameterizedTest
    @ValueSource(strings = {"package-journey-air-segment", "milestone", "network-lane"})
    @WithMockUser(roles = "SHIPMENT_STATUS_VIEW")
    void downloadTemplateWithValidAttachmentType(String pathParam) throws Exception {
        //GIVEN
        final String attachmentUrl = ATTACHMENT_URL + "/" + pathParam + "/download-csv-template";
        this.stubAttachmentService();
        //WHEN
        final MvcResult result = performGetRequest(attachmentUrl);
        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaax"})
    @WithMockUser(roles = "SHIPMENT_STATUS_VIEW")
    void downloadTemplateWithInvalidAttachmentType(String pathParam) throws Exception {

        final String attachmentUrl = ATTACHMENT_URL + "/" + pathParam + "/download-csv-template";

        final MvcResult result = performGetRequest(attachmentUrl);

        Response<QuincusError> response = extractErrorResponse(result);
        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(1)
                .build();
        assertCommonErrorStructure(response, expectedError);
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaax"})
    @WithMockUser(roles = "SHIPMENT_STATUS_VIEW")
    void uploadCsvWithInvalidAttachmentType(String pathParam) throws Exception {

        final String attachmentUrl = ATTACHMENT_URL + "/" + pathParam + "/upload-csv";

        final MvcResult result = performPostFileRequest(attachmentUrl, "file", "{}");

        Response<QuincusError> response = extractErrorResponse(result);
        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(1)
                .build();
        assertCommonErrorStructure(response, expectedError);
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaax"})
    @WithMockUser(roles = "SHIPMENT_STATUS_VIEW")
    void checkUploadStatusWithInvalidAttachmentType(String pathParam) throws Exception {

        final String jobId = UUID.randomUUID().toString();

        final String attachmentUrl = ATTACHMENT_URL + "/" + pathParam + "/upload-status/" + jobId;

        final MvcResult result = performGetRequest(attachmentUrl);

        Response<QuincusError> response = extractErrorResponse(result);
        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(1)
                .build();
        assertCommonErrorStructure(response, expectedError);
    }

    @Test
    @WithMockUser(roles = "SHIPMENT_STATUS_VIEW")
    void checkUploadStatusWithInvalidJobId() throws Exception {

        final AttachmentType attachmentType = AttachmentType.MILESTONE;
        final String jobId = "abc";

        final String attachmentUrl = ATTACHMENT_URL + "/" + attachmentType.name() + "/upload-status/" + jobId;

        final MvcResult result = performGetRequest(attachmentUrl);

        Response<QuincusError> response = extractErrorResponse(result);
        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(1)
                .build();
        assertCommonErrorStructure(response, expectedError);
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaax"})
    @WithMockUser(roles = "SHIPMENT_STATUS_VIEW")
    void cancelUploadWithInvalidAttachmentType(String pathParam) throws Exception {

        final String jobId = UUID.randomUUID().toString();

        final String attachmentUrl = ATTACHMENT_URL + "/" + pathParam + "/cancel-upload/" + jobId;

        final MvcResult result = performPutRequest(attachmentUrl, "");

        Response<QuincusError> response = extractErrorResponse(result);
        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(1)
                .build();
        assertCommonErrorStructure(response, expectedError);
    }

    @Test
    @WithMockUser(roles = "SHIPMENT_STATUS_VIEW")
    void cancelUploadWithInvalidJobId() throws Exception {

        final AttachmentType attachmentType = AttachmentType.MILESTONE;
        final String jobId = "abc";

        final String attachmentUrl = ATTACHMENT_URL + "/" + attachmentType.name() + "/cancel-upload/" + jobId;

        final MvcResult result = performPutRequest(attachmentUrl, "");

        Response<QuincusError> response = extractErrorResponse(result);
        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(1)
                .build();
        assertCommonErrorStructure(response, expectedError);
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaax"})
    @WithMockUser(roles = "SHIPMENT_STATUS_VIEW")
    void exportWithInvalidAttachmentType_ShouldFailWithBadRequest(String pathParam) throws Exception {

        final String attachmentUrl = ATTACHMENT_URL + "/" + pathParam + "/export/pre-populated";
        final Request<ExportFilter> request = new Request<>();
        final MvcResult result = performPostRequest(attachmentUrl, request);

        Response<QuincusError> response = extractErrorResponse(result);
        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(1)
                .build();
        assertCommonErrorStructure(response, expectedError);
    }

    @ParameterizedTest
    @ValueSource(strings = {"milestone"})
    @WithMockUser(roles = "SHIPMENT_STATUS_VIEW")
    void exportWithInvalidExportFilter_ShouldFailWithBadRequest(String pathParam) throws Exception {
        MilestoneAttachmentService atService = Mockito.mock(MilestoneAttachmentService.class);
        Mockito.when(attachmentServiceFactory.getAttachmentServiceByType(Mockito.any()))
                .thenReturn(atService);

        final String attachmentUrl = ATTACHMENT_URL + "/" + pathParam + "/export/pre-populated";

        final Request<ExportFilter> request = new Request<>();
        ExportFilter filter = new ExportFilter();
        final String[] keys = new String[2];
        keys[0] = "EX-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5";
        filter.setKeys(keys);
        request.setData(filter);
        final MvcResult result = performPostRequest(attachmentUrl, request);

        Response<QuincusError> response = extractErrorResponse(result);
        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(1)
                .build();
        assertCommonErrorStructure(response, expectedError);
    }
}
