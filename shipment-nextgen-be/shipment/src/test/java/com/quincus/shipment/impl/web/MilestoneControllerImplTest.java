package com.quincus.shipment.impl.web;

import com.quincus.shipment.api.MilestoneApi;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.dto.MilestoneUpdateTimeRequest;
import com.quincus.web.common.model.Request;
import com.quincus.web.common.model.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MilestoneControllerImplTest {

    @InjectMocks
    private MilestoneControllerImpl milestoneController;

    @Mock
    private MilestoneApi milestoneApi;

    @Test
    void testUpdate() {
        Milestone milestone = new Milestone();
        Request<Milestone> request = new Request<>();
        request.setData(milestone);
        Milestone data = new Milestone();
        when(milestoneApi.partialUpdate(any(Milestone.class))).thenReturn(data);
        Response response = milestoneController.partialUpdate(request);
        assertThat(response.getData()).isEqualTo(data);
    }

    @Test
    void testUpdateMilestoneTime() {
        MilestoneUpdateTimeRequest milestone = new MilestoneUpdateTimeRequest();
        milestone.setId(UUID.randomUUID().toString());
        milestone.setMilestoneTime("2023-01-01T12:00:00Z");
        Request<MilestoneUpdateTimeRequest> request = new Request<>();
        request.setData(milestone);
        Milestone data = new Milestone();
        when(milestoneApi.updateMilestoneTime(any(MilestoneUpdateTimeRequest.class))).thenReturn(data);
        Response response = milestoneController.updateMilestoneTime(any(), request);
        assertThat(response.getData()).isEqualTo(data);
    }
}
