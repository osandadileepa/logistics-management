package com.quincus.shipment.impl.web;

import com.quincus.shipment.api.VendorBookingApi;
import com.quincus.shipment.api.dto.MilestoneUpdateRequest;
import com.quincus.shipment.api.dto.MilestoneUpdateResponse;
import com.quincus.shipment.api.dto.VendorBookingUpdateRequest;
import com.quincus.shipment.api.dto.VendorBookingUpdateResponse;
import com.quincus.web.common.model.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentJourneyControllerImplTest {
    @InjectMocks
    private ShipmentJourneyControllerImpl controller;
    @Mock
    private VendorBookingApi api;

    @Test
    void givenValidRequestData_whenUpdateVendorBooking_shouldTriggerApiReceiveVendorBookingUpdateAndReturnResponse() {
        String journeyId = "becd0534-5cde-11ee-8c99-0242ac120002";
        String segmentId = "d0ee7f40-5cde-11ee-8c99-0242ac120002";

        Request<VendorBookingUpdateRequest> request = new Request<>();
        request.setData(new VendorBookingUpdateRequest());

        when(api.receiveVendorBookingUpdate(request.getData())).thenReturn(new VendorBookingUpdateResponse());
        VendorBookingUpdateResponse response = controller.updateVendorBooking(journeyId, segmentId, request).getData();

        assertThat(response).isNotNull();
        verify(api, times(1)).receiveVendorBookingUpdate(request.getData());
    }

    @Test
    void givenValidRequestData_whenReceiveMilestoneUpdate_shouldTriggerApiReceiveMilestoneUpdateAndReturnResponse() {
        String journeyId = "becd0534-5cde-11ee-8c99-0242ac120002";

        Request<MilestoneUpdateRequest> request = new Request<>();
        request.setData(new MilestoneUpdateRequest());

        when(api.receiveMilestoneUpdate(journeyId, request.getData())).thenReturn(new MilestoneUpdateResponse());
        MilestoneUpdateResponse response = controller.receiveMilestoneUpdate(journeyId, request).getData();

        assertThat(response).isNotNull();
        verify(api, times(1)).receiveMilestoneUpdate(journeyId, request.getData());
    }
}
