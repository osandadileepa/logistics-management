package com.quincus.apigateway.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.apigateway.api.ApiGatewayWebhookClient;
import com.quincus.apigateway.test_utils.TestUtil;
import com.quincus.apigateway.web.model.ApiGatewayAssignVendorDetailRequest;
import com.quincus.apigateway.web.model.ApiGatewayWebhookResponse;
import com.quincus.shipment.api.ArchivedApi;
import com.quincus.shipment.api.domain.Archived;
import com.quincus.shipment.api.domain.Driver;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Partner;
import com.quincus.shipment.api.domain.Shipment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssignToVendorServiceTest {
    private final TestUtil testUtil = TestUtil.getInstance();
    @InjectMocks
    private AssignToVendorService service;
    @Mock
    private ArchivedApi archivedApi;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private ApiGatewayWebhookClient apiGatewayWebHookClient;
    @Captor
    private ArgumentCaptor<ApiGatewayAssignVendorDetailRequest> assignVendorDetailRequestCaptor;

    @Test
    void sendAssignVendorDetails_withValidMilestone_shouldHaveNoErrors() {
        Milestone milestone = testUtil.createMilestone();
        Shipment shipment = testUtil.createShipment();
        JsonNode jsonResponse = testUtil.getAssignVendorDetailsResponseJson();
        ApiGatewayWebhookResponse mockResponse = testUtil.getObjectMapper().convertValue(jsonResponse, ApiGatewayWebhookResponse.class);
        when(apiGatewayWebHookClient.assignVendorDetails(anyString(), any(ApiGatewayAssignVendorDetailRequest.class)))
                .thenReturn(mockResponse);

        ApiGatewayWebhookResponse response = service.sendAssignVendorDetails(shipment, milestone);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(mockResponse.getStatus());
        assertThat(response.getMessage()).isEqualTo(mockResponse.getMessage());
        verify(apiGatewayWebHookClient, times(1))
                .assignVendorDetails(anyString(), any(ApiGatewayAssignVendorDetailRequest.class));
        verify(archivedApi, times(1)).save(any());
    }

    @Test
    void sendAssignVendorDetails_withValidSegment_shouldHaveNoErrors() {
        Shipment shipment = testUtil.createShipment();
        PackageJourneySegment segment = newSegment("partner1");
        segment.setSegmentId("segment1");
        segment.setOrganizationId(shipment.getOrganization().getId());
        segment.setRefId("1");
        segment.setSequence("0");
        Driver driver = new Driver();
        driver.setId("driver1");
        driver.setName("Mr Driver");
        driver.setPhoneCode("+1");
        driver.setPhoneNumber("12345");
        segment.setDriver(driver);

        JsonNode jsonResponse = testUtil.getAssignVendorDetailsResponseJson();
        ApiGatewayWebhookResponse mockResponse = testUtil.getObjectMapper().convertValue(jsonResponse, ApiGatewayWebhookResponse.class);
        when(apiGatewayWebHookClient.assignVendorDetails(anyString(), any(ApiGatewayAssignVendorDetailRequest.class)))
                .thenReturn(mockResponse);

        ApiGatewayWebhookResponse response = service.sendAssignVendorDetails(shipment, segment);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(mockResponse.getStatus());
        assertThat(response.getMessage()).isEqualTo(mockResponse.getMessage());
        verify(apiGatewayWebHookClient, times(1))
                .assignVendorDetails(anyString(), assignVendorDetailRequestCaptor.capture());
        verify(archivedApi, times(1)).save(any());

        ApiGatewayAssignVendorDetailRequest vendorDetailRequest = assignVendorDetailRequestCaptor.getValue();
        assertThat(vendorDetailRequest).isNotNull();
        assertThat(vendorDetailRequest.getOrderNo()).isEqualTo(shipment.getExternalOrderId());
        assertThat(vendorDetailRequest.getSegmentId()).isEqualTo(segment.getRefId());
        assertThat(vendorDetailRequest.getVendorId()).isEqualTo(segment.getPartner().getId());
        assertThat(vendorDetailRequest.getDriverPhoneCode()).isEqualTo(driver.getPhoneCode());
        assertThat(vendorDetailRequest.getDriverPhoneNumber()).isEqualTo(driver.getPhoneNumber());
        assertThat(vendorDetailRequest.isVendorReassigned()).isFalse();
        assertThat(vendorDetailRequest.getAssignedAt()).isNotNull();
    }

    @Test
    void sendAssignVendorDetails_withValidSegmentNoDriver_shouldHaveNoErrors() {
        Shipment shipment = testUtil.createShipment();
        PackageJourneySegment segment = newSegment("partner1");
        segment.setSegmentId("segment1");
        segment.setOrganizationId(shipment.getOrganization().getId());
        segment.setRefId("1");
        segment.setSequence("0");

        JsonNode jsonResponse = testUtil.getAssignVendorDetailsResponseJson();
        ApiGatewayWebhookResponse mockResponse = testUtil.getObjectMapper().convertValue(jsonResponse, ApiGatewayWebhookResponse.class);
        when(apiGatewayWebHookClient.assignVendorDetails(anyString(), any(ApiGatewayAssignVendorDetailRequest.class)))
                .thenReturn(mockResponse);

        ApiGatewayWebhookResponse response = service.sendAssignVendorDetails(shipment, segment);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(mockResponse.getStatus());
        assertThat(response.getMessage()).isEqualTo(mockResponse.getMessage());
        verify(apiGatewayWebHookClient, times(1))
                .assignVendorDetails(anyString(), assignVendorDetailRequestCaptor.capture());
        verify(archivedApi, times(1)).save(any());

        ApiGatewayAssignVendorDetailRequest vendorDetailRequest = assignVendorDetailRequestCaptor.getValue();
        assertThat(vendorDetailRequest).isNotNull();
        assertThat(vendorDetailRequest.getOrderNo()).isEqualTo(shipment.getOrder().getOrderIdLabel());
        assertThat(vendorDetailRequest.getSegmentId()).isEqualTo(segment.getRefId());
        assertThat(vendorDetailRequest.getVendorId()).isEqualTo(segment.getPartner().getId());
        assertThat(vendorDetailRequest.getDriverPhoneCode()).isNull();
        assertThat(vendorDetailRequest.getDriverPhoneNumber()).isNull();
        assertThat(vendorDetailRequest.isVendorReassigned()).isFalse();
        assertThat(vendorDetailRequest.getAssignedAt()).isNotNull();
    }

    @Test
    void sendAssignVendorDetails_withNoPartner_shouldSendWebhook() {
        Shipment shipment = testUtil.createShipment();
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId("segment1");
        segment.setOrganizationId(shipment.getOrganization().getId());
        segment.setRefId("1");
        segment.setSequence("0");
        Driver driver = new Driver();
        driver.setId("driver1");
        driver.setName("Mr Driver");
        driver.setPhoneCode("+1");
        driver.setPhoneNumber("12345");
        segment.setDriver(driver);
        segment.setStartFacility(new Facility());
        segment.setEndFacility(new Facility());

        JsonNode jsonResponse = testUtil.getAssignVendorDetailsResponseJson();
        ApiGatewayWebhookResponse mockResponse = testUtil.getObjectMapper().convertValue(jsonResponse, ApiGatewayWebhookResponse.class);
        mockResponse.setMessage("Missing required field vendorId");
        when(apiGatewayWebHookClient.assignVendorDetails(anyString(), any(ApiGatewayAssignVendorDetailRequest.class)))
                .thenReturn(mockResponse);

        ApiGatewayWebhookResponse response = service.sendAssignVendorDetails(shipment, segment);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(mockResponse.getStatus());
        assertThat(response.getMessage()).isEqualTo(mockResponse.getMessage());
        verify(apiGatewayWebHookClient, times(1))
                .assignVendorDetails(anyString(), assignVendorDetailRequestCaptor.capture());
        verify(archivedApi, never()).save(any());

        ApiGatewayAssignVendorDetailRequest vendorDetailRequest = assignVendorDetailRequestCaptor.getValue();
        assertThat(vendorDetailRequest).isNotNull();
        assertThat(vendorDetailRequest.getOrderNo()).isEqualTo(shipment.getExternalOrderId());
        assertThat(vendorDetailRequest.getSegmentId()).isEqualTo(segment.getRefId());
        assertThat(vendorDetailRequest.getVendorId()).isNull();
        assertThat(vendorDetailRequest.getDriverPhoneCode()).isEqualTo(driver.getPhoneCode());
        assertThat(vendorDetailRequest.getDriverPhoneNumber()).isEqualTo(driver.getPhoneNumber());
        assertThat(vendorDetailRequest.isVendorReassigned()).isFalse();
        assertThat(vendorDetailRequest.getAssignedAt()).isNotNull();
    }

    @Test
    void sendAssignVendorDetails_withValidMilestoneAndNoExternalOrderId_shouldSendToWebhook() {
        Milestone milestone = testUtil.createMilestone();
        Shipment shipment = testUtil.createShipment();
        shipment.setExternalOrderId(null);
        shipment.getOrder().setOrderIdLabel(null);

        ApiGatewayWebhookResponse response = service.sendAssignVendorDetails(shipment, milestone);

        assertThat(response).isNull();
        verify(apiGatewayWebHookClient, never())
                .assignVendorDetails(anyString(), any(ApiGatewayAssignVendorDetailRequest.class));
        verify(archivedApi, never()).save(any());
    }

    @Test
    void sendAssignVendorDetails_withValidSegmentAndNoExternalOrderId_shouldNotSendToWebhook() {
        Shipment shipment = testUtil.createShipment();
        shipment.setExternalOrderId(null);
        shipment.getOrder().setOrderIdLabel(null);
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId("segment1");
        segment.setOrganizationId(shipment.getOrganization().getId());
        segment.setRefId("1");
        segment.setSequence("0");
        Driver driver = new Driver();
        driver.setId("driver1");
        driver.setName("Mr Driver");
        driver.setPhoneCode("+1");
        driver.setPhoneNumber("12345");
        segment.setDriver(driver);
        Partner partner = new Partner();
        partner.setId("partner1");
        segment.setPartner(partner);

        ApiGatewayWebhookResponse response = service.sendAssignVendorDetails(shipment, segment);

        assertThat(response).isNull();
        verify(apiGatewayWebHookClient, never())
                .assignVendorDetails(anyString(), assignVendorDetailRequestCaptor.capture());
        verify(archivedApi, never()).save(any());
    }

    @Test
    void sendAssignVendorDetails_withValidSegmentNoDriverAndNoExternalOrderId_shouldNotSendToWebhook() {
        Shipment shipment = testUtil.createShipment();
        shipment.setExternalOrderId(null);
        shipment.getOrder().setOrderIdLabel(null);
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId("segment1");
        segment.setOrganizationId(shipment.getOrganization().getId());
        segment.setRefId("1");
        segment.setSequence("0");
        Partner partner = new Partner();
        partner.setId("partner1");
        segment.setPartner(partner);

        ApiGatewayWebhookResponse response = service.sendAssignVendorDetails(shipment, segment);

        assertThat(response).isNull();
        verify(apiGatewayWebHookClient, never())
                .assignVendorDetails(anyString(), assignVendorDetailRequestCaptor.capture());
        verify(archivedApi, never()).save(any());
    }

    @Test
    void sendAssignVendorDetails_withNoExternalIdButWithOrderIdLabel_shouldHaveNoErrors() {
        Shipment shipment = testUtil.createShipment();
        shipment.setExternalOrderId(null);
        PackageJourneySegment segment = newSegment("partnerA");
        segment.setSegmentId("segment1");
        segment.setOrganizationId(shipment.getOrganization().getId());
        segment.setRefId("1");
        segment.setSequence("0");
        Driver driver = new Driver();
        driver.setId("driver1");
        driver.setName("Mr Driver");
        driver.setPhoneCode("+1");
        driver.setPhoneNumber("12345");
        segment.setDriver(driver);

        JsonNode jsonResponse = testUtil.getAssignVendorDetailsResponseJson();
        ApiGatewayWebhookResponse mockResponse = testUtil.getObjectMapper().convertValue(jsonResponse, ApiGatewayWebhookResponse.class);
        when(apiGatewayWebHookClient.assignVendorDetails(anyString(), any(ApiGatewayAssignVendorDetailRequest.class)))
                .thenReturn(mockResponse);

        ApiGatewayWebhookResponse response = service.sendAssignVendorDetails(shipment, segment);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(mockResponse.getStatus());
        assertThat(response.getMessage()).isEqualTo(mockResponse.getMessage());
        verify(apiGatewayWebHookClient, times(1))
                .assignVendorDetails(anyString(), assignVendorDetailRequestCaptor.capture());
        verify(archivedApi, times(1)).save(any());

        ApiGatewayAssignVendorDetailRequest vendorDetailRequest = assignVendorDetailRequestCaptor.getValue();
        assertThat(vendorDetailRequest).isNotNull();
        assertThat(vendorDetailRequest.getOrderNo()).isEqualTo(shipment.getOrder().getOrderIdLabel());
        assertThat(vendorDetailRequest.getSegmentId()).isEqualTo(segment.getRefId());
        assertThat(vendorDetailRequest.getVendorId()).isEqualTo(segment.getPartner().getId());
        assertThat(vendorDetailRequest.getDriverPhoneCode()).isEqualTo(driver.getPhoneCode());
        assertThat(vendorDetailRequest.getDriverPhoneNumber()).isEqualTo(driver.getPhoneNumber());
        assertThat(vendorDetailRequest.isVendorReassigned()).isFalse();
        assertThat(vendorDetailRequest.getAssignedAt()).isNotNull();
    }

    @Test
    void sendAssignVendorDetails_withNotSuccessResponse_shouldNotArchived() {
        Shipment shipment = testUtil.createShipment();
        PackageJourneySegment segment = newSegment(null);
        segment.setSegmentId("segment1");
        segment.setOrganizationId(shipment.getOrganization().getId());

        JsonNode jsonResponse = testUtil.getAssignVendorDetailsResponseJson();
        ApiGatewayWebhookResponse mockResponse = testUtil.getObjectMapper().convertValue(jsonResponse, ApiGatewayWebhookResponse.class);
        mockResponse.setMessage("Missing required field vendorId");
        when(apiGatewayWebHookClient.assignVendorDetails(anyString(), any(ApiGatewayAssignVendorDetailRequest.class)))
                .thenReturn(mockResponse);

        ApiGatewayWebhookResponse response = service.sendAssignVendorDetails(shipment, segment);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(mockResponse.getStatus());
        assertThat(response.getMessage()).isEqualTo(mockResponse.getMessage());
        verify(archivedApi, never()).save(any());
    }

    @Test
    @DisplayName("Should mark as reassigned when changed from partner A to partner B")
    void testIsVendorReassigned_ChangeFromPartnerAToPartnerB() throws Exception {
        stubPreviousPartner("partnerB");
        assertThat(service.isVendorReassigned(testUtil.createShipment(), newSegment("partnerA"))).isTrue();
    }

    @Test
    @DisplayName("Should mark as reassigned when changed from partner A to no partner")
    void testIsVendorReassigned_ChangeFromPartnerAToNull() throws Exception {
        stubPreviousPartner(null);
        assertThat(service.isVendorReassigned(testUtil.createShipment(), newSegment("partnerA"))).isTrue();
    }

    @Test
    @DisplayName("Should mark as reassigned when changed from no partner to partner A")
    void testIsVendorReassigned_ChangeFromNullToPartnerA() throws Exception {
        stubPreviousPartner("partnerA");
        assertThat(service.isVendorReassigned(testUtil.createShipment(), newSegment(null))).isTrue();
    }

    @Test
    @DisplayName("Should not mark as reassigned when there is no change in partner A")
    void testIsVendorReassigned_NoChange() throws Exception {
        stubPreviousPartner("partnerA");
        assertThat(service.isVendorReassigned(testUtil.createShipment(), newSegment("partnerA"))).isFalse();
    }

    private void stubPreviousPartner(String archivedPartnerId) throws Exception {
        Archived archivedData = new Archived();
        PackageJourneySegment archivedSegment = newSegment(archivedPartnerId);

        when(archivedApi.findLatestByReferenceId(any())).thenReturn(Optional.of(archivedData));
        when(objectMapper.readValue(archivedData.getData(), PackageJourneySegment.class)).thenReturn(archivedSegment);
    }

    private PackageJourneySegment newSegment(String partnerId) {
        PackageJourneySegment segment = new PackageJourneySegment();
        if (partnerId != null) {
            Partner partner = new Partner();
            partner.setId(partnerId);
            segment.setPartner(partner);
        }
        Facility startFacility = new Facility();
        startFacility.setId(UUID.randomUUID().toString());
        Facility endFacility = new Facility();
        endFacility.setId(UUID.randomUUID().toString());
        segment.setStartFacility(startFacility);
        segment.setEndFacility(endFacility);
        segment.setJourneyId(UUID.randomUUID().toString());
        return segment;
    }

}
