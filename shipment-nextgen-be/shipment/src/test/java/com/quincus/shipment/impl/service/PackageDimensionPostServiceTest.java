package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.MessageApi;
import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.domain.HostedFile;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.api.domain.Shipment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackageDimensionPostServiceTest {

    @InjectMocks
    private PackageDimensionPostProcessService packageDimensionPostProcessService;
    @Mock
    private MilestoneService milestoneService;
    @Mock
    private MessageApi messageApi;
    @Captor
    private ArgumentCaptor<Shipment> shipmentArgumentCaptor;

    @Test
    void givenShipmentEntity_whenCreateAndSendDimsAndWeightMilestoneForShipment_thenCreateMilestoneAndSendMessage() {
        Shipment shipment = new Shipment();
        Order order = new Order();
        order.setId("orderId");
        shipment.setOrder(order);
        shipment.setId("1");
        shipment.setShipmentTrackingId("shipmentTrackingId");
        shipment.setOrganization(new Organization());
        shipment.getOrganization().setId("orgId");
        List<HostedFile> existingAttachments = new ArrayList<>();
        HostedFile existingAttachment = new HostedFile(randomUUID().toString(), "file2", "url.com", null, 3245L, null);
        existingAttachments.add(existingAttachment);
        shipment.setShipmentAttachments(existingAttachments);

        Milestone milestoneCreated = new Milestone();
        when(milestoneService.createPackageDimensionUpdateMilestone(any())).thenReturn(milestoneCreated);

        packageDimensionPostProcessService.createAndSendDimsAndWeightMilestoneForShipment(shipment);
        verify(milestoneService, times(1)).createPackageDimensionUpdateMilestone(shipmentArgumentCaptor.capture());
        verify(messageApi, times(1)).sendMilestoneMessage(shipmentArgumentCaptor.getValue(), TriggeredFrom.SHP);

        assertThat(shipmentArgumentCaptor.getValue().getMilestone()).isEqualTo(milestoneCreated);
        assertThat(shipmentArgumentCaptor.getValue().getMilestoneEvents().get(0)).isEqualTo(milestoneCreated);

    }


}