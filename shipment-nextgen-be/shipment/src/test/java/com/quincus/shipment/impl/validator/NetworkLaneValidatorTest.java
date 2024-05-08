package com.quincus.shipment.impl.validator;

import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.NetworkLane;
import com.quincus.shipment.api.domain.NetworkLaneSegment;
import com.quincus.shipment.api.domain.Partner;
import com.quincus.shipment.api.exception.NetworkLaneException;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

@ExtendWith(MockitoExtension.class)
class NetworkLaneValidatorTest {
    private static final String partnerName = "test-partner-name";
    private static final String organizationId = "TestOrgId";
    @InjectMocks
    private NetworkLaneValidator networkLaneValidator;

    @ParameterizedTest
    @MethodSource("provideNetworkLaneScenarioDataAndExpectedErrors")
    void testUpdate_WithInvalidSegments_ShouldThrowValidationException(
            final NetworkLane givenNetworkLane,
            final int expectedErrorCount,
            final List<String> expectedErrorMessages) {

        Throwable thrown = catchThrowable(() -> networkLaneValidator.validate(givenNetworkLane));

        if (expectedErrorCount == 0 && expectedErrorMessages == null) {
            assertThat(thrown).isNull();
        } else {
            assertThat(thrown)
                    .isInstanceOfSatisfying(NetworkLaneException.class, exception -> {
                        assertThat(exception.getErrors())
                                .hasSize(expectedErrorCount)
                                .containsAll(expectedErrorMessages);
                    })
                    .hasMessageContaining("Invalid Network Lane");
        }
    }

    public static Stream<Arguments> provideNetworkLaneScenarioDataAndExpectedErrors() {
        return Stream.of(
                Arguments.of(Named.of("Should not allow identical first & last node facility.", createScenario_identicalFirstAndLastNodesFacility()), 1, List.of(
                        "networklane.origin_facility and destination_facility Identical first & last network nodes."
                )),
                Arguments.of(Named.of("Should not allow identical first & last node location.", createScenario_identicalFirstAndLastNodesLocation()), 1, List.of(
                        "networklane.origin and destination Identical first & last network nodes."
                )),
                Arguments.of(Named.of("Should not validate identical first & last node when facility id is empty or blank.", createScenario_notIdenticalFirstAndLastNodesWhenEmptyFacilityId()), 0, null),
                Arguments.of(Named.of("Should not validate identical first & last node when location id is empty or blank.", createScenario_notIdenticalFirstAndLastNodesWhenEmptyLocationId()), 0, null),
                Arguments.of(Named.of("Should not allow edit when location is missing from any of the segment lanes.", createScenario_missingLocationFromAnySegmentLanes()), 5, List.of(
                        "networklane.network_lane_segments[0].start_facility.location Location is missing.",
                        "networklane.network_lane_segments[1].start_facility.location Location is missing."
                )),
                Arguments.of(Named.of("Should not allow edit when origin to first lane segment is disconnected.", createScenario_originAndFirstSegmentLaneIsDisconnected()), 1, List.of(
                        "networklane.origin_facility and network_lane_segments[0].start_facility Nodes not connected."
                )),
                Arguments.of(Named.of("Should not allow edit when last lane segment to destination is disconnected.", createScenario_destinationAndLastSegmentLaneIsDisconnected()), 1, List.of(
                        "networklane.destination_facility and network_lane_segments[2].end_facility Nodes not connected."
                )),
                Arguments.of(Named.of("Should not allow edit when middle lane segment is disconnected.", createScenario_middleSegmentLaneIsDisconnected()), 1, List.of(
                        "networklane.network_lane_segments[1].end_facility and network_lane_segments[2].start_facility Nodes not connected."
                )),
                Arguments.of(Named.of("Should allow edit when network lane segments are connected.", createScenario_validNetworkLane()), 0, null)
        );
    }

    private static NetworkLane createScenario_validNetworkLane() {

        Facility facility1 = new Facility();
        facility1.setId(UUID.randomUUID().toString());
        facility1.setLocation(getRandomAddress());

        Facility facility2 = new Facility();
        facility2.setId(UUID.randomUUID().toString());
        facility2.setLocation(getRandomAddress());

        NetworkLane givenNetworkLane = getNetworkLaneWithOriginAndDestinationFacility();

        List<NetworkLaneSegment> networkLaneSegments = new ArrayList<>();
        networkLaneSegments.add(generateNetworkLaneSegment(givenNetworkLane.getOriginFacility(), facility1));
        networkLaneSegments.add(generateNetworkLaneSegment(facility1, facility2));
        networkLaneSegments.add(generateNetworkLaneSegment(facility2, givenNetworkLane.getDestinationFacility()));

        givenNetworkLane.setNetworkLaneSegments(networkLaneSegments);
        return givenNetworkLane;
    }

    private static NetworkLane createScenario_networkLaneWithEmptySegmentLanes() {
        NetworkLane givenNetworkLane = getNetworkLaneWithOriginAndDestinationFacility();
        givenNetworkLane.setNetworkLaneSegments(Collections.emptyList());
        return givenNetworkLane;
    }

    private static NetworkLane createScenario_middleSegmentLaneIsDisconnected() {

        Facility facility1 = new Facility();
        facility1.setId(UUID.randomUUID().toString());
        facility1.setLocation(getRandomAddress());

        Facility facility2 = new Facility();
        facility2.setId(UUID.randomUUID().toString());
        facility2.setLocation(getRandomAddress());

        Facility disconnectedFacility = new Facility();
        disconnectedFacility.setId(UUID.randomUUID().toString());
        disconnectedFacility.setLocation(getRandomAddress());

        NetworkLane givenNetworkLane = getNetworkLaneWithOriginAndDestinationFacility();

        List<NetworkLaneSegment> networkLaneSegments = new ArrayList<>();
        networkLaneSegments.add(generateNetworkLaneSegment(givenNetworkLane.getOriginFacility(), facility1));
        networkLaneSegments.add(generateNetworkLaneSegment(facility1, disconnectedFacility));
        networkLaneSegments.add(generateNetworkLaneSegment(facility2, givenNetworkLane.getDestinationFacility()));

        givenNetworkLane.setNetworkLaneSegments(networkLaneSegments);
        return givenNetworkLane;
    }

    private static NetworkLane createScenario_destinationAndLastSegmentLaneIsDisconnected() {

        Facility facility1 = new Facility();
        facility1.setId(UUID.randomUUID().toString());
        facility1.setLocation(getRandomAddress());

        Facility facility2 = new Facility();
        facility2.setId(UUID.randomUUID().toString());
        facility2.setLocation(getRandomAddress());

        Facility disconnectedFacility = new Facility();
        disconnectedFacility.setId(UUID.randomUUID().toString());
        disconnectedFacility.setLocation(getRandomAddress());

        NetworkLane givenNetworkLane = getNetworkLaneWithOriginAndDestinationFacility();

        List<NetworkLaneSegment> networkLaneSegments = new ArrayList<>();
        networkLaneSegments.add(generateNetworkLaneSegment(givenNetworkLane.getOriginFacility(), facility1));
        networkLaneSegments.add(generateNetworkLaneSegment(facility1, facility2));
        networkLaneSegments.add(generateNetworkLaneSegment(facility2, disconnectedFacility));

        givenNetworkLane.setNetworkLaneSegments(networkLaneSegments);
        return givenNetworkLane;
    }

    private static NetworkLane createScenario_originAndFirstSegmentLaneIsDisconnected() {

        Facility facility1 = new Facility();
        facility1.setId(UUID.randomUUID().toString());
        facility1.setLocation(getRandomAddress());

        Facility facility2 = new Facility();
        facility2.setId(UUID.randomUUID().toString());
        facility2.setLocation(getRandomAddress());

        Facility disconnectedFacility = new Facility();
        disconnectedFacility.setId(UUID.randomUUID().toString());
        disconnectedFacility.setLocation(getRandomAddress());

        NetworkLane givenNetworkLane = getNetworkLaneWithOriginAndDestinationFacility();

        List<NetworkLaneSegment> networkLaneSegments = new ArrayList<>();
        networkLaneSegments.add(generateNetworkLaneSegment(disconnectedFacility, facility1));
        networkLaneSegments.add(generateNetworkLaneSegment(facility1, facility2));
        networkLaneSegments.add(generateNetworkLaneSegment(facility2, givenNetworkLane.getDestinationFacility()));

        givenNetworkLane.setNetworkLaneSegments(networkLaneSegments);
        return givenNetworkLane;
    }

    private static NetworkLane createScenario_missingLocationFromAnySegmentLanes() {

        Facility startFacility = new Facility();
        startFacility.setId(UUID.randomUUID().toString());
        startFacility.setLocation(null);

        Facility endFacility = new Facility();
        endFacility.setId(UUID.randomUUID().toString());
        endFacility.setLocation(getRandomAddress());

        List<NetworkLaneSegment> networkLaneSegments = new ArrayList<>();
        networkLaneSegments.add(generateNetworkLaneSegment(startFacility, endFacility));
        networkLaneSegments.add(generateNetworkLaneSegment(startFacility, endFacility));

        NetworkLane givenNetworkLane = getNetworkLaneWithOriginAndDestinationFacility();
        givenNetworkLane.setNetworkLaneSegments(networkLaneSegments);
        return givenNetworkLane;
    }

    private static NetworkLane createScenario_identicalFirstAndLastNodesFacility() {
        Facility facility1 = new Facility();
        facility1.setId("f-1");
        facility1.setLocation(getRandomAddress());

        Facility facility2 = new Facility();
        facility2.setId("f-2");
        facility2.setLocation(getRandomAddress());

        NetworkLaneSegment segment1 = new NetworkLaneSegment();
        segment1.setSequence("0");
        segment1.setStartFacility(facility1);
        segment1.setEndFacility(facility2);

        NetworkLaneSegment segment2 = new NetworkLaneSegment();
        segment2.setSequence("1");
        segment2.setStartFacility(facility2);
        segment2.setEndFacility(facility1);

        NetworkLane givenNetworkLane = new NetworkLane();
        givenNetworkLane.addNetworkLaneSegment(segment1);
        givenNetworkLane.addNetworkLaneSegment(segment2);
        return givenNetworkLane;
    }

    private static NetworkLane createScenario_identicalFirstAndLastNodesLocation() {
        Facility facility1 = new Facility();
        facility1.setLocation(getRandomAddress());

        Facility facility2 = new Facility();
        facility2.setId("f-2");
        facility2.setLocation(getRandomAddress());

        NetworkLaneSegment segment1 = new NetworkLaneSegment();
        segment1.setSequence("0");
        segment1.setStartFacility(facility1);
        segment1.setEndFacility(facility2);

        NetworkLaneSegment segment2 = new NetworkLaneSegment();
        segment2.setSequence("1");
        segment2.setStartFacility(facility2);
        segment2.setEndFacility(facility1);

        NetworkLane givenNetworkLane = new NetworkLane();
        givenNetworkLane.addNetworkLaneSegment(segment1);
        givenNetworkLane.addNetworkLaneSegment(segment2);
        return givenNetworkLane;
    }

    private static NetworkLane createScenario_notIdenticalFirstAndLastNodesWhenEmptyFacilityId() {
        Facility facility1 = new Facility();
        facility1.setId("");
        facility1.setLocation(getRandomAddress());

        Facility facility2 = new Facility();
        facility2.setId("");
        facility2.setLocation(getRandomAddress());

        NetworkLaneSegment segment1 = new NetworkLaneSegment();
        segment1.setSequence("0");
        segment1.setStartFacility(facility1);
        segment1.setEndFacility(facility2);

        NetworkLane givenNetworkLane = new NetworkLane();
        givenNetworkLane.addNetworkLaneSegment(segment1);
        return givenNetworkLane;
    }

    private static NetworkLane createScenario_notIdenticalFirstAndLastNodesWhenEmptyLocationId() {
        Address address1 = new Address();
        address1.setId(UUID.randomUUID().toString());
        Facility facility1 = new Facility();
        facility1.setLocation(address1);

        Address address2 = new Address();
        address2.setId(UUID.randomUUID().toString());
        Facility facility2 = new Facility();
        facility2.setLocation(address2);

        NetworkLaneSegment segment1 = new NetworkLaneSegment();
        segment1.setSequence("0");
        segment1.setStartFacility(facility1);
        segment1.setEndFacility(facility2);

        NetworkLane givenNetworkLane = new NetworkLane();
        givenNetworkLane.addNetworkLaneSegment(segment1);
        return givenNetworkLane;
    }

    // helper methods

    private static NetworkLaneSegment generateNetworkLaneSegment(Facility startFacility, Facility endFacility) {
        NetworkLaneSegment networkLaneSegment = new NetworkLaneSegment();
        networkLaneSegment.setPartner(generatePartner());
        networkLaneSegment.setSequence("1");
        networkLaneSegment.setOrganizationId(organizationId);
        networkLaneSegment.setStartFacility(startFacility);
        networkLaneSegment.setEndFacility(endFacility);
        return networkLaneSegment;
    }

    private static Partner generatePartner() {
        Partner partner = new Partner();
        partner.setName(partnerName);
        partner.setOrganizationId(organizationId);
        return partner;
    }

    private static Address getRandomAddress() {
        Address address = new Address();
        address.setId(UUID.randomUUID().toString());
        return address;
    }

    private static NetworkLane getNetworkLaneWithOriginAndDestinationFacility() {

        Facility originFacility = new Facility();
        originFacility.setId("f-1");
        originFacility.setLocation(getRandomAddress());

        Facility destinationFacility = new Facility();
        destinationFacility.setId("f-2");
        destinationFacility.setLocation(getRandomAddress());

        NetworkLane givenNetworkLane = new NetworkLane();
        givenNetworkLane.setOriginFacility(originFacility);
        givenNetworkLane.setDestinationFacility(destinationFacility);
        return givenNetworkLane;
    }


}
