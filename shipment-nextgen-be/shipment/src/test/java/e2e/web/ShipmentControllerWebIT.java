package e2e.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.quincus.qlogger.api.QLoggerAPI;
import com.quincus.shipment.api.ShipmentApi;
import com.quincus.shipment.api.constant.MeasurementUnit;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.dto.PackageDimensionUpdateRequest;
import com.quincus.shipment.api.dto.ShipmentMilestoneOpsUpdateRequest;
import com.quincus.shipment.api.dto.UserLocation;
import com.quincus.shipment.api.filter.ExportFilter;
import com.quincus.shipment.api.filter.ShipmentFilter;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.test_utils.TestUtil;
import com.quincus.shipment.impl.web.ShipmentControllerImpl;
import com.quincus.shipment.impl.web.exception.ShipmentExceptionHandler;
import com.quincus.web.common.exception.model.QuincusError;
import com.quincus.web.common.exception.model.QuincusFieldError;
import com.quincus.web.common.model.Request;
import com.quincus.web.common.model.Response;
import com.quincus.web.common.web.BaseShipmentControllerWebIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.quincus.shipment.api.constant.ShipmentErrorCode.VALIDATION_ERROR;
import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(controllers = {ShipmentControllerImpl.class})
@ContextConfiguration(classes = {ShipmentControllerImpl.class, ShipmentExceptionHandler.class})
class ShipmentControllerWebIT extends BaseShipmentControllerWebIT {

    private static final String SHIPMENTS = "/shipments";
    private static final String SHIPMENTS_BULK = "/shipments/bulk";
    private final static TestUtil TEST_UTIL = TestUtil.getInstance();

    @MockBean
    private ShipmentApi shipmentApi;
    @MockBean
    private QLoggerAPI qLoggerAPI;
    @MockBean
    private UserDetailsProvider userDetailsProvider;

    @Override
    protected MockMvcConfigurer applySpringSecurity() {
        return SecurityMockMvcConfigurers.springSecurity();
    }

    @Test
    @DisplayName("Given a user without any roles, when a shipment update request is made, an unauthorized response should be returned.")
    void shouldReturnUnauthorizedForValidShipmentWithoutRole() throws Exception {
        //GIVEN
        Request<Shipment> request = createValidShipmentRequest();

        //WHEN
        final MvcResult result = performPutRequest(SHIPMENTS, request);

        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @WithMockUser(roles = {"SHIPMENTS_EDIT"})
    @DisplayName("Given a user with SHIPMENTS_EDIT role, when a valid shipment request is made, then the response should be OK.")
    void updateShipmentWithValidRequest() throws Exception {
        //GIVEN
        Request<Shipment> request = createValidShipmentRequest();

        //WHEN
        final MvcResult result = performPutRequest(SHIPMENTS, request);

        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @WithMockUser(roles = {"SHIPMENTS_EDIT"})
    @DisplayName("Given a user with SHIPMENTS_EDIT, when invalid details entered for a shipment update a validation error should be returned.")
    void shouldReturnValidationErrorWhenShipmentHadInvalidDetails() throws Exception {
        //GIVEN
        Shipment shipment = createShipment();
        shipment.setId("not-a-uuid");
        shipment.setShipmentTrackingId("ddadadadhkjkkkjkjkadnmndakdkjjajlndao0894384i3ukrnkrh837483yuhrekhrieyr877" +
                "43753975398593753hrierererery38683yr83yksjdgklns80973978563jnjsfhskjfbsigfaskfnsfhoaioi8748736jnjljsn");
        List<String> shipmentReferenceId = shipment.getShipmentReferenceId();
        shipmentReferenceId.add("additional-id");
        shipment.setShipmentReferenceId(shipmentReferenceId);
        shipment.getShipmentJourney().setOrderId("not-a-valid-order-id");
        shipment.getShipmentJourney().setJourneyId("not-a-valid-journey-id");

        Request<Shipment> request = createValidShipmentRequest(shipment);

        //WHEN
        final MvcResult result = performPutRequest(SHIPMENTS, request);

        //THEN
        Response<QuincusError> response = extractErrorResponse(result);

        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(5)
                .build();

        List<FieldError> expectedFieldErrors = buildErrorList(
                new FieldError("data.id", "must be a valid UUIDv4 format"),
                new FieldError("data.shipment_journey.order_id", "must be a valid UUIDv4 format"),
                new FieldError("data.shipment_journey.journey_id", "must be a valid UUIDv4 format"),
                new FieldError("data.shipment_tracking_id", "Maximum of 48 characters allowed."),
                new FieldError("data.shipment_reference_id", "size must be between 0 and 5")
        );


        assertCommonErrorStructure(response, expectedError);
        assertFieldErrors(response.getData().fieldErrors(), expectedFieldErrors);
    }

    @Test
    @WithMockUser(roles = {"SHIPMENTS_EDIT"})
    @DisplayName("Given a user with SHIPMENTS_EDIT role, when a valid ShipmentJourney request is made, then the response should be OK.")
    void updateShipmentJourneyWithValidRequest() throws Exception {
        //GIVEN
        Request<ShipmentJourney> request = createValidUpdateShipmentJourneyRequest();

        //WHEN
        final MvcResult result = performPutRequest(SHIPMENTS + "/shipment_journey", request);

        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @WithMockUser(roles = {"SHIPMENTS_EDIT"})
    @DisplayName("Given a user with SHIPMENTS_EDIT, when invalid details entered for a Shipment Journey update a validation error should be returned.")
    void shouldReturnValidationErrorWhenShipmentJourneyHadInvalidDetails() throws Exception {
        //GIVEN
        ShipmentJourney shipmentJourney = createShipmentJourney();
        shipmentJourney.setJourneyId("not-a-uuid");
        shipmentJourney.setShipmentId("not-a-valid-shipment-id");
        shipmentJourney.setOrderId("not-a-valid-order-id");

        Request<ShipmentJourney> request = createValidShipmentJourneyRequest(shipmentJourney);

        //WHEN
        final MvcResult result = performPutRequest(SHIPMENTS + "/shipment_journey", request);

        //THEN
        Response<QuincusError> response = extractErrorResponse(result);

        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(3)
                .build();

        List<FieldError> expectedFieldErrors = buildErrorList(
                new FieldError("data.journey_id", "must be a valid UUIDv4 format"),
                new FieldError("data.shipment_id", "must be a valid UUIDv4 format"),
                new FieldError("data.order_id", "must be a valid UUIDv4 format")
        );


        assertCommonErrorStructure(response, expectedError);
        assertFieldErrors(response.getData().fieldErrors(), expectedFieldErrors);
    }

    @Test
    @WithMockUser(roles = {"SHIPMENT_STATUS_EDIT"})
    @DisplayName("Given a user with SHIPMENT_STATUS_EDIT role, when updating package dimension from given shipment tracking id, then the response should be OK.")
    void updatePackageDimensionFromShipmentTrackingIdWithValidRequest() throws Exception {
        //GIVEN
        Request<PackageDimensionUpdateRequest> request = createPackageDimensionUpdateRequest();
        String shipmentTrackingId = request.getData().getShipmentTrackingId();

        //WHEN
        final MvcResult result = performPutRequest(SHIPMENTS + "/get-by-tracking-id/" + shipmentTrackingId + "/package-dimension", request);

        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }


    @Test
    @WithMockUser(roles = {"SHIPMENT_STATUS_EDIT"})
    @DisplayName("Given a user with SHIPMENT_STATUS_EDIT, when invalid details entered for package dimension update, a validation error should be returned.")
    void shouldReturnValidationErrorForInvalidPackageDimensionDetails() throws Exception {
        //GIVEN
        String shipmentTrackingId = "01QCPC202306020314";
        PackageDimensionUpdateRequest packageDimensionUpdate = createPackageDimensionUpdate();
        packageDimensionUpdate.setUserId("not-a-valid-user-id");
        packageDimensionUpdate.setShipmentTrackingId("ddadadadhkjkkkjkjkadnmndakdkjjajlndao0894384i3ukrnkrh837483yuhrekhrieyr877" +
                "daddadlkdaldnkf24294k4i24u20424j204i2oj4024i-24nrwjrojwrpwrpiwrkwhrkwjrjwrowrrwoir4-394-943-9-493-94-94-394");

        Request<PackageDimensionUpdateRequest> request = createPackageDimensionUpdateRequest(packageDimensionUpdate);

        //WHEN
        final MvcResult result = performPutRequest(SHIPMENTS + "/get-by-tracking-id/" + shipmentTrackingId + "/package-dimension", request);

        //THEN
        Response<QuincusError> response = extractErrorResponse(result);

        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(2)
                .build();

        List<FieldError> expectedFieldErrors = buildErrorList(
                new FieldError("data.user_id", "must be a valid UUIDv4 format"),
                new FieldError("data.shipment_tracking_id", "Maximum of 48 characters allowed.")
        );


        assertCommonErrorStructure(response, expectedError);
        assertFieldErrors(response.getData().fieldErrors(), expectedFieldErrors);
    }


    @Test
    @WithMockUser(roles = {"SHIPMENTS_EDIT"})
    @DisplayName("Given a user with SHIPMENTS_EDIT role, when a valid shipment id is provided, a success response should be returned.")
    void cancelShipmentSuccessForValidShipmentId() throws Exception {
        //GIVEN
        UUID id = UUID.randomUUID();

        //WHEN
        final String apiUrl = SHIPMENTS + "/cancel/" + id;
        final MvcResult result = performPatchRequest(apiUrl);

        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @WithMockUser(roles = "SHIPMENTS_EDIT")
    @DisplayName("Given a user with SHIPMENTS_EDIT role, when an invalid Shipment ID format is provided, a validation error should be returned.")
    void cancelShipmentFailureForInvalidShipmentId() throws Exception {
        //GIVEN
        String id = "INVALID ID";

        //WHEN
        final String apiUrl = SHIPMENTS + "/cancel/" + id;
        final MvcResult result = performPatchRequest(apiUrl);

        //THEN
        Response<QuincusError> response = extractErrorResponse(result);

        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(1)
                .build();

        List<FieldError> expectedFieldErrors = buildErrorList(new FieldError("cancel.id", "must be a valid UUIDv4 format"));

        assertCommonErrorStructure(response, expectedError);
        assertFieldErrors(response.getData().fieldErrors(), expectedFieldErrors);
    }


    @Test
    @WithMockUser(roles = {"SHIPMENT_STATUS_EDIT"})
    @DisplayName("Given a user with SHIPMENT_STATUS_EDIT role, when a valid shipmentTracing id and shipmentMilestoneUpdateRequest is provided, a success response should be returned.")
    void updateShipmentMilestoneForValidShipmentTrackingId() throws Exception {
        //GIVEN
        String shipmentTrackingId = "valid-tracking-id";
        Request<ShipmentMilestoneOpsUpdateRequest> shipmentMilestoneOpsUpdateRequest = createShipmentMilestoneOpsUpdateRequest();

        //WHEN
        final String apiUrl = SHIPMENTS + "/get-by-tracking-id/" + shipmentTrackingId + "/milestone-and-additional-info";
        final MvcResult result = performPatchRequest(apiUrl, shipmentMilestoneOpsUpdateRequest);

        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @WithMockUser(roles = "SHIPMENTS_EDIT")
    @DisplayName("Given a user with SHIPMENT_STATUS_EDIT role, when a invalid shipmentTracing id and shipmentMilestoneUpdateRequest is provided a validation error should be returned.")
    void updateShipmentMilestoneForInvalidShipmentTrackingIdAndPayLoadFailure() throws Exception {
        //GIVEN
        String shipmentTrackingId = "shipment-tracking-id";
        ShipmentMilestoneOpsUpdateRequest shipmentMilestoneOpsUpdate = createShipmentMilestoneOpsUpdate();
        shipmentMilestoneOpsUpdate.setMilestoneName(null);
        shipmentMilestoneOpsUpdate.setMilestoneTime(null);

        Request<ShipmentMilestoneOpsUpdateRequest> shipmentMilestoneOpsUpdateRequest = createShipmentMilestoneOpsUpdateRequest(shipmentMilestoneOpsUpdate);

        //WHEN
        final String apiUrl = SHIPMENTS + "/get-by-tracking-id/" + shipmentTrackingId + "/milestone-and-additional-info";
        final MvcResult result = performPatchRequest(apiUrl, shipmentMilestoneOpsUpdateRequest);

        //THEN
        Response<QuincusError> response = extractErrorResponse(result);

        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(2)
                .build();

        List<FieldError> expectedFieldErrors = buildErrorList(new FieldError("data.milestone_name", "must not be empty"),
                new FieldError("data.milestone_time", "must not be blank"));

        assertCommonErrorStructure(response, expectedError);
        assertFieldErrors(response.getData().fieldErrors(), expectedFieldErrors);
    }


    private ShipmentMilestoneOpsUpdateRequest createShipmentMilestoneOpsUpdate() {
        ShipmentMilestoneOpsUpdateRequest shipmentMilestoneOpsUpdateRequest = new ShipmentMilestoneOpsUpdateRequest();
        shipmentMilestoneOpsUpdateRequest.setMilestoneName("milestone_name");
        shipmentMilestoneOpsUpdateRequest.setMilestoneCode("1405");
        shipmentMilestoneOpsUpdateRequest.setMilestoneTime("2034-08-31T10:46:52.486Z");
        shipmentMilestoneOpsUpdateRequest.setNotes("This is a XXXX");
        UserLocation userLocation = new UserLocation();
        userLocation.setLocationId("2efb867f-c0a3-43e4-9a07-a4e1ca56044b");
        userLocation.setLocationFacilityName("BLACK PINK");
        shipmentMilestoneOpsUpdateRequest.setUsersLocation(userLocation);

        return shipmentMilestoneOpsUpdateRequest;
    }

    private Request<ShipmentMilestoneOpsUpdateRequest> createShipmentMilestoneOpsUpdateRequest() {
        Request<ShipmentMilestoneOpsUpdateRequest> request = new Request<>();
        request.setData(createShipmentMilestoneOpsUpdate());
        return request;
    }

    private Request<ShipmentMilestoneOpsUpdateRequest> createShipmentMilestoneOpsUpdateRequest(ShipmentMilestoneOpsUpdateRequest updateRequest) {
        Request<ShipmentMilestoneOpsUpdateRequest> request = new Request<>();
        request.setData(updateRequest);
        return request;
    }

    private PackageDimensionUpdateRequest createPackageDimensionUpdate() {
        PackageDimensionUpdateRequest packageDimensionUpdateRequest = new PackageDimensionUpdateRequest();
        packageDimensionUpdateRequest.setUserId(UUID.randomUUID().toString());
        packageDimensionUpdateRequest.setUserLocationId("facilityid");
        packageDimensionUpdateRequest.setOrganizationId("orgid");
        packageDimensionUpdateRequest.setShipmentTrackingId("01QCPC202306020314-01");
        packageDimensionUpdateRequest.setMeasurementUnit(MeasurementUnit.METRIC);
        packageDimensionUpdateRequest.setLength(BigDecimal.valueOf(100));
        packageDimensionUpdateRequest.setWidth(BigDecimal.valueOf(23));
        packageDimensionUpdateRequest.setGrossWeight(BigDecimal.valueOf(440));
        packageDimensionUpdateRequest.setHeight(BigDecimal.valueOf(44));
        return packageDimensionUpdateRequest;
    }

    private Request<PackageDimensionUpdateRequest> createPackageDimensionUpdateRequest() {
        Request<PackageDimensionUpdateRequest> request = new Request<>();
        request.setData(createPackageDimensionUpdate());
        return request;
    }

    private Request<PackageDimensionUpdateRequest> createPackageDimensionUpdateRequest(PackageDimensionUpdateRequest updateRequest) {
        Request<PackageDimensionUpdateRequest> request = new Request<>();
        request.setData(updateRequest);
        return request;
    }

    private Request<Shipment> createValidShipmentRequest(Shipment shipment) {
        Request<Shipment> request = new Request<>();
        request.setData(shipment);
        return request;
    }

    private Request<Shipment> createValidShipmentRequest() throws JsonProcessingException {
        Request<Shipment> request = new Request<>();
        request.setData(createShipment());
        return request;
    }

    private Shipment createShipment() throws JsonProcessingException {
        JsonNode data = TEST_UTIL.getDataFromFile("samplepayload/request/updateSingleShipment.json");
        return objectMapper.readValue(data.get("data").toString(), Shipment.class);
    }

    private Request<ShipmentJourney> createValidUpdateShipmentJourneyRequest() throws JsonProcessingException {
        Request<ShipmentJourney> request = new Request<>();
        request.setData(createShipmentJourney());
        return request;
    }

    private ShipmentJourney createShipmentJourney() throws JsonProcessingException {
        JsonNode data = TEST_UTIL.getDataFromFile("samplepayload/request/updateShipmentJourney.json");
        return objectMapper.readValue(data.get("data").toString(), ShipmentJourney.class);
    }

    private Request<ShipmentJourney> createValidShipmentJourneyRequest(ShipmentJourney shipment) {
        Request<ShipmentJourney> request = new Request<>();
        request.setData(shipment);
        return request;
    }

    // ----------------------------------------------------------------------------- //


    //****************** 4 POST REQUESTS ********************//
    @Test
    @WithMockUser(roles = {"KARATE_USER"})
    @DisplayName("Given a User with KARATE_USER role, is allowed to create a Shipment with a valid data in request.")
    void shouldCreateShipment_SUCCESS_ADD_METHOD() throws Exception {
        //GIVEN
        final Request<Shipment> request = createValidShipmentRequest2();

        //WHEN
        final MvcResult result = performPostRequest(SHIPMENTS, request);

        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @WithMockUser(roles = {"KARATE_USER"})
    @DisplayName("Given a User with KARATE_USER role, is NOT allowed to create a Shipment with a INVALID data in request.")
    void shouldNotCreateShipment_FAILED_ADD_METHOD() throws Exception {
        //GIVEN
        final Request<Shipment> request = createInvalidShipmentRequest();

        //WHEN
        final MvcResult result = performPostRequest(SHIPMENTS, request);

        //THEN
        final Response<QuincusError> response = extractErrorResponse(result);
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        final ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(71)
                .build();
        // 67 errors
        final List<FieldError> expectedFieldErrors = this.shipmentFieldErrors();

        assertCommonErrorStructure(response, expectedError);
        assertFieldErrors(response.getData().fieldErrors(), expectedFieldErrors);
    }

    @Test
    @WithMockUser(roles = {"KARATE_USER"})
    @DisplayName("Given a User with KARATE_USER role, is allowed to create multiple Shipments in bulk with a valid data in request.")
    void shouldCreateShipmentsBulk_SUCCESS() throws Exception {
        //GIVEN
        final Request<List<Shipment>> request = createValidShipmentsRequest();

        //WHEN
        final MvcResult result = performPostRequest(SHIPMENTS_BULK, request);

        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @WithMockUser(roles = {"KARATE_USER"})
    @DisplayName("Given a User with KARATE_USER role, is NOT allowed to create multiple Shipments in bulk with a invalid data in request.")
    void shouldCreateShipmentsBulk_FAILED() throws Exception {
        //GIVEN
        final Request<List<Shipment>> request = createInValidShipmentsRequest();

        //WHEN
        final MvcResult result = performPostRequest(SHIPMENTS_BULK, request);

        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        final Response<QuincusError> response = extractErrorResponse(result);

        assertThat(response.getData().fieldErrors()).containsAll(getExpectedQuincusFieldErrors());
    }

    @Test
    @WithMockUser(roles = {"SHIPMENTS_VIEW"})
    @DisplayName("Given a User with SHIPMENTS_VIEW role, is allowed to list Shipments with a valid filter parameters in request.")
    void shouldListShipments_SUCCESS() throws Exception {
        //GIVEN
        final Request<ShipmentFilter> request = new Request<>();
        final ShipmentFilter filter = new ShipmentFilter();
        filter.setPageNumber(2);
        filter.setSize(10);
        request.setData(filter);
        //WHEN
        final MvcResult result = performPostRequest(SHIPMENTS + "/list", request);
        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @WithMockUser(roles = {"SHIPMENTS_VIEW"})
    @DisplayName("Given a User with SHIPMENTS_VIEW role, is NOT allowed to list Shipments with invalid filter parameters in request.")
    void shouldListShipments_FAILED() throws Exception {
        //GIVEN
        final Request<ShipmentFilter> request = new Request<>();
        final ShipmentFilter filter = new ShipmentFilter();
        filter.setSize(2147483647);
        filter.setPageNumber(2147483647);
        request.setData(filter);
        //WHEN
        final MvcResult result = performPostRequest(SHIPMENTS + "/list", request);
        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @WithMockUser(roles = {"SHIPMENTS_EXPORT"})
    @DisplayName("Given a User with SHIPMENTS_EXPORT role, is allowed to export Shipments retrieved with valid filter parameters in request.")
    void shouldExportShipments_SUCCESS() throws Exception {
        //GIVEN
        final Request<ExportFilter> request = new Request<>();
        final ExportFilter filter = new ExportFilter();
        filter.setDestination(new String[]{"AHD"});
        request.setData(filter);
        //WHEN
        final MvcResult result = performPostRequest(SHIPMENTS + "/export", request);
        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @WithMockUser(roles = {"SHIPMENTS_EXPORT"})
    @DisplayName("Given a User with SHIPMENTS_EXPORT role, is NOT allowed to export Shipments retrieved with invalid filter parameters in request.")
    void shouldExportShipments_FAILED() throws Exception {
        //GIVEN
        final Request<ExportFilter> request = new Request<>();
        final ExportFilter filter = new ExportFilter();
        filter.setDestination(new String[]{"EX-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5"});
        final Order order = new Order();
        order.setStatus("EX-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5");
        filter.setOrder(order);
        request.setData(filter);
        //WHEN
        final MvcResult result = performPostRequest(SHIPMENTS + "/export", request);
        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    //****************** 4 GET REQUESTS ********************//
    @Test
    @WithMockUser(roles = {"SHIPMENTS_VIEW"})
    @DisplayName("Given a User with SHIPMENTS_VIEW role, is allowed to find a Shipment with a valid ID parameters in request.")
    void shouldFindByShipmentId_SUCCESS() throws Exception {
        //GIVEN
        final String shipmentId = "030bc314-b6b0-4acc-8894-dd5f6891768b";
        //WHEN
        final MvcResult result = performGetRequest(SHIPMENTS + "/" + shipmentId);
        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @WithMockUser(roles = {"SHIPMENTS_VIEW"})
    @DisplayName("Given a User with SHIPMENTS_VIEW role, is NOT allowed to find a Shipment with an invalid ID parameters in request.")
    void shouldFindByShipmentId_FAILED() throws Exception {
        //GIVEN
        final String shipmentId = "INVALID_ID_BLBAT2JFZN4Y43"; // SHOULD BE UUID
        //WHEN
        final MvcResult result = performGetRequest(SHIPMENTS + "/" + shipmentId);
        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @WithMockUser(roles = {"SHIPMENTS_VIEW"})
    @DisplayName("Given a User with SHIPMENTS_VIEW role, is allowed to find a Shipment with a valid Tracking ID parameters in request.")
    void shouldFindByShipmentTrackingId_SUCCESS() throws Exception {
        //GIVEN
        final String trackingId = "Some_Valid_Tracking_ID_030bc314-b6b0-4acc-8894-dd5f6891768b";
        //WHEN
        final MvcResult result = performGetRequest(SHIPMENTS + "?shipment_tracking_id=" + trackingId);
        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @WithMockUser(roles = {"SHIPMENTS_VIEW"})
    @DisplayName("Given a User with SHIPMENTS_VIEW role, is not allowed to find a Shipment with an invalid Tracking ID parameters in request.")
    void shouldFindByShipmentTrackingId_FAILED() throws Exception {
        //GIVEN
        final String trackingId = "Some_Long_invalid_Tracking_ID_BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5";
        //WHEN
        final MvcResult result = performGetRequest(SHIPMENTS + "?shipment_tracking_id=" + trackingId);
        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @WithMockUser(roles = {"SHIPMENTS_VIEW"})
    @DisplayName("Given a User with SHIPMENTS_VIEW role, is allowed to find a Shipment Journey with a valid ID parameters in request.")
    void shouldFindJourneyByShipmentId_SUCCESS() throws Exception {
        //GIVEN
        final String shipmentId = "030bc314-b6b0-4acc-8894-dd5f6891768b";
        //WHEN
        Shipment shipment = new Shipment();
        Mockito.when(shipmentApi.findAndCheckLocationPermission(shipmentId)).thenReturn(shipment);
        final MvcResult result = performGetRequest(SHIPMENTS + "/" + shipmentId + "/shipment_journey");
        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @WithMockUser(roles = {"SHIPMENTS_VIEW"})
    @DisplayName("Given a User with SHIPMENTS_VIEW role, is NOT allowed to find a Shipment Journey with an invalid ID parameters in request.")
    void shouldFindJourneyByShipmentId_FAILED() throws Exception {
        //GIVEN
        final String shipmentId = "INVALID_ID_BLBAT2JFZN4Y43"; // SHOULD BE UUID
        //WHEN
        final MvcResult result = performGetRequest(SHIPMENTS + "/" + shipmentId + "/shipment_journey");
        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @WithMockUser(roles = {"SHIPMENTS_VIEW"})
    @DisplayName("Given a User with SHIPMENTS_VIEW role, is allowed to find a Shipment Package Dimension with a valid Tracking ID parameters in request.")
    void shouldFindPackageDimensionByShipmentTrackingId_SUCCESS() throws Exception {
        //GIVEN
        final String trackingId = "Some_Valid_Tracking_ID_030bc314-b6b0-4acc-8894-dd5f6891768b";
        //WHEN
        final MvcResult result = performGetRequest(SHIPMENTS + "/get-by-tracking-id/" + trackingId + "/package-dimension");
        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @WithMockUser(roles = {"SHIPMENTS_VIEW"})
    @DisplayName("Given a User with SHIPMENTS_VIEW role, is not allowed to find a Shipment Package Dimension with an invalid Tracking ID parameters in request.")
    void shouldFindPackageDimensionByShipmentTrackingId_FAILED() throws Exception {
        //GIVEN
        final String trackingId = "Some_Long_invalid_Tracking_ID_BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5";
        //WHEN
        final MvcResult result = performGetRequest(SHIPMENTS + "/get-by-tracking-id/" + trackingId + "/package-dimension");
        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    private Request<Shipment> createValidShipmentRequest2() throws JsonProcessingException {
        JsonNode data = TEST_UTIL.getDataFromFile("samplepayload/request/createSingleShipment.json");
        Request<Shipment> request = new Request<>();
        request.setData(objectMapper.readValue(data.get("data").toString(), Shipment.class));
        return request;
    }

    private Request<List<Shipment>> createValidShipmentsRequest() throws JsonProcessingException {
        JsonNode data = TEST_UTIL.getDataFromFile("samplepayload/request/createSingleShipment.json");
        Request<List<Shipment>> request = new Request<>();
        List<Shipment> shipments = new ArrayList<>(1);
        shipments.add(objectMapper.readValue(data.get("data").toString(), Shipment.class));
        request.setData(shipments);
        return request;
    }

    private Request<Shipment> createInvalidShipmentRequest() throws JsonProcessingException {
        JsonNode data = TEST_UTIL.getDataFromFile("samplepayload/request/createSingleShipment_FieldsInvalid.json");
        Request<Shipment> request = new Request<>();
        request.setData(objectMapper.readValue(data.get("data").toString(), Shipment.class));
        return request;
    }

    private Request<List<Shipment>> createInValidShipmentsRequest() throws JsonProcessingException {
        JsonNode data = TEST_UTIL.getDataFromFile("samplepayload/request/createSingleShipment_FieldsInvalid.json");
        Request<List<Shipment>> request = new Request<>();
        List<Shipment> shipments = new ArrayList<>(1);
        shipments.add(objectMapper.readValue(data.get("data").toString(), Shipment.class));
        request.setData(shipments);
        return request;
    }


    private List<FieldError> shipmentFieldErrors() {
        return buildErrorList(
                new FieldError("data.notes", "Maximum of 2000 characters allowed."),
                new FieldError("data.shipment_package.pricing_info.currency", "size must be between 0 and 64"),
                new FieldError("data.partner_id", "Maximum of 48 characters allowed."),
                new FieldError("data.customer.name", "size must be between 0 and 64"),
                new FieldError("data.shipment_package.commodities[1].value", "numeric value out of bounds (<15 digits>.<4 digits> expected)"),
                new FieldError("data.shipment_package.dimension.length", "numeric value out of bounds (<15 digits>.<3 digits> expected)"),
                new FieldError("data.shipment_package.commodities[1].quantity", "numeric value out of bounds (<10 digits>.<0 digits> expected)"),
                new FieldError("data.sender.contact_number", "Must be maximum of 64 characters."),
                new FieldError("data.description", "Maximum of 255 characters allowed."),
                new FieldError("data.service_type.code", "Must be maximum of 64 characters"),
                new FieldError("data.sender.name", "Must be maximum of 128 characters."),
                new FieldError("data.shipment_package.commodities[0].name", "Must be maximum of 45 characters."),
                new FieldError("data.shipment_package.dimension.volume_weight", "numeric value out of bounds (<15 digits>.<3 digits> expected)"),
                new FieldError("data.order.status", "Maximum of 50 characters allowed."),
                new FieldError("data.consignee.email", "Must be maximum of 64 characters."),
                new FieldError("data.internal_order_id", "Maximum of 64 characters allowed."),
                new FieldError("data.shipment_package.pricing_info.insurance_charge", "numeric value out of bounds (<14 digits>.<3 digits> expected)"),
                new FieldError("data.order_id", "Maximum of 48 characters allowed."),
                new FieldError("data.shipment_package.commodities[1].external_id", "must be a valid UUIDv4 format"),
                new FieldError("data.shipment_package.dimension.width", "numeric value out of bounds (<15 digits>.<3 digits> expected)"),
                new FieldError("data.shipment_journey.order_id", "must be a valid UUIDv4 format"),
                new FieldError("data.shipment_package.pricing_info.total", "numeric value out of bounds (<14 digits>.<3 digits> expected)"),
                new FieldError("data.shipment_package.dimension.chargeable_weight", "numeric value out of bounds (<15 digits>.<3 digits> expected)"),
                new FieldError("data.instructions[0].source", "size must be between 0 and 256"),
                new FieldError("data.shipment_package.pricing_info.surcharge", "numeric value out of bounds (<14 digits>.<3 digits> expected)"),
                new FieldError("data.shipment_package.pricing_info.base_tariff", "numeric value out of bounds (<14 digits>.<3 digits> expected)"),
                new FieldError("data.shipment_package.pricing_info.service_type_charge", "numeric value out of bounds (<14 digits>.<3 digits> expected)"),
                new FieldError("data.shipment_package.total_value", "numeric value out of bounds (<16 digits>.<4 digits> expected)"),
                new FieldError("data.consignee.contact_number", "Must be maximum of 64 characters."),
                new FieldError("data.order.notes", "Must be maximum of 2000 characters."),
                new FieldError("data.shipment_package.pricing_info.tax", "numeric value out of bounds (<14 digits>.<3 digits> expected)"),
                new FieldError("data.instructions[0].label", "size must be between 0 and 256"),
                new FieldError("data.external_order_id", "Maximum of 64 characters allowed."),
                new FieldError("data.shipment_package.commodities[0].code", "Must be maximum of 45 characters."),
                new FieldError("data.shipment_journey.shipment_id", "must be a valid UUIDv4 format"),
                new FieldError("data.return_location", "Maximum of 256 characters allowed."),
                new FieldError("data.shipment_package.dimension.gross_weight", "numeric value out of bounds (<15 digits>.<3 digits> expected)"),
                new FieldError("data.shipment_package.type_ref_id", "Must be maximum of 48 characters."),
                new FieldError("data.shipment_package.currency", "Must be maximum of 4 characters."),
                new FieldError("data.shipment_package.type", "Must be maximum of 45 characters."),
                new FieldError("data.shipment_package.pricing_info.discount", "numeric value out of bounds (<14 digits>.<3 digits> expected)"),
                new FieldError("data.order.order_id_label", "Maximum of 64 characters allowed."),
                new FieldError("data.shipment_package.commodities[1].packaging_type", "Must be maximum of 45 characters."),
                new FieldError("data.consignee.name", "Must be maximum of 128 characters."),
                new FieldError("data.instructions[0].value", "Must be maximum of 4000 characters."),
                new FieldError("data.user_id", "must be a valid UUIDv4 format"),
                new FieldError("data.sender.email", "Must be maximum of 64 characters."),
                new FieldError("data.delivery_location", "Maximum of 256 characters allowed."),
                new FieldError("data.customer.id", "must be a valid UUIDv4 format"),
                new FieldError("data.instructions[0].id", "must be a valid UUIDv4 format"),
                new FieldError("data.shipment_journey.journey_id", "must be a valid UUIDv4 format"),
                new FieldError("data.shipment_package.pricing_info.external_id", "must be a valid UUIDv4 format"),
                new FieldError("data.service_type.name", "Must be maximum of 128 characters"),
                new FieldError("data.shipment_package.pricing_info.extra_care_charge", "numeric value out of bounds (<14 digits>.<3 digits> expected)"),
                new FieldError("data.shipment_package.total_items_count", "must be less than or equal to 2147483647"),
                new FieldError("data.shipment_journey.package_journey_segments[0].journey_id", "size must be between 0 and 48"),
                new FieldError("data.shipment_package.dimension.height", "numeric value out of bounds (<15 digits>.<3 digits> expected)"),
                new FieldError("data.order.group", "Maximum of 32 characters allowed."),
                new FieldError("data.customer_order_id", "Maximum of 64 characters allowed."),
                new FieldError("data.pick_up_location", "Maximum of 256 characters allowed."),
                new FieldError("data.shipment_package.value", "Must be maximum of 45 characters"),
                new FieldError("data.shipment_package.commodities[0].hs_code", "Must be maximum of 45 characters."),
                new FieldError("data.shipment_tracking_id", "Maximum of 48 characters allowed."),
                new FieldError("data.shipment_package.pricing_info.cod", "numeric value out of bounds (<14 digits>.<3 digits> expected)"),
                new FieldError("data.shipment_journey.package_journey_segments[0].ops_type", "size must be between 0 and 45"),
                new FieldError("data.customer.code", "size must be between 0 and 64")
        );
    }

    private List<QuincusFieldError> getExpectedQuincusFieldErrors() {
        return Arrays.asList(
                new QuincusFieldError("data[0].consignee.email", "Must be maximum of 64 characters.", "Size", "michaeljordan_BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5_BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5@example.com"),
                new QuincusFieldError("data[0].shipment_package.pricing_info.discount", "numeric value out of bounds (<14 digits>.<3 digits> expected)", "Digits", "2.0E+16"),
                new QuincusFieldError("data[0].shipment_journey.package_journey_segments[0].journey_id", "size must be between 0 and 48", "Size", "EX-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].shipment_package.pricing_info.insurance_charge", "numeric value out of bounds (<14 digits>.<3 digits> expected)", "Digits", "2.0E+16"),
                new QuincusFieldError("data[0].instructions[0].label", "size must be between 0 and 256", "Size", "label-EX-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].shipment_package.pricing_info.service_type_charge", "numeric value out of bounds (<14 digits>.<3 digits> expected)", "Digits", "2.0E+16"),
                new QuincusFieldError("data[0].shipment_package.dimension.width", "numeric value out of bounds (<15 digits>.<3 digits> expected)", "Digits", "2.0E+16"),
                new QuincusFieldError("data[0].shipment_package.dimension.gross_weight", "numeric value out of bounds (<15 digits>.<3 digits> expected)", "Digits", "2.0E+16"),
                new QuincusFieldError("data[0].shipment_package.pricing_info.tax", "numeric value out of bounds (<14 digits>.<3 digits> expected)", "Digits", "2.0E+16"),
                new QuincusFieldError("data[0].service_type.name", "Must be maximum of 128 characters", "Size", "name-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].consignee.contact_number", "Must be maximum of 64 characters.", "Size", "202-555-0193-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].customer.code", "size must be between 0 and 64", "Size", "EX-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].shipment_package.pricing_info.base_tariff", "numeric value out of bounds (<14 digits>.<3 digits> expected)", "Digits", "2.0E+16"),
                new QuincusFieldError("data[0].service_type.code", "Must be maximum of 64 characters", "Size", "code-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].delivery_location", "Maximum of 256 characters allowed.", "Size", "EX-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].pick_up_location", "Maximum of 256 characters allowed.", "Size", "EX-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].shipment_journey.shipment_id", "must be a valid UUIDv4 format", "UUID", "EX-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].shipment_package.dimension.volume_weight", "numeric value out of bounds (<15 digits>.<3 digits> expected)", "Digits", "2.0E+16"),
                new QuincusFieldError("data[0].return_location", "Maximum of 256 characters allowed.", "Size", "EX-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].description", "Maximum of 255 characters allowed.", "Size", "EX-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].shipment_package.pricing_info.extra_care_charge", "numeric value out of bounds (<14 digits>.<3 digits> expected)", "Digits", "2.0E+16"),
                new QuincusFieldError("data[0].order.status", "Maximum of 50 characters allowed.", "Size", "Invalid-Long-Status-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-6JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].shipment_package.total_items_count", "must be less than or equal to 2147483647", "Max", "9223372036854775"),
                new QuincusFieldError("data[0].shipment_journey.journey_id", "must be a valid UUIDv4 format", "UUID", "EX-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].instructions[0].source", "size must be between 0 and 256", "Size", "order-EX-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].shipment_package.dimension.height", "numeric value out of bounds (<15 digits>.<3 digits> expected)", "Digits", "2.0E+16"),
                new QuincusFieldError("data[0].shipment_tracking_id", "Maximum of 48 characters allowed.", "Size", "EX-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-6JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].shipment_package.pricing_info.cod", "numeric value out of bounds (<14 digits>.<3 digits> expected)", "Digits", "2.0E+16"),
                new QuincusFieldError("data[0].user_id", "must be a valid UUIDv4 format", "UUID", "EX-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-6JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].shipment_package.currency", "Must be maximum of 4 characters.", "Size", "PHP-EXTRA"),
                new QuincusFieldError("data[0].shipment_package.total_value", "numeric value out of bounds (<16 digits>.<4 digits> expected)", "Digits", "2.0E+16"),
                new QuincusFieldError("data[0].shipment_package.commodities[0].name", "Must be maximum of 45 characters.", "Size", "Food-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-6JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].shipment_journey.package_journey_segments[0].ops_type", "size must be between 0 and 45", "Size", "EX-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-6JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].shipment_package.type_ref_id", "Must be maximum of 48 characters.", "Size", "type-id-1-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-6JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].shipment_package.commodities[1].quantity", "numeric value out of bounds (<10 digits>.<0 digits> expected)", "Digits", "123456789000"),
                new QuincusFieldError("data[0].internal_order_id", "Maximum of 64 characters allowed.", "Size", "EX-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].order.group", "Maximum of 32 characters allowed.", "Size", "EX-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].sender.email", "Must be maximum of 64 characters.", "Size", "tombrady_EX_BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5_BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5@example.com"),
                new QuincusFieldError("data[0].order.order_id_label", "Maximum of 64 characters allowed.", "Size", "EX-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].shipment_package.commodities[0].code", "Must be maximum of 45 characters.", "Size", "EX-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-6JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].notes", "Maximum of 2000 characters allowed.", "Size", "MoreThan-2K-LongNote-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"),
                new QuincusFieldError("data[0].shipment_package.pricing_info.surcharge", "numeric value out of bounds (<14 digits>.<3 digits> expected)", "Digits", "2.0E+16"),
                new QuincusFieldError("data[0].shipment_package.dimension.chargeable_weight", "numeric value out of bounds (<15 digits>.<3 digits> expected)", "Digits", "2.0E+16"),
                new QuincusFieldError("data[0].shipment_package.value", "Must be maximum of 45 characters", "Size", "EX-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-6JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].external_order_id", "Maximum of 64 characters allowed.", "Size", "EX-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].sender.name", "Must be maximum of 128 characters.", "Size", "Tom Brady-EX-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].shipment_package.commodities[0].hs_code", "Must be maximum of 45 characters.", "Size", "EX-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-6JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].order_id", "Maximum of 48 characters allowed.", "Size", "EX-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-6JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].shipment_package.type", "Must be maximum of 45 characters.", "Size", "packageType-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-6JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].shipment_package.pricing_info.total", "numeric value out of bounds (<14 digits>.<3 digits> expected)", "Digits", "2.0E+16"),
                new QuincusFieldError("data[0].partner_id", "Maximum of 48 characters allowed.", "Size", "EX-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-6JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].consignee.name", "Must be maximum of 128 characters.", "Size", "Michael Jordan-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].shipment_package.pricing_info.currency", "size must be between 0 and 64", "Size", "PHP-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].shipment_journey.order_id", "must be a valid UUIDv4 format", "UUID", "EX-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].order.notes", "Must be maximum of 2000 characters.", "Size", "MoreThan-2K-LongNote-nVLrzwrkSAEUVxAc5UY1EQUgrM3IFIFkdCaeL1uCEyRByNCVE7l4b5GPr1bRxUehp19f4YeAOjUhVC9eRmyXQgQfPzQspECZ083BogetI1zOYxg5Bbu0e6sixGH6XtR9E3l7aZvqzVm6T3HjI87aEYeA5Qmr6eROmWDekYuYdNGFwoMQCjZoAfmoII8Zap3dHZUNK4lzjLRWNxQkXfyKE1rZAk9n66fpsLU6td726KMr3ZmadAOK2lgpMEKvMkTIY06itl9KhplTNqyUaW7QRWAQMFBMJzl7sWbfx4XnoEl2fEav7uZpKY3qvVPTmDAuFyZxEl2y4QGWQpt1NT6Ano2fi5KUL9mWlRYwn8FkAptrOXGRu3mikd1lbqNM9ZXCSEBoZYWxuwRi7UTRSii1fVRim69zOn9LX3PqUlwVcJRnEQaZ6azezIElnKOdZjGlksvqzw2FEYzm9axcauu4mOnlX72ofrRLhjooIAx8Y8rPvUcKuB9L7xTfJByVGUfulEe5kcYIsSP0Of25yA9RkXOoclplhnyHBhh7KlQRFYd1d7dkO6FheCymjRCCRyp68kTWMeNa5VoD3siFpyoUatFiA3jrGTCRZJDsOCMPAAg15QgQT7zdKSgordRmTXLwVJp12nz9APcO7kqfg0YW7lCLnl06mJoGWyHs3pzZGFDYjZZqVjJJ4mmrBccSu9slU21E9S31W6iLoolygKoTPSUxeesehGbe9DO5Oft7Cg8KMr67wbkOQaeAN0BQ0SR67ItZCFuWYa7k7KYJuPEOPWK0MX837g9PSamddu6EipkiT0y2cqTRhGSkXWbYdG40a8tPSTPG9ZFdb7DMTlVhMKT38d311XWtQ6tcDWZSJpcKFXhChCPnGCvRxSrYBD0TwzZKwn38UIYOztlfPZowdsNVFvZFbsfQyTaUG7Q0LQJfIGUp9ImRVZcRhhbchXtr3beXB7CsrJP9zWy6nOpTasFYnnyZzOYzhNc8fVVgNFMIbPBMHH5eQC9wBz3Kq06PijhZ2gdfyA6ZCFODY74WpSkBb1tNiF1bCx45KMjuYJOUM86cXgk3aAqMcnQlCvtKfNssDsjwnPIQ1XNvchORMGxMaAmc1XhV4BZOm1HRjiIAHOHm47qJj3uyCilRYosf3geuPwSalCZhMf5NPVrF2wrRzf120grnmcmeIE7rDZL83E7cmgT7S7waxHnNKuSgQAydFuZoH0tQqlycCxSOC3uz6iujMVHVCa7tgrznxP4Qzz2UI0SazXGooe9BnBDha5RR68rOUKgyJadfS5Z7jn6iTLyOFXDsofIA5Ua9t06GJApcJpvPt6p85QkOgRag2cEGbX0lkGrY2avGKMnp4Q0bMsP19e8YU9fYEtDgqC1DOeAVOJRhAYusQyzA3mK6IrOuA3FmLyY3phGnoqwcWpuVtpp9ErYXnSQj7tNcUNBzTBdYYnzOAzjXgtWeJNuBgdNNxWHpY0Yuw04AR1Da0MhNQUiptogGQArROCEwsyI8vwFyLlgbqR8946TI1CPlGGL7CQesM3UCBfEj527z7C7VvwGvMh4jRRvDPRSa8IPr7pjpukdPplJFRlMxX07J3J8pZQqMfbrCey4i20Gmpk39B1xHU3DXX0Jp8IF1OE7aXP6jfkwDLjRb7WScG3lm5Pea0zdZRnjqAB4rrUupX9AlVxlYs5Uhusa9kCpqicqlEeGppKipYqqwzfkmvFPohhALf7QIORMwh9aOZuV3IpHzcT6XsP8fP7AsJxEQWTVgwPyipTmqwl9am1fE9vaLWNgjLfITX86zS4Y4qaeHVsIv3mIJ3WcR5Iw58vK3kPdtKtBfT7vLsu7SDU4BKYNueSNOW402jQNiopkWP22KidjMnPpboQMhvbeYNBlSXLWUr699NSgPrgeYQFQACXVTLF7BCpIJCb88rsJ0DX1agJ3j4srFl1z1TSWDKh8hWR6YUjgef4pYeqZesVWMCOguOkWwOlUYJSPVRF7iHqbICSSNAWkb1RpBoYBdK1IAVMbcVR8k5U4o2lifriZtUvac3p"),
                new QuincusFieldError("data[0].customer_order_id", "Maximum of 64 characters allowed.", "Size", "EX-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].shipment_package.commodities[1].value", "numeric value out of bounds (<15 digits>.<4 digits> expected)", "Digits", "2.0E+16"),
                new QuincusFieldError("data[0].shipment_package.commodities[1].packaging_type", "Must be maximum of 45 characters.", "Size", "EX-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-6JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].sender.contact_number", "Must be maximum of 64 characters.", "Size", "202-555-0166-EX-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].customer.id", "must be a valid UUIDv4 format", "UUID", "NOT_VALID_UUID"),
                new QuincusFieldError("data[0].customer.name", "size must be between 0 and 64", "Size", "EX-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5"),
                new QuincusFieldError("data[0].shipment_package.dimension.length", "numeric value out of bounds (<15 digits>.<3 digits> expected)", "Digits", "2.0E+16")
        );
    }

}
