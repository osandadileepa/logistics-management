package e2e.web;

import com.quincus.qportal.api.QPortalApi;
import com.quincus.shipment.impl.service.QPortalService;
import com.quincus.shipment.impl.web.QPortalControllerImpl;
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
import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(controllers = {QPortalControllerImpl.class})
@ContextConfiguration(classes = {QPortalControllerImpl.class, ShipmentExceptionHandler.class})
class QPortalControllerWebIT extends BaseShipmentControllerWebIT {
    private static final String QPORTAL_URL = "/qportal";

    @MockBean
    private QPortalApi qPortalApi;
    @MockBean
    private QPortalService qPortalService;

    @Override
    protected MockMvcConfigurer applySpringSecurity() {
        return SecurityMockMvcConfigurers.springSecurity();
    }

    @Test
    @WithMockUser(roles = "SHIPMENTS_VIEW")
    @DisplayName("For a given valid partner id, response should be OK.")
    void returnOkForValidPartnerId() throws Exception {
        //GIVEN
        UUID partnerId = UUID.randomUUID();

        //WHEN
        final String apiUrl = QPORTAL_URL + "/partners/" + partnerId;
        final MvcResult result = performGetRequest(apiUrl);

        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Given a user without any roles, when a partner get request is made with a valid ID, an unauthorized response should be returned.")
    void shouldReturnUnauthorizedForValidPartnerIdWithoutRole() throws Exception {
        //GIVEN
        UUID partnerId = UUID.randomUUID();

        //WHEN
        final String apiUrl = QPORTAL_URL + "/partners/" + partnerId;
        final MvcResult result = performGetRequest(apiUrl);

        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @WithMockUser(roles = "SHIPMENTS_VIEW")
    @DisplayName("Given a user with SHIPMENTS_VIEW role, when an invalid Partner ID format is provided, a validation error should be returned.")
    void shouldReturnValidationErrorForInvalidPartnerIdWithViewRole() throws Exception {
        //GIVEN
        String partnerId = "INVALID ID";

        //WHEN
        final String apiUrl = QPORTAL_URL + "/partners/" + partnerId;
        final MvcResult result = performGetRequest(apiUrl);

        //THEN
        Response<QuincusError> response = extractErrorResponse(result);

        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(1)
                .build();

        List<FieldError> expectedFieldErrors = buildErrorList(new FieldError("get_partner.partner_id", "must be a valid UUIDv4 format"));

        assertCommonErrorStructure(response, expectedError);
        assertFieldErrors(response.getData().fieldErrors(), expectedFieldErrors);
    }

    @Test
    @WithMockUser(roles = "SHIPMENTS_VIEW")
    @DisplayName("For a given search key, response should be OK.")
    void listPartnersWithValidParams() throws Exception {
        //GIVEN
        int perPage = 10;
        int page = 3;
        String key = "validkey";

        //WHEN
        final String apiUrl = QPORTAL_URL + "/v2/partners/?per_page=" + perPage + "&page=" + page + "&key=" + key;
        final MvcResult result = performGetRequest(apiUrl);

        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @WithMockUser(roles = "SHIPMENTS_VIEW")
    @DisplayName("For a given invalid page number, response should be bad request.")
    void listPartnersWithInvalidPageValue() throws Exception {
        //GIVEN
        int perPage = 3;
        String page = String.valueOf(Integer.MAX_VALUE) + 1;
        String key = "validkey";

        //WHEN
        final String apiUrl = QPORTAL_URL + "/v2/partners/?per_page=" + perPage + "&page=" + page + "&key=" + key;
        final MvcResult result = performGetRequest(apiUrl);

        //THEN
        Response<QuincusError> response = extractErrorResponse(result);

        ExpectedError expectedError = ExpectedError.builder()
                .message("There is an invalid format in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(INVALID_FORMAT.name())
                .fieldErrorSize(1)
                .build();

        List<FieldError> expectedFieldErrors = buildErrorList(new FieldError("page", "Failed to convert `page` to `int`"));

        assertCommonErrorStructure(response, expectedError);
        assertFieldErrors(response.getData().fieldErrors(), expectedFieldErrors);
    }


    @Test
    @WithMockUser(roles = "SHIPMENTS_VIEW")
    @DisplayName("For a given invalid key size, response should be OK.")
    void listPartnersWithInvalidKeyValueWithRole() throws Exception {
        //GIVEN
        int perPage = 3;
        int page = 2333;
        // key with more than 256 characters
        String key = "cF5*Jz1Xs0Kx8RvNtHgVfZdYqMbPwLoEuArItXoUd1TmYeRvQxSfWzKgVbCjWb0DzWnXcReRvLoUgFb0OzDwYqAmPnJbCzFg" +
                "VdZx3EwQxSnHgVbCjZxWcRvQbLoUgFbDzWnXcRvLo1UgFb0OzDwYqAmPn1BzZdYqMgHgVfZxJz1EwQxSfWzKg1VfZdYqMbPwLcF5*J" +
                "zWnXcRvLoUgFb0OzDwYqAmPn1BzZdYqMgHgVfZxJz1EwQxSfWzKg1VfZdYqMbPwLcF5*JzWnXcRvLoUgFb0OzDwYqAmPn1BzZdYqMgHgV" +
                "fZxJz1EwQxSfWzKg1VfZdYqMbPwLcF5*JzWnXcRvLoUgFb0OzDwYqAmPn1BzZdYqMgHgVfZxJz1EwQxSfWzKg1VfZdYqMbPwLcF5*JzWnX" +
                "cRvLoUgFb0OzDwYqAmPn1BzZdYqMgHgVfZxJz1EwQxSfWzKg1VfZdYqMbPwLcF5*JzWnXcRvLoUgFb0OzDwYqAmPn1BzZdYqMgHgVfZxJz" +
                "1EwQxSfWzKg1VfZdYqMbPwLcF5*JzWnXcRvLoUgFb0OzDwYqAmPn1BzZdYqMgHgVfZxJz1EwQxSfWzKg1VfZdYqMbPwLcF5*JzWnXcRvL" +
                "oUgFb0OzDwYqAmPn1BzZdYqMgHgVfZxJz1EwQxSfWzKg1VfZdYqMbPwLcF5*JzWnXcRvLoUgFb0OzDwYqAmPn1BzZdYqMgHgVfZxJz1EwQxS" +
                "fWzKg1VfZdYqMbPwLcF5*JzWnXcRvLoUgFb0OzDwYqAmPn1BzZdYqMgHgVfZxJz1EwQxSfWzKg1VfZdYqMbPwLcF5*JzWnXcRvLoUgFb0OzDw" +
                "YqAmPn1BzZdYqMgHgVfZxJz1EwQxSfWzKg1VfZdYqMbPwLcF5*JzWnXcRvLoUgFb0OzDwYqAmPn1BzZdYqMgHgVfZxJz1EwQxSfWzKg1VfZdYq" +
                "MbPwLcF5*JzWnXcRvLoUgFb0OzDwYqAmPn1BzZdYqMgHgVfZxJz1EwQxSfWzKg1VfZdYqMbPwLcF5*JzWnXcRvLoUgFb0OzDwYqAmPn1BzZdYqM" +
                "gHgVfZxJz1EwQxSfWzKg1VfZdYqMbPwLcF5*JzWnXcRvLoUgFb0OzDwYqAmPn1BzZdYqMgHgVfZxJz1EwQxSfWzKg1VfZdYqMbPwLcF5*JzWnXcR" +
                "vLoUgFb0OzDwYqAmPn1BzZdYqMgHgVfZxJz1EwQxSfWzKg1VfZdYqMbPwLcF5*JzWnXcRvLoUgFb0OzDwYqAmPn1BzZdYqMgHgVfZxJz1EwQxSfWzKg";

        //WHEN
        final String apiUrl = QPORTAL_URL + "/v2/partners/?per_page=" + perPage + "&page=" + page + "&key=" + key;
        final MvcResult result = performGetRequest(apiUrl);

        //THEN
        Response<QuincusError> response = extractErrorResponse(result);

        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(1)
                .build();

        List<FieldError> expectedFieldErrors = buildErrorList(new FieldError("list_partners.key", "size must be between 1 and 256"));

        assertCommonErrorStructure(response, expectedError);
        assertFieldErrors(response.getData().fieldErrors(), expectedFieldErrors);
    }

    @Test
    @WithMockUser(roles = "SHIPMENTS_EDIT")
    @DisplayName("For a given valid user Id, response should be OK.")
    void returnOkForValidUserId() throws Exception {
        //GIVEN
        UUID userId = UUID.randomUUID();

        //WHEN
        final String apiUrl = QPORTAL_URL + "/users/" + userId;
        final MvcResult result = performGetRequest(apiUrl);

        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @WithMockUser(roles = "SHIPMENTS_EDIT")
    @DisplayName("Given a user with SHIPMENTS_EDIT role, when an invalid User ID format is provided, a validation error should be returned.")
    void shouldReturnValidationErrorForInvalidUserIdFormatWithEditRole() throws Exception {
        //GIVEN
        String userId = "INVALID-USER-ID";

        //WHEN
        final String apiUrl = QPORTAL_URL + "/users/" + userId;
        final MvcResult result = performGetRequest(apiUrl);

        //THEN
        Response<QuincusError> response = extractErrorResponse(result);

        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(1)
                .build();

        List<FieldError> expectedFieldErrors = buildErrorList(new FieldError("get_user.user_id", "must be a valid UUIDv4 format"));

        assertCommonErrorStructure(response, expectedError);
        assertFieldErrors(response.getData().fieldErrors(), expectedFieldErrors);
    }

    @Test
    @WithMockUser(roles = "SHIPMENTS_EXPORT")
    @DisplayName("For a given valid location id, response should be OK.")
    void returnOkForValidaLocationId() throws Exception {
        //GIVEN
        UUID locationId = UUID.randomUUID();

        //WHEN
        final String apiUrl = QPORTAL_URL + "/locations/" + locationId;
        final MvcResult result = performGetRequest(apiUrl);

        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @WithMockUser(roles = "SHIPMENTS_EXPORT")
    @DisplayName("Given a user with SHIPMENTS_EXPORT role, when an invalid User ID format is provided, a validation error should be returned.")
    void shouldReturnValidationErrorForInvalidLocationIdWithRole() throws Exception {
        //GIVEN
        String locationId = "INVALID-LOC-ID";

        //WHEN
        final String apiUrl = QPORTAL_URL + "/locations/" + locationId;
        final MvcResult result = performGetRequest(apiUrl);

        //THEN
        Response<QuincusError> response = extractErrorResponse(result);

        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(1)
                .build();

        List<FieldError> expectedFieldErrors = buildErrorList(new FieldError("get_location.location_id", "must be a valid UUIDv4 format"));

        assertCommonErrorStructure(response, expectedError);
        assertFieldErrors(response.getData().fieldErrors(), expectedFieldErrors);
    }

    @Test
    @WithMockUser(roles = "SHIPMENT_STATUS_VIEW")
    @DisplayName("For a given search key, response should be OK.")
    void listMilestonesWithValidParams() throws Exception {
        //GIVEN
        int perPage = 10;
        int page = 3;
        String key = "valid-milestone";

        //WHEN
        final String apiUrl = QPORTAL_URL + "/v2/milestones?per_page=" + perPage + "&page=" + page + "&key=" + key;
        final MvcResult result = performGetRequest(apiUrl);

        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @WithMockUser(roles = "SHIPMENT_STATUS_VIEW")
    @DisplayName("For a given invalid per page, response should be 400.")
    void listMilestonesWithInvalidPerPageValue() throws Exception {
        //GIVEN
        int perPage = 10003;
        int page = 32;
        String key = "validkey";

        //WHEN
        final String apiUrl = QPORTAL_URL + "/v2/milestones?per_page=" + perPage + "&page=" + page + "&key=" + key;
        final MvcResult result = performGetRequest(apiUrl);

        //THEN
        Response<QuincusError> response = extractErrorResponse(result);

        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(1)
                .build();

        List<FieldError> expectedFieldErrors = buildErrorList(new FieldError("list_milestones.per_page", "must be less than or equal to 10000"));

        assertCommonErrorStructure(response, expectedError);
        assertFieldErrors(response.getData().fieldErrors(), expectedFieldErrors);
    }

    @Test
    @WithMockUser(roles = "SHIPMENT_STATUS_VIEW")
    @DisplayName("For a given invalid key size, response should be OK.")
    void listMilestonesWithInvalidKeyValueWithRole() throws Exception {
        //GIVEN
        int perPage = 998;
        int page = 32;
        String key = "";

        //WHEN
        final String apiUrl = QPORTAL_URL + "/v2/milestones?per_page=" + perPage + "&page=" + page + "&key=" + key;
        final MvcResult result = performGetRequest(apiUrl);

        //THEN
        Response<QuincusError> response = extractErrorResponse(result);

        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(1)
                .build();

        List<FieldError> expectedFieldErrors = buildErrorList(new FieldError("list_milestones.key", "size must be between 1 and 256"));

        assertCommonErrorStructure(response, expectedError);
        assertFieldErrors(response.getData().fieldErrors(), expectedFieldErrors);
    }
}
