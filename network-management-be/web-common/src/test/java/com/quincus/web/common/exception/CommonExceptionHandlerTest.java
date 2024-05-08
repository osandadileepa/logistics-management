package com.quincus.web.common.exception;

import com.quincus.web.common.exception.model.ApiCallException;
import com.quincus.web.common.exception.model.ObjectNotFoundException;
import com.quincus.web.common.exception.model.OperationNotAllowedException;
import com.quincus.web.common.model.Response;
import org.hibernate.exception.JDBCConnectionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class CommonExceptionHandlerTest {

    private final CommonExceptionHandler exceptionHandler = new CommonExceptionHandler();

    @Test
    void validationError_argumentMethodArgumentNotValidException_shouldReturnBadRequest() {
        final Method method = new Object() {}.getClass().getEnclosingMethod();
        MethodParameter parameter = new MethodParameter(method, -1, 0);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("order", "id", "test-only.");
        given(bindingResult.getFieldErrors()).willReturn(List.of(fieldError));

        ResponseEntity<Response<String>> response = exceptionHandler
                .handleMethodArgumentNotValidException(new MethodArgumentNotValidException(parameter, bindingResult));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull().isInstanceOfSatisfying(Response.class, res -> {
            assertThat(res.getErrors().get(0))
                    .withFailMessage("Error message mismatch.")
                    .isEqualTo("id test-only.");
        });
    }

    @Test
    void apiCallError_apiCallResponseFailure_shouldReturnApiCallResponseCode() {
        String url = "http://example.com/object";
        HttpEntity requestDummy = mock(HttpEntity.class);
        String contentDummy = "Test Only.";
        Response<List<String>> responseBody = new Response<>(List.of(contentDummy));
        responseBody.setMessage("Error. Test Only.");
        ResponseEntity<Response<List<String>>> responseDummy = new ResponseEntity<>(responseBody, HttpStatus.BAD_GATEWAY);
        ApiCallException apiCallException = new ApiCallException(url, requestDummy, responseDummy);

        ResponseEntity<Response<String>> response = exceptionHandler.handleApiCallException(apiCallException);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains(url, contentDummy);
    }

    @Test
    void apiCallError_exceptionOccurredOnApiCall_shouldReturnApiCallResponseCode() {
        String url = "http://example.com/object";
        HttpEntity request = mock(HttpEntity.class);
        String errorMessage = "This is a test Only.";
        RestClientException exception = new RestClientException(errorMessage);
        ApiCallException apiCallException = new ApiCallException(url, request, errorMessage, exception);

        ResponseEntity<Response<String>> response = exceptionHandler.handleApiCallException(apiCallException);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains(url, errorMessage);

    }

    @Test
    void objectNotFoundError_objectNotFound_shouldReturnNotFound() {
        String type = "file";
        String fileName = "pic.jpg";
        ObjectNotFoundException exception = new ObjectNotFoundException(type, fileName);

        ResponseEntity<Response<Object>> response = exceptionHandler.handleObjectNotFoundException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains(type, fileName);
    }

    @Test
    void unsupportedMediaTypeError_unsupportedMediaType_shouldReturnUnsupportedMediaType() {
        String errMsg = "Unsupported Media Type: plain/text";
        UnsupportedMediaTypeStatusException exception = new UnsupportedMediaTypeStatusException(errMsg);

        ResponseEntity<Response<String>> response = exceptionHandler.handleUnsupportedMediaTypeStatusException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains(errMsg);
    }

    @Test
    void operationNotAllowedError_notAllowed_shouldReturnNotAcceptable() {
        String errMsg = "Not Allowed in directory `secret`";
        OperationNotAllowedException exception = new OperationNotAllowedException(errMsg);

        ResponseEntity<Response<String>> response = exceptionHandler.handleOperationNotAllowedException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_ACCEPTABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains(errMsg);
    }

    @Test
    void generic_argumentIoException_shouldReturnBadRequest() {
        IOException ioException = new IOException("Test only.");

        ResponseEntity<Response<String>> response = exceptionHandler.handleGenericException(ioException);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("Test only.");
    }

    @Test
    void accessDeniedExceptionHandler_accessDeniedException_shouldReturnUnauthorized() {
        AccessDeniedException exception = new AccessDeniedException("Test Only.");

        ResponseEntity<Response<String>> response = exceptionHandler.handleAccessDeniedException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void handleDataAccessException_shouldReturnServiceUnavailable() {
        DataAccessException dataAccessException = new DataAccessException("Test Only.") {};

        ResponseEntity<Response<String>> response = exceptionHandler.handleDataAccessException(dataAccessException);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("We're experiencing issues processing your request due to a system constraint. Please try again later.");
    }

    @Test
    void handleJDBCConnectionException_shouldReturnServiceUnavailable() {
        JDBCConnectionException jdbcConnectionException = new JDBCConnectionException("Test Only.", new SQLException());

        ResponseEntity<Response<String>> response = exceptionHandler.handleJDBCConnectionException(jdbcConnectionException);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("We're experiencing issues processing your request due to a system constraint. Please try again later.");
    }

}
