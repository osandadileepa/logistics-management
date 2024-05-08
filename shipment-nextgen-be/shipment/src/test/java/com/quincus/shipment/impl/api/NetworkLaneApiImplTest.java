package com.quincus.shipment.impl.api;

import com.quincus.shipment.api.domain.NetworkLane;
import com.quincus.shipment.api.domain.NetworkLaneSegment;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.service.NetworkLaneService;
import com.quincus.shipment.impl.validator.NetworkLaneValidator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NetworkLaneApiImplTest {

    @InjectMocks
    private NetworkLaneApiImpl networkLaneApi;

    @Mock
    private NetworkLaneService networkLaneService;

    @Mock
    private UserDetailsProvider userDetailsProvider;

    @Mock
    private NetworkLaneValidator networkLaneValidator;

    @Test
    void testUpdate_sequenceEnriched() {
        NetworkLane networkLane = new NetworkLane();
        List<NetworkLaneSegment> segments = Arrays.asList(new NetworkLaneSegment(), new NetworkLaneSegment());
        networkLane.setNetworkLaneSegments(segments);
        when(networkLaneService.update(any(NetworkLane.class))).thenReturn(networkLane);

        final NetworkLane updatedNetworkLane = networkLaneApi.update(networkLane);

        final ArgumentCaptor<NetworkLane> captor = ArgumentCaptor.forClass(NetworkLane.class);
        verify(networkLaneValidator, times(1)).validate(captor.capture());
        verify(networkLaneService, times(1)).update(networkLane);

        Assertions.assertThat(updatedNetworkLane).isEqualTo(networkLane);
        final NetworkLane actualNetworkLaneEnriched = captor.getValue();
        Assertions.assertThat(actualNetworkLaneEnriched.getNetworkLaneSegments().get(0).getSequence()).isEqualTo("0");
        Assertions.assertThat(actualNetworkLaneEnriched.getNetworkLaneSegments().get(1).getSequence()).isEqualTo("1");
    }

}
