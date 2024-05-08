package com.quincus.shipment.impl.service;

import com.quincus.shipment.impl.repository.InstructionRepository;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstructionFetchServiceTest {

    @InjectMocks
    private InstructionFetchService instructionFetchService;
    @Mock
    private InstructionRepository instructionRepository;
    @Mock
    private UserDetailsProvider userDetailsProvider;

    @Test
    void givenSegmentIds_whenFindBySegmentIds_thenInvokeRepository() {
        //Given:
        List<String> segmentIds = List.of("1", "2");
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("orgId");
        //When:
        instructionFetchService.findBySegmentIds(segmentIds);
        //Then:
        verify(instructionRepository, times(1)).findAllBySegmentIds(segmentIds, "orgId");
    }

    @Test
    void givenSegmentIds_whenFindByOrderId_thenInvokeRepository() {
        //Given:
        String orderId = "order-id1";
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("orgId");
        //When:
        instructionFetchService.findByOrderId(orderId);
        //Then:
        verify(instructionRepository, times(1)).findAllByOrderId(orderId, "orgId");
    }

}
