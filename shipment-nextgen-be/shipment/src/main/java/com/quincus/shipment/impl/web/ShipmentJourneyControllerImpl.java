package com.quincus.shipment.impl.web;

import com.quincus.shipment.ShipmentJourneyController;
import com.quincus.shipment.api.VendorBookingApi;
import com.quincus.shipment.api.dto.MilestoneUpdateRequest;
import com.quincus.shipment.api.dto.MilestoneUpdateResponse;
import com.quincus.shipment.api.dto.VendorBookingUpdateRequest;
import com.quincus.shipment.api.dto.VendorBookingUpdateResponse;
import com.quincus.web.common.model.Request;
import com.quincus.web.common.model.Response;
import com.quincus.web.common.utility.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Validated
public class ShipmentJourneyControllerImpl implements ShipmentJourneyController {

    private final VendorBookingApi vendorBookingApi;

    @Override
    public Response<VendorBookingUpdateResponse> updateVendorBooking(String journeyId, String segmentId, Request<VendorBookingUpdateRequest> request) {
        VendorBookingUpdateRequest requestData = request.getData();
        requestData.setShipmentJourneyId(journeyId);
        requestData.setSegmentId(segmentId);
        return new Response<>(vendorBookingApi.receiveVendorBookingUpdate(requestData));
    }

    @Override
    @LogExecutionTime
    public Response<MilestoneUpdateResponse> receiveMilestoneUpdate(String journeyId, Request<MilestoneUpdateRequest> request) {
        final MilestoneUpdateRequest milestoneUpdateRequest = request.getData();
        return new Response<>(vendorBookingApi.receiveMilestoneUpdate(journeyId, milestoneUpdateRequest));
    }
}
