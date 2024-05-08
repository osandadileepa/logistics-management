package com.quincus.shipment.impl.helper.journey.generator;

import com.quincus.order.api.domain.Destination;
import com.quincus.order.api.domain.Origin;
import com.quincus.order.api.domain.Root;
import com.quincus.shipment.api.constant.JourneyStatus;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.NetworkLane;
import com.quincus.shipment.api.domain.NetworkLaneSegment;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.filter.NetworkLaneFilter;
import com.quincus.shipment.api.filter.NetworkLaneFilterResult;
import com.quincus.shipment.impl.helper.journey.PackageJourneySegmentPickUpDropOffTimeCalculator;
import com.quincus.shipment.impl.helper.journey.PackageJourneySegmentTypeAssigner;
import com.quincus.shipment.impl.mapper.NetworkLaneFilterRootMapper;
import com.quincus.shipment.impl.mapper.NetworkLaneJourneySegmentMapper;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.service.NetworkLaneService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NetworkLaneSourceShipmentJourneyGeneratorTest {

    @InjectMocks
    private NetworkLaneSourceShipmentJourneyGenerator networkLaneSourceJourneyGenerator;
    @Mock
    private NetworkLaneService networkLaneService;
    @Mock
    private UserDetailsProvider userDetailsProvider;
    @Mock
    private NetworkLaneJourneySegmentMapper networkLaneJourneySegmentMapper;
    @Mock
    private NetworkLaneFilterRootMapper networkLaneFilterRootMapper;
    @Mock
    private PackageJourneySegmentTypeAssigner packageJourneySegmentTypeAssigner;
    @Mock
    private PackageJourneySegmentPickUpDropOffTimeCalculator packageJourneySegmentPickupDropOffTimeCalculator;

    private NetworkLaneSegment laneSegment1;
    private NetworkLaneSegment laneSegment2;
    private NetworkLaneSegment laneSegment3;
    private NetworkLaneSegment laneSegment4;

    @Test
    void givenMultipleNetworkLaneSegmentsMapped_whenGenerateJourney_createPlanedJourneyAssignSegmentsMapped() {
        //GIVEN:
        List<NetworkLane> networkLanes = generateNetworkLaneWithMultipleNetworkLaneConnections();
        Root orderMessage = createOmMessage();

        NetworkLaneFilter networkLaneFilter = mock(NetworkLaneFilter.class);
        NetworkLaneFilterResult result = mock(NetworkLaneFilterResult.class);
        when(result.getResult()).thenReturn(networkLanes);

        PackageJourneySegment segment1 = new PackageJourneySegment();
        segment1.setSequence("1");
        segment1.setRefId("0");
        segment1.setStartFacility(createFacility("start1"));
        segment1.setEndFacility(createFacility("end1"));
        PackageJourneySegment segment2 = new PackageJourneySegment();
        segment2.setSequence("2");
        segment2.setRefId("1");
        segment2.setStartFacility(createFacility("start2"));
        segment2.setEndFacility(createFacility("end2"));
        PackageJourneySegment segment3 = new PackageJourneySegment();
        segment3.setSequence("3");
        segment3.setRefId("2");
        segment3.setStartFacility(createFacility("start3"));
        segment3.setEndFacility(createFacility("end3"));
        PackageJourneySegment segment4 = new PackageJourneySegment();
        segment4.setSequence("4");
        segment4.setRefId("3");
        segment4.setStartFacility(createFacility("star4"));
        segment4.setEndFacility(createFacility("end4"));

        String ORG_ID = "orgId";
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(ORG_ID);
        when(networkLaneFilterRootMapper.mapOrderMessageToNetworkLaneFilter(orderMessage)).thenReturn(networkLaneFilter);
        when(networkLaneService.findAll(networkLaneFilter, ORG_ID)).thenReturn(result);
        when(networkLaneJourneySegmentMapper.toPackageJourneySegment(laneSegment1, "0", orderMessage.getOpsType())).thenReturn(segment1);
        when(networkLaneJourneySegmentMapper.toPackageJourneySegment(laneSegment2, "1", orderMessage.getOpsType())).thenReturn(segment2);
        when(networkLaneJourneySegmentMapper.toPackageJourneySegment(laneSegment3, "2", orderMessage.getOpsType())).thenReturn(segment3);
        when(networkLaneJourneySegmentMapper.toPackageJourneySegment(laneSegment4, "3", orderMessage.getOpsType())).thenReturn(segment4);

        //WHEN:
        ShipmentJourney shipmentJourney = networkLaneSourceJourneyGenerator.generateShipmentJourney(orderMessage);

        //THEN:
        assertThat(shipmentJourney).isNotNull();
        assertThat(shipmentJourney.getStatus()).isEqualTo(JourneyStatus.PLANNED);
        assertThat(shipmentJourney.getPackageJourneySegments()).hasSize(4);
        assertThat(shipmentJourney.getPackageJourneySegments().get(0).getStartFacility()).isNotNull();
        assertThat(shipmentJourney.getPackageJourneySegments().get(shipmentJourney.getPackageJourneySegments().size() - 1).getEndFacility()).isNotNull();
        shipmentJourney.getPackageJourneySegments().forEach(segment -> {
            assertThat(segment.getStartFacility().getId()).isNull();
            assertThat(segment.getEndFacility().getId()).isNull();
        });
        verify(networkLaneJourneySegmentMapper, times(1)).toPackageJourneySegment(laneSegment1, "0", orderMessage.getOpsType());
        verify(networkLaneJourneySegmentMapper, times(1)).toPackageJourneySegment(laneSegment2, "1", orderMessage.getOpsType());
        verify(networkLaneJourneySegmentMapper, times(1)).toPackageJourneySegment(laneSegment3, "2", orderMessage.getOpsType());
        verify(networkLaneJourneySegmentMapper, times(1)).toPackageJourneySegment(laneSegment4, "3", orderMessage.getOpsType());
        verify(packageJourneySegmentTypeAssigner, times(1)).assignSegmentTypes(any());
        verify(packageJourneySegmentPickupDropOffTimeCalculator, times(1)).computeAndAssignPickUpAndDropOffTime(any(), any());

    }

    @Test
    void givenFirstSegmentStartFacilityAndLastSegmentEndFacilityIsNull_shouldUseOrderOriginAndDestinationForStartAndEndFacility() {
        //GIVEN:
        List<NetworkLane> networkLanes = generateNetworkLaneWithMultipleNetworkLaneConnections();
        Root orderMessage = createOmMessage();

        NetworkLaneFilter networkLaneFilter = mock(NetworkLaneFilter.class);
        NetworkLaneFilterResult result = mock(NetworkLaneFilterResult.class);
        when(result.getResult()).thenReturn(networkLanes);

        PackageJourneySegment segment1 = new PackageJourneySegment();
        segment1.setSequence("1");
        segment1.setRefId("0");
        segment1.setEndFacility(createFacility("end1"));
        PackageJourneySegment segment2 = new PackageJourneySegment();
        segment2.setSequence("2");
        segment2.setRefId("1");
        segment2.setStartFacility(createFacility("start1"));
        segment2.setEndFacility(createFacility("end2"));
        PackageJourneySegment segment3 = new PackageJourneySegment();
        segment3.setSequence("3");
        segment3.setRefId("2");
        segment3.setStartFacility(createFacility("start3"));
        segment3.setEndFacility(createFacility("end3"));
        PackageJourneySegment segment4 = new PackageJourneySegment();
        segment4.setSequence("4");
        segment4.setRefId("3");
        segment4.setStartFacility(createFacility("start4"));

        String ORG_ID = "orgId";
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(ORG_ID);
        when(networkLaneFilterRootMapper.mapOrderMessageToNetworkLaneFilter(orderMessage)).thenReturn(networkLaneFilter);
        when(networkLaneService.findAll(networkLaneFilter, ORG_ID)).thenReturn(result);
        when(networkLaneJourneySegmentMapper.toPackageJourneySegment(laneSegment1, "0", orderMessage.getOpsType())).thenReturn(segment1);
        when(networkLaneJourneySegmentMapper.toPackageJourneySegment(laneSegment2, "1", orderMessage.getOpsType())).thenReturn(segment2);
        when(networkLaneJourneySegmentMapper.toPackageJourneySegment(laneSegment3, "2", orderMessage.getOpsType())).thenReturn(segment3);
        when(networkLaneJourneySegmentMapper.toPackageJourneySegment(laneSegment4, "3", orderMessage.getOpsType())).thenReturn(segment4);

        //WHEN:
        ShipmentJourney shipmentJourney = networkLaneSourceJourneyGenerator.generateShipmentJourney(orderMessage);

        //THEN:
        assertThat(shipmentJourney).isNotNull();
        assertThat(shipmentJourney.getStatus()).isEqualTo(JourneyStatus.PLANNED);
        assertThat(shipmentJourney.getPackageJourneySegments()).hasSize(4);
        assertThat(shipmentJourney.getPackageJourneySegments().get(0).getStartFacility()).isNotNull();
        assertThat(shipmentJourney.getPackageJourneySegments().get(shipmentJourney.getPackageJourneySegments().size() - 1).getEndFacility()).isNotNull();
        shipmentJourney.getPackageJourneySegments().forEach(segment -> {
            assertThat(segment.getStartFacility().getId()).isNull();
            assertThat(segment.getEndFacility().getId()).isNull();
        });
        verify(networkLaneJourneySegmentMapper, times(1)).toPackageJourneySegment(laneSegment1, "0", orderMessage.getOpsType());
        verify(networkLaneJourneySegmentMapper, times(1)).toPackageJourneySegment(laneSegment2, "1", orderMessage.getOpsType());
        verify(networkLaneJourneySegmentMapper, times(1)).toPackageJourneySegment(laneSegment3, "2", orderMessage.getOpsType());
        verify(networkLaneJourneySegmentMapper, times(1)).toPackageJourneySegment(laneSegment4, "3", orderMessage.getOpsType());
        verify(packageJourneySegmentTypeAssigner, times(1)).assignSegmentTypes(any());
        verify(packageJourneySegmentPickupDropOffTimeCalculator, times(1)).computeAndAssignPickUpAndDropOffTime(any(), any());

    }

    private Root createOmMessage() {
        Root orderMessage = new Root();
        orderMessage.setOpsType("p2p");
        Origin origin = new Origin();
        origin.setId("origin_facility_id");
        origin.setCountryId("origin_country_id");
        origin.setStateId("origin_state_id");
        origin.setCityId("origin_city_id");

        Destination destination = new Destination();
        destination.setId("destination_facility_id");
        destination.setCountryId("destination_country_id");
        destination.setStateId("destination_state_id");
        destination.setCityId("destination_city_id");

        orderMessage.setOrigin(origin);
        orderMessage.setDestination(destination);
        orderMessage.setServiceType("Express");
        return orderMessage;
    }

    private List<NetworkLane> generateNetworkLaneWithMultipleNetworkLaneConnections() {
        List<NetworkLane> networkLanes = new ArrayList<>();
        NetworkLane networkLane = new NetworkLane();

        laneSegment1 = new NetworkLaneSegment();
        laneSegment1.setSequence("1");
        laneSegment2 = new NetworkLaneSegment();
        laneSegment2.setSequence("2");
        laneSegment3 = new NetworkLaneSegment();
        laneSegment3.setSequence("3");
        laneSegment4 = new NetworkLaneSegment();
        laneSegment4.setSequence("4");
        networkLane.addNetworkLaneSegment(laneSegment1);
        networkLane.addNetworkLaneSegment(laneSegment2);
        networkLane.addNetworkLaneSegment(laneSegment3);
        networkLane.addNetworkLaneSegment(laneSegment4);
        networkLanes.add(networkLane);
        return networkLanes;
    }

    private Facility createFacility(String facilityId) {
        Facility facility = new Facility();
        facility.setId(facilityId);
        facility.setExternalId(facilityId);
        return facility;
    }
}
