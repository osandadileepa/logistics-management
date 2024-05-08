package com.quincus.apigateway.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.quincus.apigateway.domain.FlightScheduleSearchParameter;
import com.quincus.apigateway.service.ApiGatewayService;
import com.quincus.apigateway.validator.FlightScheduleSearchValidator;
import com.quincus.apigateway.web.model.ApiGatewayWebhookResponse;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiGatewayApiImplTest {

    @InjectMocks
    private ApiGatewayApiImpl apiGatewayApi;
    @Mock
    private ApiGatewayRestClient apiGatewayRestClient;
    @Mock
    private FlightScheduleSearchValidator validator;
    @Mock
    private ApiGatewayService apiGatewayService;

    @Test
    void searchFlights_hasApiGatewayImpl_shouldCallRestSearchFlight() {
        apiGatewayApi.searchFlights(new FlightScheduleSearchParameter());
        verify(validator, times(1)).validateFlightScheduleSearchParameter(any(FlightScheduleSearchParameter.class));
        verify(apiGatewayRestClient, times(1)).searchFlights(any(FlightScheduleSearchParameter.class));
    }

    @Test
    void sendUpdateOrderProgress_withMilestone_shouldHaveNoErrors() throws JsonProcessingException {
        apiGatewayApi.sendUpdateOrderProgress(new Shipment(), new Milestone());
        verify(apiGatewayService, times(1)).sendUpdateOrderProgress(any(Shipment.class),
                any(Milestone.class));
    }

    @Test
    void sendUpdateOrderProgress_withSegment_shouldHaveNoErrors() throws JsonProcessingException {
        apiGatewayApi.sendUpdateOrderProgress(new Shipment(), new PackageJourneySegment(), new Milestone());
        verify(apiGatewayService, times(1)).sendUpdateOrderProgress(any(Shipment.class),
                any(PackageJourneySegment.class), any(Milestone.class));
    }

    @Test
    void sendAssignVendorDetails_withMilestone_shouldHaveNoErrors() throws JsonProcessingException {
        apiGatewayApi.sendAssignVendorDetails(new Shipment(), new Milestone());
        verify(apiGatewayService, times(1)).sendAssignVendorDetails(any(Shipment.class), any(Milestone.class));
    }

    @Test
    void sendAssignVendorDetailsWithPackageJourneySegmentWithRetry() throws JsonProcessingException {
        apiGatewayApi.sendAssignVendorDetailsWithRetry(new Shipment(), new PackageJourneySegment());
        verify(apiGatewayService, times(1)).sendAssignVendorDetails(any(Shipment.class), any(PackageJourneySegment.class));
    }

    @Test
    void testSendAssignVendorDetailsWithRetry_SuccessfulCall() throws JsonProcessingException {
        Shipment shipment = new Shipment();
        PackageJourneySegment packageJourneySegment = new PackageJourneySegment();
        ApiGatewayWebhookResponse expectedResponse = new ApiGatewayWebhookResponse();

        when(apiGatewayService.sendAssignVendorDetails(any(Shipment.class), any(PackageJourneySegment.class)))
                .thenReturn(expectedResponse);

        ApiGatewayWebhookResponse result = apiGatewayApi.sendAssignVendorDetailsWithRetry(shipment, packageJourneySegment);

        assertThat(result).isEqualTo(expectedResponse);
        verify(apiGatewayService, times(1)).sendAssignVendorDetails(shipment, packageJourneySegment);
    }

    @Test
    void sendCheckInDetails_withMilestone_shouldHaveNoErrors() throws JsonProcessingException {
        apiGatewayApi.sendCheckInDetails(new Shipment(), new Milestone());
        verify(apiGatewayService, times(1)).sendCheckInDetails(any(Shipment.class),
                any(Milestone.class));
    }

    @Test
    void sendCheckInDetails_withMilestoneAndSegment_shouldHaveNoErrors() throws JsonProcessingException {
        apiGatewayApi.sendCheckInDetails(new Shipment(), new PackageJourneySegment(), new Milestone());
        verify(apiGatewayService, times(1)).sendCheckInDetails(any(Shipment.class),
                any(PackageJourneySegment.class), any(Milestone.class));
    }
}
