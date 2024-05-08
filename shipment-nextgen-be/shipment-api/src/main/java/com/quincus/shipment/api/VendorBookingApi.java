package com.quincus.shipment.api;

import com.quincus.shipment.api.dto.MilestoneUpdateRequest;
import com.quincus.shipment.api.dto.MilestoneUpdateResponse;
import com.quincus.shipment.api.dto.VendorBookingUpdateRequest;
import com.quincus.shipment.api.dto.VendorBookingUpdateResponse;

public interface VendorBookingApi {
    VendorBookingUpdateResponse receiveVendorBookingUpdate(VendorBookingUpdateRequest vendorBookingUpdateRequest);

    MilestoneUpdateResponse receiveMilestoneUpdate(String journeyId, MilestoneUpdateRequest milestoneUpdateRequest);
}
