package e2e.web;

import com.quincus.shipment.api.FilterApi;
import com.quincus.shipment.api.constant.LocationType;
import com.quincus.shipment.impl.web.LocationControllerImpl;
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

import static com.quincus.shipment.api.constant.ShipmentErrorCode.INVALID_FORMAT;
import static com.quincus.shipment.api.constant.ShipmentErrorCode.VALIDATION_ERROR;
import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(controllers = {LocationControllerImpl.class})
@ContextConfiguration(classes = {LocationControllerImpl.class, ShipmentExceptionHandler.class})
class LocationControllerWebIT extends BaseShipmentControllerWebIT {
    private static final String LOCATIONS_URL = "/filter/locations";

    @MockBean
    private FilterApi filterApi;

    @Override
    protected MockMvcConfigurer applySpringSecurity() {
        return SecurityMockMvcConfigurers.springSecurity();
    }

    @Test
    @WithMockUser
    @DisplayName("When type = COUNTRY, page=1 and per_page=1, response should be OK")
    void shouldReturnOKForValidTypePageAndPerPageValue() throws Exception {
        //GIVEN
        LocationType type = LocationType.COUNTRY;
        int page = 1;
        int per_page = 1;
        //WHEN
        final String apiUrl = LOCATIONS_URL + "?type=" + type + "&page=" + page + "&per_page=" + per_page;
        final MvcResult result = performGetRequest(apiUrl);
        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @WithMockUser
    @DisplayName("When type is invalid, page=1 and per_page=1, response should be error")
    void shouldReturnInvalidFormatForInvalidTypeValue() throws Exception {
        //GIVEN
        String type = "INVALID";
        int page = 1;
        int per_page = 1;
        //WHEN
        final String apiUrl = LOCATIONS_URL + "?type=" + type + "&page=" + page + "&per_page=" + per_page;
        final MvcResult result = performGetRequest(apiUrl);
        //THEN
        Response<QuincusError> response = extractErrorResponse(result);
        ExpectedError expectedError = ExpectedError.builder()
                .message("There is an invalid format in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(INVALID_FORMAT.name())
                .fieldErrorSize(1)
                .build();
        List<FieldError> expectedFieldErrors = buildErrorList(
                new FieldError("type", "Failed to convert `type` to `com.quincus.shipment.api.constant.LocationType`")
        );
        assertCommonErrorStructure(response, expectedError);
        assertFieldErrors(response.getData().fieldErrors(), expectedFieldErrors);
    }

    @Test
    @WithMockUser
    @DisplayName("When page=0 and per_page=0, response should be error")
    void shouldReturnValidationErrorForPageAndPerPageBelowMinValue() throws Exception {
        //GIVEN
        LocationType type = LocationType.STATE;
        int page = 0;
        int per_page = 0;
        //WHEN
        final String apiUrl = LOCATIONS_URL + "?type=" + type + "&page=" + page + "&per_page=" + per_page;
        final MvcResult result = performGetRequest(apiUrl);
        //THEN
        Response<QuincusError> response = extractErrorResponse(result);
        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(2)
                .build();
        List<FieldError> expectedFieldErrors = buildErrorList(
                new FieldError("find_locations.per_page", "must be greater than or equal to 1"),
                new FieldError("find_locations.page", "must be greater than or equal to 1")
        );
        assertCommonErrorStructure(response, expectedError);
        assertFieldErrors(response.getData().fieldErrors(), expectedFieldErrors);
    }

    @Test
    @WithMockUser
    @DisplayName("When page=1 and per_page=101, response should be error")
    void shouldReturnValidationErrorForPerPageAboveMaxValue() throws Exception {
        //GIVEN
        LocationType type = LocationType.CITY;
        int page = 1;
        int per_page = 101;
        //WHEN
        final String apiUrl = LOCATIONS_URL + "?type=" + type + "&page=" + page + "&per_page=" + per_page;
        final MvcResult result = performGetRequest(apiUrl);
        //THEN
        Response<QuincusError> response = extractErrorResponse(result);
        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(1)
                .build();
        List<FieldError> expectedFieldErrors = buildErrorList(
                new FieldError("find_locations.per_page", "must be less than or equal to 100")
        );
        assertCommonErrorStructure(response, expectedError);
        assertFieldErrors(response.getData().fieldErrors(), expectedFieldErrors);
    }

    @Test
    @WithMockUser
    @DisplayName("When page=1, per_page=1 and key has more than 100 characters, response should be error")
    void shouldReturnValidationErrorForKeyAboveMaxValue() throws Exception {
        //GIVEN
        LocationType type = LocationType.FACILITY;
        int page = 1;
        int per_page = 1;
        String key = "ThisIsMoreThan100Characters-ThisIsMoreThan100Characters-ThisIsMoreThan100Characters-ThisIsMoreThan100Characters";

        //WHEN
        final String apiUrl = LOCATIONS_URL + "?type=" + type + "&page=" + page + "&per_page=" + per_page + "&key=" + key;
        final MvcResult result = performGetRequest(apiUrl);
        //THEN
        Response<QuincusError> response = extractErrorResponse(result);
        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(1)
                .build();
        List<FieldError> expectedFieldErrors = buildErrorList(
                new FieldError("find_locations.key", "size must be between 0 and 100")
        );
        assertCommonErrorStructure(response, expectedError);
        assertFieldErrors(response.getData().fieldErrors(), expectedFieldErrors);
    }
}
