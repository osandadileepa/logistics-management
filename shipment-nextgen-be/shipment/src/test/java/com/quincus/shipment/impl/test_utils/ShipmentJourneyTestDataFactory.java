package com.quincus.shipment.impl.test_utils;

import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Partner;
import com.quincus.shipment.api.domain.ShipmentJourney;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.junit.platform.commons.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.NONE)
public class ShipmentJourneyTestDataFactory {

    public static ShipmentJourney createShipmentJourney() {
        ShipmentJourney sj = new ShipmentJourney();
        Partner partner = new Partner();
        partner.setId("PartnerId");

        PackageJourneySegment pjs1 = new PackageJourneySegment();
        pjs1.setTransportType(TransportType.GROUND);
        pjs1.setRefId("1");
        pjs1.setSequence("1");
        pjs1.setPickUpTime("2016-10-05T08:20:10+05:30");
        pjs1.setDropOffTime("2016-10-05T08:21:10+05:30");
        pjs1.setPartner(partner);

        Facility startFacility1 = new Facility();
        startFacility1.setExternalId("blankThisIsOrigin");
        Facility endFacility1 = new Facility();
        endFacility1.setExternalId("facilityId1");
        pjs1.setStartFacility(startFacility1);
        pjs1.setEndFacility(endFacility1);

        PackageJourneySegment pjs2 = new PackageJourneySegment();
        pjs2.setTransportType(TransportType.GROUND);
        pjs2.setRefId("2");
        pjs2.setSequence("2");
        pjs2.setPickUpTime("2016-10-05T09:20:10+05:30");
        pjs2.setDropOffTime("2016-10-05T09:22:10+05:30");
        pjs2.setPartner(partner);

        Facility startFacility2 = new Facility();
        startFacility2.setExternalId("facilityId1");
        Facility endFacility2 = new Facility();
        endFacility2.setExternalId("facilityId2");
        pjs2.setStartFacility(startFacility2);
        pjs2.setEndFacility(endFacility2);

        PackageJourneySegment pjs3 = new PackageJourneySegment();
        pjs3.setTransportType(TransportType.GROUND);
        pjs3.setRefId("3");
        pjs3.setSequence("3");
        pjs3.setPickUpTime("2016-10-06T01:07:10+05:30");
        pjs3.setDropOffTime("2016-10-07T08:20:10+05:30");
        pjs3.setPartner(partner);

        Facility startFacility3 = new Facility();
        startFacility3.setExternalId("facilityId2");
        Facility endFacility3 = new Facility();
        endFacility3.setExternalId("facilityId3");
        pjs3.setStartFacility(startFacility3);
        pjs3.setEndFacility(endFacility3);

        PackageJourneySegment pjs4 = new PackageJourneySegment();
        pjs4.setTransportType(TransportType.GROUND);
        pjs4.setRefId("4");
        pjs4.setSequence("4");
        pjs4.setPickUpTime("2016-11-04T08:20:10+05:30");
        pjs4.setDropOffTime("2016-12-01T08:20:10+05:30");
        pjs4.setPartner(partner);

        Facility startFacility4 = new Facility();
        startFacility4.setExternalId("facilityId3");
        Facility endFacility4 = new Facility();
        endFacility4.setExternalId("thisisBlankThisShouldBeDestination");
        pjs4.setStartFacility(startFacility4);
        pjs4.setEndFacility(endFacility4);

        List<PackageJourneySegment> list = new ArrayList<>();
        list.add(pjs1);
        list.add(pjs2);
        list.add(pjs3);
        list.add(pjs4);
        sj.setPackageJourneySegments(list);
        return sj;
    }

    public static ShipmentJourney createDummyJourneyFromSegments(PackageJourneySegment... segments) {
        ShipmentJourney journey = new ShipmentJourney();
        journey.setJourneyId(UUID.randomUUID().toString());
        journey.setPackageJourneySegments(Arrays.stream(segments).toList());
        return journey;
    }

    public static PackageJourneySegment createDummyGroundSegment(String pickUpTime, String dropOffTime) {
        return createDummyGroundSegment(1, pickUpTime, dropOffTime);
    }

    public static PackageJourneySegment createDummyGroundSegment(int segmentPos, String pickUpTime, String dropOffTime) {
        String refId = Integer.toString(segmentPos);
        String sequence = Integer.toString(segmentPos - 1);
        PackageJourneySegment segment = createDummySegment(TransportType.GROUND, refId, sequence);

        segment.setPickUpTime(pickUpTime);
        segment.setDropOffTime(dropOffTime);

        return segment;
    }

    public static PackageJourneySegment createDummyAirSegment(String lockOutTime, String departureTime, String arrivalTime, String recoveryTime) {
        return createDummyAirSegment(1, lockOutTime, departureTime, arrivalTime, recoveryTime);
    }

    public static PackageJourneySegment createDummyAirSegment(int segmentPos, String lockOutTime, String departureTime, String arrivalTime, String recoveryTime) {
        String refId = Integer.toString(segmentPos);
        String sequence = Integer.toString(segmentPos - 1);
        PackageJourneySegment segment = createDummySegment(TransportType.AIR, refId, sequence);

        segment.setLockOutTime(lockOutTime);
        segment.setDepartureTime(departureTime);
        segment.setArrivalTime(arrivalTime);
        segment.setRecoveryTime(recoveryTime);

        return segment;
    }

    public static PackageJourneySegment createDummySegment(TransportType transportType, String refId, String sequence) {
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId(UUID.randomUUID().toString());
        segment.setJourneyId(UUID.randomUUID().toString());
        segment.setTransportType(transportType);
        segment.setRefId(refId);
        segment.setSequence(sequence);

        return segment;
    }

    public static void enrichSegmentWithFacilities(PackageJourneySegment segment, String firstStartFacilityId, String firstEndFacilityId) {
        Facility firstStartFacility = new Facility();
        segment.setStartFacility(firstStartFacility);
        Facility firstEndFacility = new Facility();
        segment.setEndFacility(firstEndFacility);
        firstStartFacility.setExternalId(firstStartFacilityId);
        firstEndFacility.setExternalId(firstEndFacilityId);
    }

    public static void enrichSegmentWithPartner(PackageJourneySegment segment, String partnerId) {
        if (StringUtils.isBlank(partnerId)) {
            return;
        }

        Partner partner = new Partner();
        partner.setId(partnerId);
        segment.setPartner(partner);
    }
}
