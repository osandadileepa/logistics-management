package com.quincus.shipment.kafka.consumers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.quincus.shipment.api.MilestonePostProcessApi;
import com.quincus.shipment.api.ShipmentApi;
import com.quincus.shipment.api.constant.MilestoneCode;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.Shipment;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DispatchModuleListenerTest {

    @InjectMocks
    private DispatchModuleListener listener;
    @Mock
    private ShipmentApi shipmentApi;
    @Mock
    private MilestonePostProcessApi milestonePostProcessApi;
    @Captor
    private ArgumentCaptor<Milestone> milestoneCaptor;

    @Test
    void givenValidMilestone_whenMilestoneTopicListen_thenValidateMethodsInvoked() throws JsonProcessingException {
        //GIVEN:
        String shipmentId = "shipmentId";
        String milestoneMessage = "{\"shipmentId\":\"" + shipmentId + "\",\"milestoneCode\":\"DSP_PICKUP_SUCCESSFUL\"}";
        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord<>("topic", 0, 0, "key", milestoneMessage);

        Shipment shipment = new Shipment();
        shipment.setId(shipmentId);
        Milestone milestone = new Milestone();
        milestone.setShipmentId(shipmentId);
        milestone.setMilestoneCode(MilestoneCode.DSP_PICKUP_SUCCESSFUL);

        when(shipmentApi.receiveMilestoneMessageFromDispatch(anyString(), anyString())).thenReturn(milestone);
        when(shipmentApi.find(shipmentId)).thenReturn(shipment);

        //WHEN:
        listener.listen(consumerRecord);

        //THEN:
        verify(milestonePostProcessApi, times(1)).createAndSendShipmentMilestone(milestoneCaptor.capture(), eq(shipment));
        verify(milestonePostProcessApi, times(1)).createAndSendAPIGWebhooks(any(Milestone.class), eq(shipment));
        verify(milestonePostProcessApi, never()).requestDispatchForMilestoneResend(shipment);
        verify(milestonePostProcessApi, times(1)).createAndSendSegmentDispatch(any(Milestone.class), eq(shipment));
        verify(milestonePostProcessApi, times(1)).createAndSendQShipSegment(any(Milestone.class), eq(shipment));
        verify(milestonePostProcessApi, times(1)).createAndSendNotification(any(Milestone.class), eq(shipment));

        Milestone capturedMilestone = milestoneCaptor.getValue();
        assertThat(capturedMilestone).isNotNull();
        assertThat(capturedMilestone.getShipmentId()).isEqualTo(shipmentId);
        assertThat(capturedMilestone.getMilestoneCode()).isEqualTo(MilestoneCode.DSP_PICKUP_SUCCESSFUL);
    }

    @Test
    void givenFailedMileStones_whenMilestoneTopicListen_thenShouldCreateAlert_And_SendMessagesDownstream() throws JsonProcessingException {
        //GIVEN:
        String shipmentId = "shipmentId";
        String milestoneMessage = "{\"shipmentId\":\"" + shipmentId + "\",\"milestoneCode\":\"DSP_PICKUP_FAILED\"}";
        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord<>("topic", 0, 0, "key", milestoneMessage);

        Shipment shipment = new Shipment();
        shipment.setId(shipmentId);
        Milestone milestone = new Milestone();
        milestone.setShipmentId(shipmentId);
        milestone.setMilestoneCode(MilestoneCode.DSP_PICKUP_FAILED);

        when(shipmentApi.receiveMilestoneMessageFromDispatch(anyString(), anyString())).thenReturn(milestone);
        when(shipmentApi.find(shipmentId)).thenReturn(shipment);

        //WHEN:
        listener.listen(consumerRecord);

        //THEN:
        verify(milestonePostProcessApi, atLeastOnce()).createAndSendShipmentMilestone(milestoneCaptor.capture(), eq(shipment));
        verify(milestonePostProcessApi, atLeastOnce()).createAndSendAPIGWebhooks(any(Milestone.class), eq(shipment));
        verify(milestonePostProcessApi, atLeastOnce()).createAndSendSegmentDispatch(any(Milestone.class), eq(shipment));
        verify(milestonePostProcessApi, atLeastOnce()).createAndSendQShipSegment(any(Milestone.class), eq(shipment));
        verify(milestonePostProcessApi, atLeastOnce()).createAndSendNotification(any(Milestone.class), eq(shipment));

        Milestone capturedMilestone = milestoneCaptor.getValue();
        assertThat(capturedMilestone).isNotNull();
        assertThat(capturedMilestone.getShipmentId()).isEqualTo(shipmentId);
        assertThat(capturedMilestone.getMilestoneCode()).isEqualTo(MilestoneCode.DSP_PICKUP_FAILED);
    }
}