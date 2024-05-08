package com.quincus.shipment.impl.service;

import com.quincus.qportal.api.QPortalApi;
import com.quincus.qportal.model.QPortalLocation;
import com.quincus.shipment.api.constant.LocationType;
import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.NetworkLane;
import com.quincus.shipment.api.domain.NetworkLaneSegment;
import com.quincus.shipment.impl.helper.NetworkLaneDurationCalculator;
import com.quincus.web.common.exception.model.QuincusException;
import com.quincus.web.common.exception.model.QuincusValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NetworkLaneEnrichmentServiceTest {
    @InjectMocks
    private NetworkLaneEnrichmentService networkLaneEnrichmentService;
    @Mock
    private NetworkLaneDurationCalculator networkLaneDurationCalculator;

    @Test
    void givenCityExistInQPortalWhenGenerateCityAddressFromQPortalThenProperlyMapToAddress() {
        //GIVEN:
        List<NetworkLaneSegment> networkLaneSegments = new ArrayList<>();
        NetworkLaneSegment segment1 = new NetworkLaneSegment();
        segment1.setSequence("1");
        NetworkLaneSegment segment2 = new NetworkLaneSegment();
        segment2.setSequence("1");
        networkLaneSegments.add(segment1);
        networkLaneSegments.add(segment2);

        NetworkLane networkLane = new NetworkLane();
        networkLane.setNetworkLaneSegments(networkLaneSegments);

        // Mock the behavior of the networkLaneDurationCalculator
        when(networkLaneDurationCalculator.calculateNetworkLaneSegmentDuration(segment1))
                .thenReturn(BigDecimal.valueOf(100));
        when(networkLaneDurationCalculator.calculateNetworkLaneSegmentDuration(segment2))
                .thenReturn(BigDecimal.valueOf(30));

        //WHEN:
        networkLaneEnrichmentService.enrichNetworkLanesDurationCalculation(List.of(networkLane));
        //THEN:
        assertThat(segment1.getCalculatedDuration()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(segment2.getCalculatedDuration()).isEqualTo(BigDecimal.valueOf(30));
        verify(networkLaneDurationCalculator, times(networkLaneSegments.size())).calculateNetworkLaneSegmentDuration(any());
    }
}
