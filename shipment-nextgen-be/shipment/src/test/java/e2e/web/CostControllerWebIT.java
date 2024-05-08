package e2e.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.quincus.shipment.api.CostApi;
import com.quincus.shipment.api.constant.CostCategory;
import com.quincus.shipment.api.domain.Cost;
import com.quincus.shipment.api.domain.CostType;
import com.quincus.shipment.api.domain.Currency;
import com.quincus.shipment.api.domain.ProofOfCost;
import com.quincus.shipment.api.filter.CostAmountRange;
import com.quincus.shipment.api.filter.CostFilter;
import com.quincus.shipment.api.filter.ShipmentFilter;
import com.quincus.shipment.impl.test_utils.TestUtil;
import com.quincus.shipment.impl.web.CostControllerImpl;
import com.quincus.shipment.impl.web.exception.ShipmentExceptionHandler;
import com.quincus.web.common.exception.model.QuincusError;
import com.quincus.web.common.model.Request;
import com.quincus.web.common.model.Response;
import com.quincus.web.common.web.BaseShipmentControllerWebIT;
import org.apache.commons.lang3.RandomStringUtils;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.quincus.shipment.api.constant.ShipmentErrorCode.VALIDATION_ERROR;
import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(controllers = {CostControllerImpl.class})
@ContextConfiguration(classes = {CostControllerImpl.class, ShipmentExceptionHandler.class})
class CostControllerWebIT extends BaseShipmentControllerWebIT {
    private static final String COST_URL = "/costs";
    private final static TestUtil TEST_UTIL = TestUtil.getInstance();

    @MockBean
    private CostApi costApi;

    @Override
    protected MockMvcConfigurer applySpringSecurity() {
        return SecurityMockMvcConfigurers.springSecurity();
    }

    @Test
    @WithMockUser(roles = "COST_VIEW")
    @DisplayName("Given a user with COST_VIEW role, when a valid cost ID is provided, the response should be OK.")
    void shouldReturnOkForValidCostIdWithViewRole() throws Exception {
        //GIVEN
        UUID costId = UUID.randomUUID();
        //WHEN
        final String apiUrl = COST_URL + "/" + costId;
        final MvcResult result = performGetRequest(apiUrl);

        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Given a user without any roles, when a cost request is made with a valid ID, an unauthorized response should be returned.")
    void shouldReturnUnauthorizedForValidCostIdWithoutRole() throws Exception {
        //GIVEN
        UUID costId = UUID.randomUUID();

        //WHEN
        final String apiUrl = COST_URL + "/" + costId;
        final MvcResult result = performGetRequest(apiUrl);

        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @WithMockUser(roles = "COST_VIEW")
    @DisplayName("Given a user with COST_VIEW role, when an invalid cost ID format is provided, a validation error should be returned.")
    void shouldReturnValidationErrorForInvalidCostIdFormatWithViewRole() throws Exception {
        //GIVEN
        String costId = "INVALID ID";

        //WHEN
        final String apiUrl = COST_URL + "/" + costId;
        final MvcResult result = performGetRequest(apiUrl);

        //THEN
        Response<QuincusError> response = extractErrorResponse(result);

        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(1)
                .build();

        List<FieldError> expectedFieldErrors = buildErrorList(new FieldError("find.id", "must be a valid UUIDv4 format"));

        assertCommonErrorStructure(response, expectedError);
        assertFieldErrors(response.getData().fieldErrors(), expectedFieldErrors);
    }

    @Test
    @WithMockUser(roles = {"COST_CREATE", "S2S"})
    @DisplayName("Given a user with COST_CREATE role, when a valid cost request is made, then the response should be OK.")
    void shouldCreateCostSuccessfullyWithAppropriateRole() throws Exception {
        //GIVEN
        Request<Cost> request = createValidCostRequest();

        //WHEN
        final MvcResult result = performPostRequest(COST_URL, request);

        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @WithMockUser(roles = {"COST_CREATE", "S2S"})
    @DisplayName("Given a user with COST_CREATE role, when a valid cost request is made with category time-based, then the response should be OK.")
    void shouldCreateCostSuccessfullyWithAppropriateRoleAndCategoryIsTimeBased() throws Exception {
        //GIVEN
        Cost cost = createCost();
        int limit = 2000;
        String longRemarks = RandomStringUtils.randomAlphabetic(limit);
        cost.setRemarks(longRemarks);
        CostType costType = new CostType();
        costType.setId(cost.getCostType().getId());
        costType.setCategory(CostCategory.TIME_BASED);

        Currency currency = new Currency();
        currency.setId("");
        cost.setCostType(costType);
        cost.setCurrency(currency);

        Request<Cost> request = createValidCostRequest(cost);

        //WHEN
        final MvcResult result = performPostRequest(COST_URL, request);

        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @WithMockUser(roles = {"COST_CREATE", "S2S"})
    @DisplayName("Given a user with COST_CREATE and S2S roles, when a category non time-based ,currency id is null and invalid proof of cost, then a validation error should be returned.")
    void shouldReturnValidationErrorWhenNonTimeBasedCategoryAndCurrencyIdIsNullAndInvalidProofOfCosts() throws Exception {
        //GIVEN
        Cost cost = createCost();
        CostType costType = new CostType();
        costType.setId(cost.getCostType().getId());
        costType.setCategory(CostCategory.NON_TIME_BASED);

        Currency currency = new Currency();
        currency.setId("");
        cost.setCostType(costType);
        cost.setCurrency(currency);
        ProofOfCost invalidProofOfCost = cost.getProofOfCost().get(0);
        invalidProofOfCost.setFileSize((long) (100 * 1024 * 1024));
        invalidProofOfCost.setFileName("invalid");
        List<ProofOfCost> additionalProofOfCosts = new ArrayList<>(cost.getProofOfCost());
        additionalProofOfCosts.addAll(cost.getProofOfCost());
        cost.setProofOfCost(additionalProofOfCosts);

        Request<Cost> request = createValidCostRequest(cost);

        //WHEN
        final MvcResult result = performPostRequest(COST_URL, request);

        //THEN
        Response<QuincusError> response = extractErrorResponse(result);

        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(6)
                .build();

        List<FieldError> expectedFieldErrors = buildErrorList(
                new FieldError("data.currency.id", "Must be required and a valid UUID v4 format for non-time-based cost types"),
                new FieldError("data.proof_of_cost[4].file_name", "Invalid file name format. It must have an extension."),
                new FieldError("data.proof_of_cost[0].file_size", "File size should not exceed 24MB."),
                new FieldError("data.proof_of_cost[0].file_name", "Invalid file name format. It must have an extension."),
                new FieldError("data.proof_of_cost[4].file_size", "File size should not exceed 24MB."),
                new FieldError("data.currency.id", "Must be required and a valid UUID v4 format for non-time-based cost types"),
                new FieldError("data.proof_of_cost", "size must be between 0 and 5")
        );


        assertCommonErrorStructure(response, expectedError);
        assertFieldErrors(response.getData().fieldErrors(), expectedFieldErrors);
    }

    @Test
    @DisplayName("Given a user without any role, when a cost request is made, then the response should be UNAUTHORIZED.")
    void shouldNotCreateCostForUserWithoutRole() throws Exception {
        //GIVEN
        Request<Cost> request = createValidCostRequest();

        //WHEN
        final MvcResult result = performPostRequest(COST_URL, request);

        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @WithMockUser(roles = {"COST_CREATE", "S2S"})
    @DisplayName("Given a user with COST_CREATE and S2S roles, when an invalid cost request is made, then a validation error should be returned.")
    void shouldReturnValidationErrorWhenInvalidCostRequestIsMadeWithCOST_CREATERole() throws Exception {
        //GIVEN
        int limit = 2000;
        String longRemarks = RandomStringUtils.randomAlphabetic(limit + 1);
        Cost cost = createInvalidCost();
        cost.setRemarks(longRemarks);

        Request<Cost> request = createInvalidCostRequest(cost);

        //WHEN
        MvcResult result = performPostRequest(COST_URL, request);

        //THEN
        Response<QuincusError> response = extractErrorResponse(result);

        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(14)
                .build();

        List<FieldError> expectedFieldErrors = getExpectedErrorFieldsOnCreateAndUpdate();

        assertCommonErrorStructure(response, expectedError);
        assertFieldErrors(response.getData().fieldErrors(), expectedFieldErrors);
    }

    @Test
    @WithMockUser(roles = "COST_EDIT")
    @DisplayName("Given a user with COST_EDIT role, when a valid cost update request is made, then the update should succeed.")
    void shouldUpdateSuccessfullyWhenValidCostRequestIsMadeWithCOST_EDITRole() throws Exception {
        //GIVEN
        Request<Cost> request = createValidCostRequest();
        String costId = request.getData().getId();

        //WHEN
        final String apiUrl = COST_URL + "/" + costId;
        final MvcResult result = performPutRequest(apiUrl, request);

        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Given a user without the COST_EDIT role, when a cost update request is made, then an unauthorized error should be returned.")
    void shouldReturnUnauthorizedErrorWhenCostUpdateRequestIsMadeWithoutCOST_EDITRole() throws Exception {
        //GIVEN
        Request<Cost> request = createValidCostRequest();
        String costId = request.getData().getId();

        //WHEN
        final String apiUrl = COST_URL + "/" + costId;
        final MvcResult result = performPutRequest(apiUrl, request);

        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @WithMockUser(roles = "COST_EDIT")
    @DisplayName("Given a user with COST_EDIT role, when an invalid cost update request is made, then a validation error should be returned.")
    void shouldReturnValidationErrorWhenInvalidCostUpdateRequestIsMadeWithCOST_EDITRole() throws Exception {
        //GIVEN
        int limit = 2000;
        String longRemarks = RandomStringUtils.randomAlphabetic(limit + 1);
        Cost cost = createInvalidCost();
        cost.setRemarks(longRemarks);
        Request<Cost> request = createInvalidCostRequest(cost);
        String costId = request.getData().getId();

        //WHEN
        final String apiUrl = COST_URL + "/" + costId;
        final MvcResult result = performPutRequest(apiUrl, request);

        //THEN
        Response<QuincusError> response = extractErrorResponse(result);

        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(14)
                .build();

        List<FieldError> expectedFieldErrors = getExpectedErrorFieldsOnCreateAndUpdate();

        assertCommonErrorStructure(response, expectedError);
        assertFieldErrors(response.getData().fieldErrors(), expectedFieldErrors);
    }

    @Test
    @WithMockUser(roles = "COST_VIEW")
    @DisplayName("Given a user with COST_VIEW role, when a cost filter request is made, then the response should be OK.")
    void shouldReturnCostsForFilterWithViewRole() throws Exception {
        //GIVEN
        Request<CostFilter> request = new Request<>();
        CostFilter costFilter = new CostFilter();
        costFilter.setPageNumber(10);
        costFilter.setSize(10);
        request.setData(costFilter);

        //WHEN
        final String apiUrl = COST_URL + "/list";
        final MvcResult result = performPostRequest(apiUrl, request);

        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Given a user without COST_VIEW role, when a cost filter request is made, then an unauthorized response should be returned.")
    void shouldReturnUnauthorizedForCostFilterWithoutRole() throws Exception {
        //GIVEN
        Request<CostFilter> request = new Request<>();
        CostFilter costFilter = new CostFilter();
        costFilter.setPageNumber(10);
        costFilter.setSize(10);
        request.setData(costFilter);

        //WHEN
        final String apiUrl = COST_URL + "/list";
        final MvcResult result = performPostRequest(apiUrl, request);

        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @WithMockUser(roles = "COST_VIEW")
    @DisplayName("Given a user with COST_VIEW role, when a cost filter request with invalid parameters is made, a validation error should be returned.")
    void shouldReturnValidationErrorForInvalidCostFilterRequestWithViewRole() throws Exception {
        //GIVEN
        String longTextValue = new String(new char[10000]).replace('\0', 'a');
        Request<CostFilter> request = new Request<>();
        CostFilter costFilter = new CostFilter();
        costFilter.setPageNumber(-1);
        costFilter.setSize(-1);
        costFilter.setSortDir(longTextValue);
        costFilter.setSortBy(longTextValue);
        CostAmountRange costAmountRange = new CostAmountRange();
        costAmountRange.setMaxCostAmount(new BigDecimal("11111111111111111111111111"));
        costAmountRange.setMinCostAmount(new BigDecimal("11111111111111111111111111"));
        costFilter.setCostAmountRange(costAmountRange);

        request.setData(costFilter);

        //WHEN
        final String apiUrl = COST_URL + "/list";
        final MvcResult result = performPostRequest(apiUrl, request);

        //THEN
        Response<QuincusError> response = extractErrorResponse(result);

        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(6)
                .build();

        List<FieldError> expectedFieldErrors = buildErrorList(
                new FieldError("data.sort_dir", "size must be between 0 and 10"),
                new FieldError("data.sort_by", "size must be between 0 and 100"),
                new FieldError("data.page_number", "must be greater than or equal to 1"),
                new FieldError("data.size", "must be greater than or equal to 1"),
                new FieldError("data.cost_amount_range.max_cost_amount", "numeric value out of bounds (<15 digits>.<2 digits> expected)"),
                new FieldError("data.cost_amount_range.min_cost_amount", "numeric value out of bounds (<15 digits>.<2 digits> expected)"));

        assertCommonErrorStructure(response, expectedError);
        assertFieldErrors(response.getData().fieldErrors(), expectedFieldErrors);
    }

    @Test
    @WithMockUser(roles = {"COST_CREATE", "COST_EDIT"})
    @DisplayName("Given a user with COST_CREATE and COST_EDIT roles, when a shipment search is performed, then the response should be OK.")
    void shouldReturnShipmentsForSearchWithCreateAndEditRole() throws Exception {
        //GIVEN
        Request<ShipmentFilter> request = new Request<>();
        ShipmentFilter shipmentFilter = new ShipmentFilter();
        shipmentFilter.setPageNumber(10);
        shipmentFilter.setSize(10);
        request.setData(shipmentFilter);

        //WHEN
        final String apiUrl = COST_URL + "/shipments/search";
        final MvcResult result = performPostRequest(apiUrl, request);

        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Given a user without COST_CREATE and COST_EDIT roles, when a shipment search is performed, then an unauthorized response should be returned.")
    void shouldReturnUnauthorizedForShipmentSearchWithoutRoles() throws Exception {
        //GIVEN
        Request<ShipmentFilter> request = new Request<>();
        ShipmentFilter shipmentFilter = new ShipmentFilter();
        shipmentFilter.setPageNumber(10);
        shipmentFilter.setSize(10);
        request.setData(shipmentFilter);

        //WHEN
        final String apiUrl = COST_URL + "/shipments/search";
        final MvcResult result = performPostRequest(apiUrl, request);

        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @WithMockUser(roles = {"COST_CREATE", "COST_EDIT"})
    @DisplayName("Given a user with COST_CREATE and COST_EDIT roles, when a shipment filter request with invalid parameters is made, a validation error should be returned.")
    void shouldReturnValidationErrorForInvalidShipmentFilterWithCreateAndEditRoles() throws Exception {
        //GIVEN
        Request<ShipmentFilter> request = new Request<>();
        ShipmentFilter shipmentFilter = new ShipmentFilter();
        shipmentFilter.setPageNumber(-1);
        shipmentFilter.setSize(-1);
        request.setData(shipmentFilter);

        //WHEN
        final String apiUrl = COST_URL + "/shipments/search";
        final MvcResult result = performPostRequest(apiUrl, request);

        //THEN
        Response<QuincusError> response = extractErrorResponse(result);

        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(2)
                .build();

        List<FieldError> expectedFieldErrors = buildErrorList(
                new FieldError("data.page_number", "must be greater than or equal to 1"),
                new FieldError("data.size", "must be greater than or equal to 1"));

        assertCommonErrorStructure(response, expectedError);
        assertFieldErrors(response.getData().fieldErrors(), expectedFieldErrors);
    }

    private List<FieldError> getExpectedErrorFieldsOnCreateAndUpdate() {
        return buildErrorList(
                new FieldError("data.cost_amount", "numeric value out of bounds (<15 digits>.<2 digits> expected)"),
                new FieldError("data.cost_type.id", "must be a valid UUIDv4 format"),
                new FieldError("data.currency.id", "must be a valid UUIDv4 format"),
                new FieldError("data.proof_of_cost[3].file_name", "Invalid file name format. It must have an extension."),
                new FieldError("data.driver_id", "must be a valid UUIDv4 format"),
                new FieldError("data.proof_of_cost[2].file_name", "Invalid file name format. It must have an extension."),
                new FieldError("data.shipments[0].segments[0].segment_id", "must be a valid UUIDv4 format"),
                new FieldError("data.organization_id", "must be a valid UUIDv4 format"),
                new FieldError("data.proof_of_cost[1].file_name", "Invalid file name format. It must have an extension."),
                new FieldError("data.proof_of_cost[0].file_name", "Invalid file name format. It must have an extension."),
                new FieldError("data.shipments[0].id", "must be a valid UUIDv4 format"),
                new FieldError("data.issued_timezone", "must not be blank"),
                new FieldError("data.source", "size must be between 0 and 50")
        );
    }

    private Request<Cost> createInvalidCostRequest(Cost cost) {
        Request<Cost> request = new Request<>();
        request.setData(cost);
        return request;
    }

    private Cost createInvalidCost() throws JsonProcessingException {
        JsonNode data = TEST_UTIL.getDataFromFile("samplepayload/request/invalidCostRequest.json");
        return objectMapper.readValue(data.get("data").toString(), Cost.class);
    }

    private Request<Cost> createValidCostRequest() throws JsonProcessingException {
        JsonNode data = TEST_UTIL.getDataFromFile("samplepayload/request/costRequest.json");
        Request<Cost> request = new Request<>();
        request.setData(objectMapper.readValue(data.get("data").toString(), Cost.class));
        return request;
    }

    private Cost createCost() throws JsonProcessingException {
        JsonNode data = TEST_UTIL.getDataFromFile("samplepayload/request/costRequest.json");
        return objectMapper.readValue(data.get("data").toString(), Cost.class);
    }

    private Request<Cost> createValidCostRequest(Cost cost) {
        Request<Cost> request = new Request<>();
        request.setData(cost);
        return request;
    }
}