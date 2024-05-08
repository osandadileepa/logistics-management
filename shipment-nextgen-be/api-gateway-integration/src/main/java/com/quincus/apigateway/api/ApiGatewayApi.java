package com.quincus.apigateway.api;

import com.quincus.apigateway.domain.FlightSchedule;
import com.quincus.apigateway.domain.FlightScheduleSearchParameter;
import com.quincus.apigateway.web.model.ApiGatewayWebhookResponse;
import com.quincus.shipment.api.domain.Cost;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;

import java.util.List;

public interface ApiGatewayApi {

    List<FlightSchedule> searchFlights(FlightScheduleSearchParameter parameter);

    ApiGatewayWebhookResponse sendAssignVendorDetails(Shipment shipment, Milestone milestone);

    ApiGatewayWebhookResponse sendAssignVendorDetailsWithRetry(Shipment shipment, PackageJourneySegment packageJourneySegment);

    ApiGatewayWebhookResponse sendUpdateOrderProgress(Shipment shipment, Milestone milestone);

    ApiGatewayWebhookResponse sendUpdateOrderProgress(Shipment shipment, PackageJourneySegment segment, Milestone milestone);

    List<ApiGatewayWebhookResponse> sendUpdateOrderAdditionalCharges(Cost cost);

    ApiGatewayWebhookResponse sendCheckInDetails(Shipment shipment, Milestone milestone);

    ApiGatewayWebhookResponse sendCheckInDetails(Shipment shipment, PackageJourneySegment segment, Milestone milestone);
}
