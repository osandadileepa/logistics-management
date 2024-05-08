package com.quincus.shipment.impl.api;

import com.quincus.shipment.api.constant.MilestoneCode;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.dto.MilestoneUpdateRequest;
import com.quincus.shipment.api.dto.MilestoneUpdateResponse;
import com.quincus.shipment.api.dto.MilestoneUpdateTimeRequest;
import com.quincus.shipment.impl.service.MilestoneService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MilestoneApiImplTest {
    @Mock
    private MilestoneService milestoneService;
    @InjectMocks
    private MilestoneApiImpl milestoneApi;

    @Test
    void testIsFailedStatusCode() {
        MilestoneCode code = MilestoneCode.DSP_AIR_ARRIVED_AT_DELIVERY_LOCATION;

        when(milestoneService.isFailedStatusCode(code)).thenReturn(true);

        boolean result = milestoneApi.isFailedStatusCode(code);

        assertThat(result).isTrue();
        verify(milestoneService, times(1)).isFailedStatusCode(code);
    }

    @Test
    void testAdd() {
        MilestoneUpdateRequest request = new MilestoneUpdateRequest();
        MilestoneUpdateResponse response = new MilestoneUpdateResponse();

        when(milestoneService.saveMilestoneFromAPIG(request)).thenReturn(response);

        MilestoneUpdateResponse result = milestoneApi.add(request);

        assertThat(result).isEqualTo(response);
        verify(milestoneService, times(1)).saveMilestoneFromAPIG(request);
    }

    @Test
    void testPartialUpdate() {
        Milestone milestone = new Milestone();
        Milestone updatedMilestone = new Milestone();

        when(milestoneService.partialUpdate(milestone)).thenReturn(updatedMilestone);

        Milestone result = milestoneApi.partialUpdate(milestone);

        assertThat(result).isEqualTo(updatedMilestone);
        verify(milestoneService, times(1)).partialUpdate(milestone);
    }

    @Test
    void testUpdateMilestoneTime() {
        MilestoneUpdateTimeRequest request = new MilestoneUpdateTimeRequest();
        request.setId(UUID.randomUUID().toString());
        request.setMilestoneTime("2023-01-01T12:00:00Z");
        Milestone updatedMilestone = new Milestone();

        when(milestoneService.updateMilestoneTime(request)).thenReturn(updatedMilestone);

        Milestone result = milestoneApi.updateMilestoneTime(request);

        assertThat(result).isEqualTo(updatedMilestone);
        verify(milestoneService, times(1)).updateMilestoneTime(request);
    }
}

