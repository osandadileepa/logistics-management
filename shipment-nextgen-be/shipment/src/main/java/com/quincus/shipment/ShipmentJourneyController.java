package com.quincus.shipment;

import com.quincus.shipment.api.dto.MilestoneUpdateRequest;
import com.quincus.shipment.api.dto.MilestoneUpdateResponse;
import com.quincus.shipment.api.dto.VendorBookingUpdateRequest;
import com.quincus.shipment.api.dto.VendorBookingUpdateResponse;
import com.quincus.web.common.model.Request;
import com.quincus.web.common.model.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/shipment_journeys")
@Tag(name = "shipment_journeys", description = "This endpoint allows to manage journey segment related transactions.")
public interface ShipmentJourneyController {

    @PutMapping("/{journey_id}/package-journey-segments/{segment_id}")
    @Operation(summary = "Receive updates from vendor for a booking", description = "Receive updates from vendor for a booking", tags = "journeys")
    Response<VendorBookingUpdateResponse> updateVendorBooking(@PathVariable(name = "journey_id") final String journeyId,
                                                              @PathVariable(name = "segment_id") final String segmentId,
                                                              @RequestBody final Request<VendorBookingUpdateRequest> request);

    @PostMapping("/{journey_id}/milestones")
    @Operation(summary = "Receive milestone updates from APIG", description = "Receive milestone updates from APIG", tags = "journeys")
    Response<MilestoneUpdateResponse> receiveMilestoneUpdate(@PathVariable(name = "journey_id") final String journeyId,
                                                             @RequestBody final Request<MilestoneUpdateRequest> request);

}
