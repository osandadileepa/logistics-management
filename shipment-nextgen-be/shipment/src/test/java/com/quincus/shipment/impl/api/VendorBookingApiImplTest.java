package com.quincus.shipment.impl.api;

import com.quincus.shipment.api.dto.MilestoneUpdateRequest;
import com.quincus.shipment.api.dto.VendorBookingUpdateRequest;
import com.quincus.shipment.impl.service.VendorBookingService;
import com.quincus.shipment.impl.service.VendorMilestoneService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VendorBookingApiImplTest {

    @InjectMocks
    private VendorBookingApiImpl vendorBookingApi;

    @Mock
    private VendorBookingService vendorBookingService;

    @Mock
    private VendorMilestoneService vendorMilestoneService;

    @Test
    void givenVendorBookingUpdateRequest_whenReceiveVendorBookingUpdate_shouldProcessVendorBookingUpdatesFromApiG() {
        VendorBookingUpdateRequest request = new VendorBookingUpdateRequest();

        vendorBookingApi.receiveVendorBookingUpdate(request);

        Mockito.verify(vendorBookingService, Mockito.times(1)).receiveVendorBookingUpdatesFromApiG(request);
    }

    @Test
    void givenMilestoneUpdateRequest_whenReceiveMilestoneUpdatee_shouldProcessMilestoneUpdateFromAPIG() {
        String journeyId = "6539dacc-09b3-4b29-84d4-336a4a260818";
        MilestoneUpdateRequest request = new MilestoneUpdateRequest();

        vendorBookingApi.receiveMilestoneUpdate(journeyId, request);

        Mockito.verify(vendorMilestoneService, Mockito.times(1)).receiveMilestoneUpdateFromAPIG(journeyId, request);
    }
}
