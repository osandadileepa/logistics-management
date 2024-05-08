package com.quincus.apigateway.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.quincus.apigateway.api.ApiGatewayWebhookClient;
import com.quincus.apigateway.test_utils.TestUtil;
import com.quincus.apigateway.web.model.ApiGatewayCheckInRequest;
import com.quincus.apigateway.web.model.ApiGatewayUpdateOrderAdditionalChargesRequest;
import com.quincus.apigateway.web.model.ApiGatewayUpdateOrderProgressRequest;
import com.quincus.apigateway.web.model.ApiGatewayWebhookResponse;
import com.quincus.qportal.api.QPortalApi;
import com.quincus.qportal.model.QPortalPartner;
import com.quincus.qportal.model.QPortalUser;
import com.quincus.shipment.api.PartnerApi;
import com.quincus.shipment.api.ShipmentFetchApi;
import com.quincus.shipment.api.constant.MilestoneCode;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.domain.Cost;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Partner;
import com.quincus.shipment.api.domain.PricingInfo;
import com.quincus.shipment.api.domain.Shipment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiGatewayServiceTest {
    private final TestUtil testUtil = TestUtil.getInstance();
    @InjectMocks
    private ApiGatewayService apiGatewayService;
    @Mock
    private ApiGatewayWebhookClient apiGatewayWebHookClient;
    @Mock
    private PartnerApi partnerApi;
    @Mock
    private ShipmentFetchApi shipmentFetchApi;
    @Mock
    private QPortalApi qPortalApi;

    @Mock
    private AssignToVendorService assignToVendorService;

    @Test
    void sendUpdateOrderProgress_withValidMilestone_shouldHaveNoErrors() {
        Milestone milestone = testUtil.createMilestone();
        Shipment shipment = testUtil.createShipment();
        JsonNode jsonResponse = testUtil.getUpdateOrderProgressResponseJson();
        ApiGatewayWebhookResponse mockResponse = testUtil.getObjectMapper().convertValue(jsonResponse, ApiGatewayWebhookResponse.class);

        when(apiGatewayWebHookClient.updateOrderProgress(anyString(), any(ApiGatewayUpdateOrderProgressRequest.class)))
                .thenReturn(mockResponse);

        ApiGatewayWebhookResponse response = apiGatewayService.sendUpdateOrderProgress(shipment, milestone);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(mockResponse.getStatus());
        assertThat(response.getMessage()).isEqualTo(mockResponse.getMessage());
        verify(apiGatewayWebHookClient, times(1))
                .updateOrderProgress(anyString(), any(ApiGatewayUpdateOrderProgressRequest.class));
    }

    @Test
    void sendUpdateOrderProgress_withValidMilestoneAndNoExternalAndInternalOrderId_shouldNotSendToWebhook() {
        Milestone milestone = testUtil.createMilestone();
        Shipment shipment = testUtil.createShipment();
        shipment.setExternalOrderId(null);
        shipment.getOrder().setOrderIdLabel(null);

        ApiGatewayWebhookResponse response = apiGatewayService.sendUpdateOrderProgress(shipment, milestone);

        assertThat(response).isNull();
        verify(apiGatewayWebHookClient, never())
                .updateOrderProgress(anyString(), any(ApiGatewayUpdateOrderProgressRequest.class));
    }

    @Test
    void sendAssignVendorDetails_withValidShipmentAndMilestone() {
        Milestone milestone = testUtil.createMilestone();
        Shipment shipment = testUtil.createShipment();

        when(assignToVendorService.sendAssignVendorDetails(any(Shipment.class), any(Milestone.class))).thenReturn(new ApiGatewayWebhookResponse());

        apiGatewayService.sendAssignVendorDetails(shipment, milestone);

        verify(assignToVendorService, times(1)).sendAssignVendorDetails(any(Shipment.class), any(Milestone.class));
    }

    @Test
    void sendAssignVendorDetails_withValidShipmentAndPackageJourneySegment() {
        Shipment shipment = testUtil.createShipment();

        when(assignToVendorService.sendAssignVendorDetails(any(Shipment.class), any(PackageJourneySegment.class))).thenReturn(new ApiGatewayWebhookResponse());

        apiGatewayService.sendAssignVendorDetails(shipment, shipment.getShipmentJourney().getPackageJourneySegments().get(0));

        verify(assignToVendorService, times(1)).sendAssignVendorDetails(any(Shipment.class), any(PackageJourneySegment.class));
    }

    @Test
    void sendUpdateOrderProgress_withValidMilestoneAndNoExternalOrderIdButHasInternalOrderId_shouldSendToWebhook() {
        Milestone milestone = testUtil.createMilestone();
        Shipment shipment = testUtil.createShipment();
        //internal orderId is already set from createShipment `shipment.getOrder().getOrderIdLabel()`
        shipment.setExternalOrderId(null);

        ApiGatewayWebhookResponse response = apiGatewayService.sendUpdateOrderProgress(shipment, milestone);

        assertThat(response).isNull();
        verify(apiGatewayWebHookClient, times(1))
                .updateOrderProgress(anyString(), any(ApiGatewayUpdateOrderProgressRequest.class));
    }

    @Test
    void sendUpdateOrderProgress_withValidMilestoneAndNoInternalOrderIdButHasExternalOrderId_shouldSendToWebhook() {
        Milestone milestone = testUtil.createMilestone();
        Shipment shipment = testUtil.createShipment();
        shipment.getOrder().setOrderIdLabel(null);
        shipment.setExternalOrderId(UUID.randomUUID().toString());

        ApiGatewayWebhookResponse response = apiGatewayService.sendUpdateOrderProgress(shipment, milestone);

        assertThat(response).isNull();
        verify(apiGatewayWebHookClient, times(1))
                .updateOrderProgress(anyString(), any(ApiGatewayUpdateOrderProgressRequest.class));
    }

    @Test
    void sendUpdateOrderProgress_withSegmentAndValidMilestone_shouldHaveNoErrors() {
        Milestone milestone = testUtil.createMilestone();
        milestone.setToLocationId("");
        milestone.setToCityId("toCityId");
        Shipment shipment = testUtil.createShipment();
        PackageJourneySegment segment = shipment.getShipmentJourney().getPackageJourneySegments().get(0);
        JsonNode jsonResponse = testUtil.getUpdateOrderProgressResponseJson();
        ApiGatewayWebhookResponse mockResponse = testUtil.getObjectMapper().convertValue(jsonResponse, ApiGatewayWebhookResponse.class);

        when(apiGatewayWebHookClient.updateOrderProgress(anyString(), any(ApiGatewayUpdateOrderProgressRequest.class)))
                .thenReturn(mockResponse);

        ApiGatewayWebhookResponse response = apiGatewayService.sendUpdateOrderProgress(shipment, segment, milestone);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(mockResponse.getStatus());
        assertThat(response.getMessage()).isEqualTo(mockResponse.getMessage());
        verify(apiGatewayWebHookClient, times(1))
                .updateOrderProgress(anyString(), any(ApiGatewayUpdateOrderProgressRequest.class));
    }

    @Test
    void sendUpdateOrderProgress_withSegmentValidMilestoneAndNoExternalAndInternalOrderId_shouldNotSendToWebhook() {
        Milestone milestone = testUtil.createMilestone();
        Shipment shipment = testUtil.createShipment();
        shipment.setExternalOrderId(null);
        shipment.getOrder().setOrderIdLabel(null);
        PackageJourneySegment segment = shipment.getShipmentJourney().getPackageJourneySegments().get(0);

        ApiGatewayWebhookResponse response = apiGatewayService.sendUpdateOrderProgress(shipment, segment, milestone);

        assertThat(response).isNull();
        verify(apiGatewayWebHookClient, never())
                .updateOrderProgress(anyString(), any(ApiGatewayUpdateOrderProgressRequest.class));
    }

    @Test
    void sendUpdateOrderProgress_withSegmentValidMilestoneAndNoExternalOrderIdButHasInternalOrderId_shouldSendToWebhook() {
        Milestone milestone = testUtil.createMilestone();
        Shipment shipment = testUtil.createShipment();
        //internal orderId is already set from createShipment `shipment.getOrder().getOrderIdLabel()`
        shipment.setExternalOrderId(null);

        PackageJourneySegment segment = shipment.getShipmentJourney().getPackageJourneySegments().get(0);

        ApiGatewayWebhookResponse response = apiGatewayService.sendUpdateOrderProgress(shipment, segment, milestone);

        assertThat(response).isNull();
        verify(apiGatewayWebHookClient, times(1))
                .updateOrderProgress(anyString(), any(ApiGatewayUpdateOrderProgressRequest.class));
    }

    @Test
    void sendUpdateOrderProgress_withSegmentValidMilestoneAndNoInternalOrderIdButHasExternalOrderId_shouldSendToWebhook() {
        Milestone milestone = testUtil.createMilestone();
        Shipment shipment = testUtil.createShipment();
        shipment.getOrder().setOrderIdLabel(null);
        shipment.setExternalOrderId(UUID.randomUUID().toString());

        PackageJourneySegment segment = shipment.getShipmentJourney().getPackageJourneySegments().get(0);

        ApiGatewayWebhookResponse response = apiGatewayService.sendUpdateOrderProgress(shipment, segment, milestone);

        assertThat(response).isNull();
        verify(apiGatewayWebHookClient, times(1))
                .updateOrderProgress(anyString(), any(ApiGatewayUpdateOrderProgressRequest.class));
    }

    @Test
    void sendUpdateOrderAdditionalCharges_withValidCost_shouldHaveNoErrors() throws JsonProcessingException {
        Cost cost = testUtil.createCost();
        cost.getShipments().forEach(costShipment -> costShipment.setExternalOrderId("123"));
        JsonNode jsonResponse = testUtil.getUpdateOrderAdditionalChargesResponseJson();

        ApiGatewayWebhookResponse mockResponse = testUtil.getObjectMapper().convertValue(jsonResponse, ApiGatewayWebhookResponse.class);
        when(apiGatewayWebHookClient.updateOrderAdditionalCharges(anyString(), any(ApiGatewayUpdateOrderAdditionalChargesRequest.class)))
                .thenReturn(mockResponse);

        List<ApiGatewayWebhookResponse> response = apiGatewayService.sendUpdateOrderAdditionalCharges(cost);
        assertThat(response).isNotNull();

        ArgumentCaptor<ApiGatewayUpdateOrderAdditionalChargesRequest> webhookCaptor = ArgumentCaptor.forClass(ApiGatewayUpdateOrderAdditionalChargesRequest.class);

        verify(apiGatewayWebHookClient, times(2))
                .updateOrderAdditionalCharges(anyString(), webhookCaptor.capture());

        assertThat(response.get(0).getStatus()).isEqualTo(mockResponse.getStatus());
        assertThat(response.get(0).getMessage()).isEqualTo(mockResponse.getMessage());
        assertThat(response.get(1).getStatus()).isEqualTo(mockResponse.getStatus());
        assertThat(response.get(1).getMessage()).isEqualTo(mockResponse.getMessage());

        ApiGatewayUpdateOrderAdditionalChargesRequest webhookRequest = webhookCaptor.getValue();
        assertThat(webhookRequest.getOrderNo()).isEqualTo(cost.getShipments().get(0).getExternalOrderId());
    }

    @Test
    void sendUpdateOrderAdditionalCharges_withNoExternalIdButWithOrderIdLabel_shouldHaveNoErrors() throws JsonProcessingException {
        Cost cost = testUtil.createCost();
        cost.getShipments().forEach(costShipment -> costShipment.setOrderIdLabel("ABCD"));
        JsonNode jsonResponse = testUtil.getUpdateOrderAdditionalChargesResponseJson();

        ApiGatewayWebhookResponse mockResponse = testUtil.getObjectMapper().convertValue(jsonResponse, ApiGatewayWebhookResponse.class);
        when(apiGatewayWebHookClient.updateOrderAdditionalCharges(anyString(), any(ApiGatewayUpdateOrderAdditionalChargesRequest.class)))
                .thenReturn(mockResponse);

        List<ApiGatewayWebhookResponse> response = apiGatewayService.sendUpdateOrderAdditionalCharges(cost);
        assertThat(response).isNotNull();

        ApiGatewayUpdateOrderAdditionalChargesRequest shipment1AddChargeRequest = (ApiGatewayUpdateOrderAdditionalChargesRequest) response.get(0).getRequest();
        ApiGatewayUpdateOrderAdditionalChargesRequest shipment2AddChargeRequest = (ApiGatewayUpdateOrderAdditionalChargesRequest) response.get(1).getRequest();


        ArgumentCaptor<ApiGatewayUpdateOrderAdditionalChargesRequest> webhookCaptor = ArgumentCaptor.forClass(ApiGatewayUpdateOrderAdditionalChargesRequest.class);

        verify(apiGatewayWebHookClient, times(2))
                .updateOrderAdditionalCharges(anyString(), webhookCaptor.capture());

        assertThat(response.get(0).getStatus()).isEqualTo(mockResponse.getStatus());
        assertThat(response.get(0).getMessage()).isEqualTo(mockResponse.getMessage());
        assertThat(response.get(1).getStatus()).isEqualTo(mockResponse.getStatus());
        assertThat(response.get(1).getMessage()).isEqualTo(mockResponse.getMessage());

        ApiGatewayUpdateOrderAdditionalChargesRequest webhookRequest = webhookCaptor.getValue();
        assertThat(webhookRequest.getOrderNo()).isEqualTo(cost.getShipments().get(0).getOrderIdLabel());
    }


    @Test
    void sendUpdateOrderAdditionalCharges_shouldMapCostNameToChargeCode() throws JsonProcessingException {
        String costTypeName = "Fuel";
        Cost cost = testUtil.createCost();
        cost.getCostType().setName(costTypeName);

        cost.getShipments().forEach(costShipment -> costShipment.setOrderIdLabel("ABCD"));
        JsonNode jsonResponse = testUtil.getUpdateOrderAdditionalChargesResponseJson();

        ApiGatewayWebhookResponse mockResponse = testUtil.getObjectMapper().convertValue(jsonResponse, ApiGatewayWebhookResponse.class);
        when(apiGatewayWebHookClient.updateOrderAdditionalCharges(anyString(), any(ApiGatewayUpdateOrderAdditionalChargesRequest.class)))
                .thenReturn(mockResponse);

        List<ApiGatewayWebhookResponse> response = apiGatewayService.sendUpdateOrderAdditionalCharges(cost);
        assertThat(response).isNotNull();

        ApiGatewayUpdateOrderAdditionalChargesRequest shipment1AddChargeRequest = (ApiGatewayUpdateOrderAdditionalChargesRequest) response.get(0).getRequest();
        ApiGatewayUpdateOrderAdditionalChargesRequest shipment2AddChargeRequest = (ApiGatewayUpdateOrderAdditionalChargesRequest) response.get(1).getRequest();

        String shipment1AddChargeRequestChargeCode = shipment1AddChargeRequest.getAdditionalCharges().get(0).getChargeCode();
        String shipment2AddChargeRequestChargeCode = shipment2AddChargeRequest.getAdditionalCharges().get(0).getChargeCode();

        assertThat(shipment1AddChargeRequestChargeCode).isEqualTo(costTypeName);
        assertThat(shipment2AddChargeRequestChargeCode).isEqualTo(costTypeName);
    }

    @Test
    void sendUpdateOrderAdditionalCharges_withValidCostAndNoExternalOrderId_shouldNotSendToWebhook() throws JsonProcessingException {
        Cost cost = testUtil.createCost();
        cost.getShipments().forEach(costShipment -> costShipment.setExternalOrderId(null));

        List<ApiGatewayWebhookResponse> response = apiGatewayService.sendUpdateOrderAdditionalCharges(cost);

        assertThat(response).isEmpty();
        verify(apiGatewayWebHookClient, never())
                .updateOrderAdditionalCharges(anyString(), any(ApiGatewayUpdateOrderAdditionalChargesRequest.class));
    }

    @Test
    void sendCheckInDetails_withValidMilestone_shouldHaveNoErrors() {
        Milestone milestone = testUtil.createMilestone();
        Shipment shipment = testUtil.createShipment();
        JsonNode jsonResponse = testUtil.getCheckInDetailsResponseJson();
        ApiGatewayWebhookResponse mockResponse = testUtil.getObjectMapper().convertValue(jsonResponse, ApiGatewayWebhookResponse.class);
        when(apiGatewayWebHookClient.checkIn(anyString(), any(ApiGatewayCheckInRequest.class)))
                .thenReturn(mockResponse);

        ApiGatewayWebhookResponse response = apiGatewayService.sendCheckInDetails(shipment, milestone);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(mockResponse.getStatus());
        assertThat(response.getMessage()).isEqualTo(mockResponse.getMessage());

        ArgumentCaptor<ApiGatewayCheckInRequest> webhookCaptor = ArgumentCaptor.forClass(ApiGatewayCheckInRequest.class);

        verify(apiGatewayWebHookClient, times(1))
                .checkIn(anyString(), webhookCaptor.capture());

        ApiGatewayCheckInRequest webhookRequest = webhookCaptor.getValue();
        assertThat(webhookRequest.getOrderNo()).isEqualTo(shipment.getExternalOrderId());
    }

    @Test
    void sendCheckInDetails_withNoExternalIdButWithOrderIdLabel_shouldHaveNoErrors() {
        Milestone milestone = testUtil.createMilestone();
        Shipment shipment = testUtil.createShipment();
        shipment.setExternalOrderId(null);
        milestone.setExternalOrderId(shipment.getOrder().getOrderIdLabel());
        JsonNode jsonResponse = testUtil.getCheckInDetailsResponseJson();
        ApiGatewayWebhookResponse mockResponse = testUtil.getObjectMapper().convertValue(jsonResponse, ApiGatewayWebhookResponse.class);
        when(apiGatewayWebHookClient.checkIn(anyString(), any(ApiGatewayCheckInRequest.class)))
                .thenReturn(mockResponse);

        ApiGatewayWebhookResponse response = apiGatewayService.sendCheckInDetails(shipment, milestone);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(mockResponse.getStatus());
        assertThat(response.getMessage()).isEqualTo(mockResponse.getMessage());

        ArgumentCaptor<ApiGatewayCheckInRequest> webhookCaptor = ArgumentCaptor.forClass(ApiGatewayCheckInRequest.class);

        verify(apiGatewayWebHookClient, times(1))
                .checkIn(anyString(), webhookCaptor.capture());

        ApiGatewayCheckInRequest webhookRequest = webhookCaptor.getValue();
        assertThat(webhookRequest.getOrderNo()).isEqualTo(shipment.getOrder().getOrderIdLabel());
    }

    @Test
    void sendCheckInDetails_withValidMilestoneAndNoExternalOrderId_shouldNotSendToWebhook() {
        Milestone milestone = testUtil.createMilestone();
        Shipment shipment = testUtil.createShipment();
        shipment.setExternalOrderId(null);
        shipment.getOrder().setOrderIdLabel(null);

        ApiGatewayWebhookResponse response = apiGatewayService.sendCheckInDetails(shipment, milestone);

        assertThat(response).isNull();
        verify(apiGatewayWebHookClient, never())
                .checkIn(anyString(), any(ApiGatewayCheckInRequest.class));
    }

    @Test
    void sendCheckInDetails_withValidMilestoneAndSegment_shouldHaveNoErrors() {
        Milestone milestone = testUtil.createMilestone();
        Shipment shipment = testUtil.createShipment();
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId(milestone.getSegmentId());
        segment.setRefId("1");
        segment.setSequence("1");

        JsonNode jsonResponse = testUtil.getCheckInDetailsResponseJson();
        ApiGatewayWebhookResponse mockResponse = testUtil.getObjectMapper().convertValue(jsonResponse, ApiGatewayWebhookResponse.class);
        when(apiGatewayWebHookClient.checkIn(anyString(), any(ApiGatewayCheckInRequest.class)))
                .thenReturn(mockResponse);

        ApiGatewayWebhookResponse response = apiGatewayService.sendCheckInDetails(shipment, segment, milestone);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(mockResponse.getStatus());
        assertThat(response.getMessage()).isEqualTo(mockResponse.getMessage());
        verify(apiGatewayWebHookClient, times(1))
                .checkIn(anyString(), any(ApiGatewayCheckInRequest.class));
    }

    @Test
    void sendCheckInDetails_withValidMilestoneAndSegmentAndNoExternalOrderId_shouldNotSendToWebhook() {
        Milestone milestone = testUtil.createMilestone();
        Shipment shipment = testUtil.createShipment();
        shipment.setExternalOrderId(null);
        shipment.getOrder().setOrderIdLabel(null);
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId(milestone.getSegmentId());
        segment.setRefId("1");
        segment.setSequence("1");

        ApiGatewayWebhookResponse response = apiGatewayService.sendCheckInDetails(shipment, segment, milestone);

        assertThat(response).isNull();
        verify(apiGatewayWebHookClient, never())
                .checkIn(anyString(), any(ApiGatewayCheckInRequest.class));
    }

    @Test
    void sendCheckInDetails_withAirSegment_shouldReturnNull() {
        Milestone milestone = testUtil.createMilestone();
        Shipment shipment = testUtil.createShipment();

        shipment.getShipmentJourney().getPackageJourneySegments().get(0).setTransportType(TransportType.AIR);

        ApiGatewayWebhookResponse response = apiGatewayService.sendCheckInDetails(shipment, milestone);

        assertThat(response).isNull();
        verify(apiGatewayWebHookClient, never())
                .checkIn(anyString(), any(ApiGatewayCheckInRequest.class));
    }

    @Test
    void sendCheckInDetails_withAirSegmentAndNoExternalOrderId_shouldNotSendToWebhook() {
        Milestone milestone = testUtil.createMilestone();
        Shipment shipment = testUtil.createShipment();
        shipment.setExternalOrderId(null);

        shipment.getShipmentJourney().getPackageJourneySegments().get(0).setTransportType(TransportType.AIR);

        ApiGatewayWebhookResponse response = apiGatewayService.sendCheckInDetails(shipment, milestone);

        assertThat(response).isNull();
        verify(apiGatewayWebHookClient, never())
                .checkIn(anyString(), any(ApiGatewayCheckInRequest.class));
    }

    @Test
    void createUpdateOrderProgressRequest_withSegmentAndValidMilestone_shouldMapProperly() {
        Milestone milestone = testUtil.createMilestone();
        milestone.setToLocationId("");
        milestone.setToCityId("toCityId");
        milestone.setMilestoneCode(MilestoneCode.DSP_PICKUP_SUCCESSFUL);
        Shipment shipment = testUtil.createShipment();
        shipment.setShipmentReferenceId(List.of("1_1"));
        shipment.setDescription("This is a description");
        PackageJourneySegment segment = shipment.getShipmentJourney().getPackageJourneySegments().get(0);

        Shipment shipment2 = createTestShipmentWithMilestone("Milestone2", "1_2");
        Shipment shipment3 = createTestShipmentWithMilestone("milestone3", "2_1");
        Shipment shipment4 = createTestShipmentWithMilestone("milestone4", "33_1");

        Shipment shipment22 = createTestShipmentWithMilestone("Milestone2", "1_2");
        Shipment shipment33 = createTestShipmentWithMilestone("milestone3", "2_1");
        Shipment shipment44 = createTestShipmentWithMilestone("milestone4", "33_1");

        when(shipmentFetchApi.findAllShipmentsByOrderId(any())).thenReturn(List.of(shipment, shipment2, shipment3, shipment4, shipment22, shipment33, shipment44));

        ApiGatewayUpdateOrderProgressRequest result = apiGatewayService.createUpdateOrderProgressRequest(shipment, segment, milestone);

        assertThat(result.getOrderNo()).isEqualTo(shipment.getOrder().getOrderIdLabel());
        assertThat(result.getSegmentId()).isEqualTo(segment.getRefId());
        assertThat(result.getLocationInfo().getLocationId()).isEqualTo(milestone.getToCityId());
        assertThat(result.getPackages()).hasSize(4);

        assertPackageDetails(result, 0, "1", "1_1", milestone.getMilestoneCode().toString());
        assertPackageDetails(result, 1, "1", "1_2", milestone.getMilestoneCode().toString());
        assertPackageDetails(result, 2, "2", "2_1", milestone.getMilestoneCode().toString());
        assertPackageDetails(result, 3, "33", "33_1", milestone.getMilestoneCode().toString());

        milestone.setToLocationId("toLocationId");
        milestone.setToCityId("");

        result = apiGatewayService.createUpdateOrderProgressRequest(shipment, segment, milestone);
        assertThat(result.getLocationInfo().getLocationId()).isEqualTo(milestone.getToLocationId());
    }

    @Test
    void createUpdateOrderProgressRequest_withNoShipmentPackages_shouldMapProperly() {
        Milestone milestone = testUtil.createMilestone();
        milestone.setToLocationId("");
        milestone.setToCityId("toCityId");
        milestone.setMilestoneCode(MilestoneCode.DSP_PICKUP_SUCCESSFUL);
        Shipment shipment = testUtil.createShipment();
        shipment.setShipmentReferenceId(List.of("1_1"));
        shipment.setDescription("This is a description");
        PackageJourneySegment segment = shipment.getShipmentJourney().getPackageJourneySegments().get(0);

        when(shipmentFetchApi.findAllShipmentsByOrderId(any())).thenReturn(List.of());

        ApiGatewayUpdateOrderProgressRequest result = apiGatewayService.createUpdateOrderProgressRequest(shipment, segment, milestone);

        assertThat(result.getOrderNo()).isEqualTo(shipment.getOrder().getOrderIdLabel());
        assertThat(result.getSegmentId()).isEqualTo(segment.getRefId());
        assertThat(result.getLocationInfo().getLocationId()).isEqualTo(milestone.getToCityId());
        assertThat(result.getPackages()).isEmpty();

        milestone.setToLocationId("toLocationId");
        milestone.setToCityId("");

        result = apiGatewayService.createUpdateOrderProgressRequest(shipment, segment, milestone);
        assertThat(result.getLocationInfo().getLocationId()).isEqualTo(milestone.getToLocationId());
    }

    @Test
    void createUpdateOrderProgressRequest_withSegmentAndValidMilestone_shouldMapPicInfoFromMilestoneValues() {
        Milestone milestone = testUtil.createMilestone();
        milestone.setToLocationId("");
        milestone.setToCityId("toCityId");
        milestone.setMilestoneCode(MilestoneCode.DSP_PICKUP_SUCCESSFUL);

        Shipment shipment = testUtil.createShipment();
        shipment.setShipmentReferenceId(List.of("1_1"));
        shipment.setDescription("This is a description");
        PackageJourneySegment segment = shipment.getShipmentJourney().getPackageJourneySegments().get(0);

        Shipment shipment2 = createTestShipmentWithMilestone("Milestone2", "1_2");
        Shipment shipment3 = createTestShipmentWithMilestone("milestone3", "2_1");
        Shipment shipment4 = createTestShipmentWithMilestone("milestone4", "33_1");

        Partner partner = new Partner();
        partner.setCode("partnerCode");
        partner.setName("partnerName");

        when(shipmentFetchApi.findAllShipmentsByOrderId(any())).thenReturn(List.of(shipment, shipment2, shipment3, shipment4));
        when(partnerApi.findByIdAndOrganizationId(any(), any())).thenReturn(partner);

        ApiGatewayUpdateOrderProgressRequest result = apiGatewayService.createUpdateOrderProgressRequest(shipment, segment, milestone);

        assertThat(result.getPicInfo().getVendorCode()).isEqualTo(partner.getCode());
        assertThat(result.getPicInfo().getVendorName()).isEqualTo(partner.getName());
        assertThat(result.getPicInfo().getDriverName()).isEqualTo(milestone.getDriverName());
        assertThat(result.getPicInfo().getDriverPhoneNo()).isEqualTo(milestone.getDriverPhoneNumber());
        assertThat(result.getPicInfo().getUserName()).isEqualTo(milestone.getUserName());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void createUpdateOrderProgressRequest_withSegmentAndValidMilestone_shouldMapPicInfoFromQPortalValues(String emptyValues) {
        Milestone milestone = testUtil.createMilestone();
        milestone.setToLocationId("");
        milestone.setToCityId("toCityId");
        milestone.setMilestoneCode(MilestoneCode.DSP_PICKUP_SUCCESSFUL);
        milestone.setUserName(emptyValues);
        milestone.setDriverName(emptyValues);
        milestone.setDriverPhoneNumber(emptyValues);
        milestone.setDriverId("UserId");

        Shipment shipment = testUtil.createShipment();
        shipment.setShipmentReferenceId(List.of("1_1"));
        shipment.setDescription("This is a description");
        PackageJourneySegment segment = shipment.getShipmentJourney().getPackageJourneySegments().get(0);

        Shipment shipment2 = createTestShipmentWithMilestone("Milestone2", "1_2");
        Shipment shipment3 = createTestShipmentWithMilestone("milestone3", "2_1");
        Shipment shipment4 = createTestShipmentWithMilestone("milestone4", "33_1");

        QPortalPartner qPortalPartner = new QPortalPartner();
        qPortalPartner.setPartnerCode("code");
        qPortalPartner.setName("name");

        QPortalUser qPortalUser = new QPortalUser();
        qPortalUser.setFirstName("firstName");
        qPortalUser.setLastName("lastName");
        qPortalUser.setMobileNo("mobileNo");
        qPortalUser.setUsername("username");

        when(shipmentFetchApi.findAllShipmentsByOrderId(any())).thenReturn(List.of(shipment, shipment2, shipment3, shipment4));
        when(partnerApi.findByIdAndOrganizationId(any(), any())).thenReturn(null);
        when(qPortalApi.getPartner(any(), any())).thenReturn(qPortalPartner);
        when(qPortalApi.getUser(any(), any())).thenReturn(qPortalUser);
        ApiGatewayUpdateOrderProgressRequest result = apiGatewayService.createUpdateOrderProgressRequest(shipment, segment, milestone);

        assertThat(result.getPicInfo().getVendorCode()).isEqualTo(qPortalPartner.getPartnerCode());
        assertThat(result.getPicInfo().getVendorName()).isEqualTo(qPortalPartner.getName());
        assertThat(result.getPicInfo().getDriverName()).isEqualTo(qPortalUser.getFullName());
        assertThat(result.getPicInfo().getDriverPhoneNo()).isEqualTo(qPortalUser.getMobileNo());
        assertThat(result.getPicInfo().getUserName()).isEqualTo(qPortalUser.getUsername());
    }

    @Test
    void createUpdateOrderProgressRequest_withSegmentAndValidMilestone_shouldMapOrderTotalCodAmtFromPricingInfo() {
        Milestone milestone = testUtil.createMilestone();
        milestone.setToLocationId("");
        milestone.setToCityId("toCityId");
        milestone.setMilestoneCode(MilestoneCode.DSP_PICKUP_SUCCESSFUL);

        Shipment shipment = testUtil.createShipment();
        shipment.setShipmentReferenceId(List.of("1_1"));
        shipment.setDescription("This is a description");

        PricingInfo pricingInfo = new PricingInfo();
        pricingInfo.setCod(new BigDecimal(21));
        shipment.getShipmentPackage().setPricingInfo(pricingInfo);
        PackageJourneySegment segment = shipment.getShipmentJourney().getPackageJourneySegments().get(0);

        Shipment shipment2 = createTestShipmentWithMilestone("Milestone2", "1_2");
        Shipment shipment3 = createTestShipmentWithMilestone("milestone3", "2_1");
        Shipment shipment4 = createTestShipmentWithMilestone("milestone4", "33_1");

        when(shipmentFetchApi.findAllShipmentsByOrderId(any())).thenReturn(List.of(shipment, shipment2, shipment3, shipment4));
        when(partnerApi.findByIdAndOrganizationId(any(), any())).thenReturn(new Partner());

        ApiGatewayUpdateOrderProgressRequest result = apiGatewayService.createUpdateOrderProgressRequest(shipment, segment, milestone);

        assertThat(result.getOrderTotalCodAmt()).isEqualTo(pricingInfo.getCod());
    }


    private Shipment createTestShipmentWithMilestone(String milestoneName, String shipmentReferenceId) {
        Shipment shipment = testUtil.createShipment();
        Milestone milestone = new Milestone();
        milestone.setMilestoneName(milestoneName);
        milestone.setShipmentId(shipment.getId());
        milestone.setMilestoneCode(MilestoneCode.DSP_PICKUP_SUCCESSFUL);
        shipment.setMilestone(milestone);
        shipment.setShipmentReferenceId(List.of(shipmentReferenceId));
        shipment.setDescription("This is a description");
        return shipment;
    }

    private void assertPackageDetails(ApiGatewayUpdateOrderProgressRequest result, int index, String packageNo, String additionalData1, String milestoneCode) {
        assertThat(result.getPackages().get(index).getPackageNo()).isEqualTo(packageNo);
        assertThat(result.getPackages().get(index).getAdditionalData1()).isEqualTo(additionalData1);
        assertThat(result.getPackages().get(index).getMilestone()).isEqualTo(milestoneCode);
        assertThat(result.getPackages().get(index).getDescription()).isNotBlank();
    }
}
