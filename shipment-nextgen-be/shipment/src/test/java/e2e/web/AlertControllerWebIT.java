package e2e.web;

import com.quincus.shipment.api.AlertApi;
import com.quincus.shipment.impl.web.AlertControllerImpl;
import com.quincus.shipment.impl.web.exception.ShipmentExceptionHandler;
import com.quincus.web.common.exception.model.QuincusError;
import com.quincus.web.common.model.Response;
import com.quincus.web.common.web.BaseShipmentControllerWebIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcConfigurer;

import java.util.List;
import java.util.UUID;

import static com.quincus.shipment.api.constant.ShipmentErrorCode.INVALID_FORMAT;
import static com.quincus.shipment.api.constant.ShipmentErrorCode.VALIDATION_ERROR;

@WebMvcTest(controllers = {AlertControllerImpl.class})
@ContextConfiguration(classes = {AlertControllerImpl.class, ShipmentExceptionHandler.class})
class AlertControllerWebIT extends BaseShipmentControllerWebIT {

    private static final String ALERT_URL = "/alerts";
    @MockBean
    private AlertApi alertApi;

    @Override
    protected MockMvcConfigurer applySpringSecurity() {
        return SecurityMockMvcConfigurers.springSecurity();
    }

    @Test
    @WithMockUser(roles = "SHIPMENTS_EDIT")
    @DisplayName("Given a user with SHIPMENTS_EDIT role, when a valid alert ID is provided, the response should be NO CONTENT.")
    void shouldReturnNoContentForValidAlertIdWithShipmentsEditRole() throws Exception {
        //GIVEN
        UUID alertId = UUID.randomUUID();
        //WHEN
        final String apiUrl = ALERT_URL + "/" + alertId + "?dismissed=" + true;
        final MvcResult result = performPatchRequest(apiUrl);
        //THEN
        assertThatHttpStatusIsExpected(result, HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("Given a user without any roles, when a alert request is made with a valid ID, an unauthorized response should be returned.")
    void shouldReturnUnauthorizedForValidAlertIdWithoutRole() throws Exception {
        //GIVEN
        UUID alertId = UUID.randomUUID();
        //WHEN
        final String apiUrl = ALERT_URL + "/" + alertId + "?dismissed=" + true;
        final MvcResult result = performPatchRequest(apiUrl);
        //THEN
        assertThatHttpStatusIsExpected(result, HttpStatus.UNAUTHORIZED);
    }

    @Test
    @WithMockUser(roles = "SHIPMENTS_EDIT")
    @DisplayName("Given a user with SHIPMENTS_EDIT role and an invalid alert ID, a validation error response should be returned.")
    void shouldReturnValidationErrorForInvalidAlertIdWithShipmentsEditRole() throws Exception {
        //GIVEN
        String alertId = "INVALID ID";

        //WHEN
        final String apiUrl = ALERT_URL + "/" + alertId + "?dismissed=" + true;
        final MvcResult result = performPatchRequest(apiUrl);

        //THEN
        Response<QuincusError> response = extractErrorResponse(result);

        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(1)
                .build();

        List<FieldError> expectedFieldErrors = buildErrorList(new FieldError("dismiss_alert.alert_id", "must be a valid UUIDv4 format"));

        assertCommonErrorStructure(response, expectedError);
        assertFieldErrors(response.getData().fieldErrors(), expectedFieldErrors);
    }

    @Test
    @WithMockUser(roles = "SHIPMENTS_EDIT")
    @DisplayName("Given a user with SHIPMENTS_EDIT role and an invalid dismissed value, an invalid format error response should be returned.")
    void shouldReturnInvalidFormatErrorForInvalidDismissedValueWithShipmentsEditRole() throws Exception {
        //GIVEN
        String alertId = UUID.randomUUID().toString();
        String invalidDismissedValue = "INVALID_BOOLEAN";

        //WHEN
        final String apiUrl = ALERT_URL + "/" + alertId + "?dismissed=" + invalidDismissedValue;
        final MvcResult result = performPatchRequest(apiUrl);

        //THEN
        Response<QuincusError> response = extractErrorResponse(result);

        ExpectedError expectedError = ExpectedError.builder()
                .message("There is an invalid format in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(INVALID_FORMAT.name())
                .fieldErrorSize(1)
                .build();

        List<FieldError> expectedFieldErrors = buildErrorList(new FieldError("dismissed", "Failed to convert `dismissed` to `boolean`"));

        assertCommonErrorStructure(response, expectedError);
        assertFieldErrors(response.getData().fieldErrors(), expectedFieldErrors);
    }
}
