package com.quincus.s3.web;

import com.quincus.s3.api.FileApi;
import com.quincus.web.common.exception.model.QuincusError;
import com.quincus.web.common.model.Response;
import com.quincus.web.common.web.BaseShipmentControllerWebIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(controllers = {FileControllerImpl.class})
@ContextConfiguration(classes = {FileControllerImpl.class})
class FileControllerWebIT extends BaseShipmentControllerWebIT {

    private static final String FILES_URL = "/files";
    private final String longDirectoryName = new String(new char[3000]).replace('\0', 'a');
    @MockBean
    private FileApi fileApi;

    @Test
    @DisplayName("Given a valid filename, when the file is requested to be deleted, the response should be NO CONTENT.")
    void shouldReturnNoContentWhenValidFileNameIsProvided() throws Exception {
        //GIVEN
        String fileName = "validFileName.jpg";
        //WHEN
        final String apiUrl = FILES_URL + "?file_name=" + fileName + "&directory=shipment_attachments";
        final MvcResult result = performDeleteRequest(apiUrl);
        //THEN
        assertThatHttpStatusIsExpected(result, HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("Given an invalid filename without an extension and invalid directory, when the file is requested to be deleted, the response should be BAD REQUEST.")
    void shouldReturnBadRequestWhenFileNameAndDirectoryAreInvalid() throws Exception {
        //GIVEN
        String fileName = "invalidFileNameWithoutExtension  ";
        //WHEN
        final String apiUrl = FILES_URL + "?file_name=" + fileName + "&directory=" + longDirectoryName;
        final MvcResult result = performDeleteRequest(apiUrl);
        //THEN
        Response<QuincusError> response = extractErrorResponse(result);

        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode("VALIDATION_ERROR")
                .fieldErrorSize(2)
                .build();

        List<FieldError> expectedFieldErrors = buildErrorList(
                new FieldError("deleteFileFromBucket.fileName", "Invalid file name format. It must have an extension."),
                new FieldError("deleteFileFromBucket.directory", "size must be between 1 and 256"));

        assertCommonErrorStructure(response, expectedError);
        assertFieldErrors(response.getData().fieldErrors(), expectedFieldErrors);
    }

    @Test
    @DisplayName("Given a valid file name and directory, when a read pre-signed link is requested, the response should be OK.")
    void shouldReturnOkWhenValidFileNameAndDirectoryProvided() throws Exception {
        //GIVEN
        String fileName = "validFileName.jpg";
        String directoryName = "cost_shipments";
        //WHEN
        final String apiUrl = FILES_URL + "/read-pre-signed-link?file_name=" + fileName + "&directory=" + directoryName;
        final MvcResult result = performGetRequest(apiUrl);
        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Given invalid file names and a long directory name, when a read pre-signed link is requested, the response should indicate validation errors.")
    void shouldIndicateValidationErrorsForInvalidFileNamesAndLongDirectoryName() throws Exception {
        //GIVEN
        String fileName = "invalid file name 1,invalid file name 2";
        //WHEN
        final String apiUrl = FILES_URL + "/read-pre-signed-link?file_name=" + fileName + "&directory=" + longDirectoryName;
        final MvcResult result = performGetRequest(apiUrl);
        //THEN
        Response<QuincusError> response = extractErrorResponse(result);

        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode("VALIDATION_ERROR")
                .fieldErrorSize(2)
                .build();

        List<FieldError> expectedFieldErrors = buildErrorList(
                new FieldError("getReadFilePreSignedUrl.directory", "size must be between 1 and 256"),
                new FieldError("getReadFilePreSignedUrl.fileNames", "Invalid file name format. It must have an extension."));

        assertCommonErrorStructure(response, expectedError);
        assertFieldErrors(response.getData().fieldErrors(), expectedFieldErrors);
    }

    @Test
    @DisplayName("Given a valid file name and directory, when an upload pre-signed link is requested, the response should be OK.")
    void shouldReturnOkWhenUploadLinkRequestedForValidFileNameAndDirectory() throws Exception {
        //GIVEN
        String fileName = "validFileName.jpg";
        String directoryName = "cost_shipments";
        //WHEN
        final String apiUrl = FILES_URL + "/upload-pre-signed-link?file_name=" + fileName + "&directory=" + directoryName;
        final MvcResult result = performGetRequest(apiUrl);
        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Given invalid file names and a long directory name, when an upload pre-signed link is requested, the response should indicate validation errors.")
    void shouldIndicateValidationErrorsWhenUploadLinkRequestedForInvalidFileNamesAndLongDirectoryName() throws Exception {
        //GIVEN
        String fileName = "invalid file name 1,invalid file name 2";
        //WHEN
        final String apiUrl = FILES_URL + "/upload-pre-signed-link?file_name=" + fileName + "&directory=" + longDirectoryName;
        final MvcResult result = performGetRequest(apiUrl);
        //THEN
        Response<QuincusError> response = extractErrorResponse(result);

        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode("VALIDATION_ERROR")
                .fieldErrorSize(2)
                .build();

        List<FieldError> expectedFieldErrors = buildErrorList(
                new FieldError("getUploadFilePreSignedUrl.directory", "size must be between 1 and 256"),
                new FieldError("getUploadFilePreSignedUrl.fileNames", "Invalid file name format. It must have an extension."));

        assertCommonErrorStructure(response, expectedError);
        assertFieldErrors(response.getData().fieldErrors(), expectedFieldErrors);
    }

}
