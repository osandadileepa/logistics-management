package com.quincus.shipment.impl.api;

import com.quincus.shipment.api.VendorBookingApi;
import com.quincus.shipment.api.dto.MilestoneUpdateRequest;
import com.quincus.shipment.api.dto.MilestoneUpdateResponse;
import com.quincus.shipment.api.dto.VendorBookingUpdateRequest;
import com.quincus.shipment.api.dto.VendorBookingUpdateResponse;
import com.quincus.shipment.impl.service.VendorBookingService;
import com.quincus.shipment.impl.service.VendorMilestoneService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class VendorBookingApiImpl implements VendorBookingApi {

    private final VendorBookingService vendorBookingService;
    private final VendorMilestoneService vendorMilestoneService;

    @Override
    public VendorBookingUpdateResponse receiveVendorBookingUpdate(VendorBookingUpdateRequest vendorBookingUpdateRequest) {
        return vendorBookingService.receiveVendorBookingUpdatesFromApiG(vendorBookingUpdateRequest);
    }

    @Override
    public MilestoneUpdateResponse receiveMilestoneUpdate(String journeyId, MilestoneUpdateRequest milestoneUpdateRequest) {
        return vendorMilestoneService.receiveMilestoneUpdateFromAPIG(journeyId, milestoneUpdateRequest);
    }
}
