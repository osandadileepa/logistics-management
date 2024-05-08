package com.quincus.web.common.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.quincus.web.common.exception.model.QuincusError;
import com.quincus.web.common.exception.model.QuincusFieldError;
import com.quincus.web.common.model.Response;
import lombok.Builder;
import lombok.Getter;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BaseShipmentControllerWebIT extends BaseControllerWebIT {

    protected Response<QuincusError> extractErrorResponse(MvcResult result) throws IOException {
        if (StringUtils.isBlank(result.getResponse().getContentAsString())) {
            return new Response<>();
        }
        return objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});
    }

    protected void assertCommonErrorStructure(Response<QuincusError> response, ExpectedError expectedError) {
        assertThat(response.getStatus()).isEqualTo(String.valueOf(expectedError.getStatus().value())); // NOSONAR
        assertThat(response.getMessage()).isEqualTo(expectedError.getMessage()); // NOSONAR
        validateErrorData(response.getData(), expectedError);
    }

    private void validateErrorData(QuincusError errorData, ExpectedError expectedError) {
        assertThat(errorData.message()).isEqualTo(expectedError.getMessage()); // NOSONAR
        assertThat(errorData.code()).hasToString(expectedError.getErrorCode()); // NOSONAR
        assertThat(errorData.fieldErrors()).hasSize(expectedError.getFieldErrorSize()); // NOSONAR
    }

    protected void assertFieldErrors(List<QuincusFieldError> fieldErrors, List<FieldError> expectedFieldErrors) {
        List<FieldError> actualFieldErrors = fieldErrors.stream()
                .map(error -> new FieldError(error.getField(), error.getMessage()))
                .toList();
        assertThat(actualFieldErrors).containsAll(expectedFieldErrors); // NOSONAR
    }

    protected static List<FieldError> buildErrorList(FieldError... fieldErrors) {
        return Arrays.asList(fieldErrors);
    }

    public record FieldError(String field, String errorMessage) {}

    @Getter
    @Builder
    public static class ExpectedError {
        private final String message;
        private final HttpStatus status;
        private final String errorCode;
        private final int fieldErrorSize;
    }

}
