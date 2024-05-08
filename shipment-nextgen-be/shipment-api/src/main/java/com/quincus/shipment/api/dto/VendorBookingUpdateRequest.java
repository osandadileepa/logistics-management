package com.quincus.shipment.api.dto;

import com.quincus.shipment.api.constant.BookingStatus;
import com.quincus.shipment.api.constant.JourneySegmentAction;
import lombok.Data;

@Data
public class VendorBookingUpdateRequest {

    private JourneySegmentAction type;
    private String bookingId;
    private String bookingVendorReferenceId;
    private BookingStatus bookingStatus;
    private String rejectionReason;
    private String waybillNumber;
    private String segmentId;
    private String shipmentJourneyId;
}
