package com.quincus.shipment.impl.helper;

import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MilestoneTimezoneHelperTest {

    @InjectMocks
    private MilestoneTimezoneHelper milestoneTimezoneHelper;

    @Test
    void givenMilestoneWithEtaAndProofOfDeliveryWithSegmentHavingEndLocationTimezone_whenSupplyLocationSpecificTimezone_thenProperlySupplyTimezone() {

        Milestone domain = new Milestone();
        domain.setEta(OffsetDateTime.now());
        domain.setSegmentId("123");
        domain.setProofOfDeliveryTime(OffsetDateTime.now());
        domain.setHubTimeZone("Asia/Manila UTC+08:00");

        milestoneTimezoneHelper.supplyEtaAndProofOfDeliveryTimezoneFromHubTimezone(domain);
        assertThat(domain.getProofOfDeliveryTimezone()).isEqualTo("UTC+08:00");
        assertThat(domain.getEtaTimezone()).isEqualTo("UTC+08:00");
    }

    @Test
    void givenMilestone_whenSupplyMilestoneTimezone_thenUseUserLocationTimezoneToPopulateMilestoneTimezone() {

        Milestone domain = new Milestone();
        domain.setHubTimeZone("Asia/Jakarta UTC+07:00");
        domain.setMilestoneTime(OffsetDateTime.now());

        milestoneTimezoneHelper.supplyMilestoneTimezoneFromHubTimezone(domain);
        assertThat(domain.getMilestoneTimezone()).isEqualTo("UTC+07:00");
    }

    @Test
    void givenMilestoneWithoutHubTimezone_whenSupplyMilestoneTimezone_thenShouldHandleAndUseUTCTimezone() {
        Milestone domain = new Milestone();
        domain.setMilestoneTime(OffsetDateTime.now());

        milestoneTimezoneHelper.supplyMilestoneTimezoneFromHubTimezone(domain);
        assertThat(domain.getMilestoneTimezone()).isEqualTo("UTC+00:00");
    }

    @Test
    void givenEtaAndProofOfDeliveryDateSegmentDomain_whenSupplyLocationSpecificTimezone_thenShouldSupplyTimezone() {
        Milestone domain = new Milestone();
        domain.setEta(OffsetDateTime.now());
        domain.setProofOfDeliveryTime(OffsetDateTime.now());

        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setStartFacility(createFacilityWithTimezone("Europe/Kirov UTC+03:00"));
        segment.setEndFacility(createFacilityWithTimezone("Asia/Manila UTC+08:00"));
        milestoneTimezoneHelper.supplyEtaAndProofOfDeliveryTimezoneFromSegmentEndFacilityTimezone(domain, segment);
        assertThat(domain.getEtaTimezone()).isEqualTo("UTC+08:00");
        assertThat(domain.getProofOfDeliveryTimezone()).isEqualTo("UTC+08:00");
    }

    @Test
    void givenEtaAndProofOfDeliveryDateSegmentEntity_whenSupplyLocationSpecificTimezone_thenShouldSupplyTimezone() {
        Milestone domain = new Milestone();
        domain.setEta(OffsetDateTime.now());
        domain.setProofOfDeliveryTime(OffsetDateTime.now());

        PackageJourneySegmentEntity segment = new PackageJourneySegmentEntity();
        segment.setStartLocationHierarchy(createLocationHierarchyWithFacilityTimezone("Asia/Manila UTC+08:00"));
        segment.setEndLocationHierarchy(createLocationHierarchyWithFacilityTimezone("Europe/Kirov UTC+03:00"));
        milestoneTimezoneHelper.supplyEtaAndProofOfDeliveryTimezoneFromSegmentEndFacilityTimezone(domain, segment);

        assertThat(domain.getEtaTimezone()).isEqualTo("UTC+03:00");
        assertThat(domain.getProofOfDeliveryTimezone()).isEqualTo("UTC+03:00");
    }

    @Test
    void givenEtaAndProofOfDeliveryDateWithFacilityNoTimezone_whenSupplyLocationSpecificTimezone_thenShouldSupplyTimezoneFromCity() {
        Milestone domain = new Milestone();
        domain.setEta(OffsetDateTime.now());
        domain.setProofOfDeliveryTime(OffsetDateTime.now());

        PackageJourneySegmentEntity segment = new PackageJourneySegmentEntity();
        segment.setStartLocationHierarchy(createLocationHierarchyWithoutFacilityTimezone("Europe/Kirov UTC+03:00"));
        segment.setEndLocationHierarchy(createLocationHierarchyWithoutFacilityTimezone("Asia/Manila UTC+08:00"));
        milestoneTimezoneHelper.supplyEtaAndProofOfDeliveryTimezoneFromSegmentEndFacilityTimezone(domain, segment);

        assertThat(domain.getEtaTimezone()).isEqualTo("UTC+08:00");
        assertThat(domain.getProofOfDeliveryTimezone()).isEqualTo("UTC+08:00");
    }

    private Facility createFacilityWithTimezone(String timezoneData) {
        Facility facility = new Facility();
        facility.setTimezone(timezoneData);
        return facility;
    }

    private LocationHierarchyEntity createLocationHierarchyWithFacilityTimezone(String timezoneData) {
        LocationEntity facility = new LocationEntity();
        facility.setTimezone(timezoneData);
        LocationHierarchyEntity locationHierarchyEntity = new LocationHierarchyEntity();
        locationHierarchyEntity.setFacility(facility);
        return locationHierarchyEntity;
    }

    private LocationHierarchyEntity createLocationHierarchyWithoutFacilityTimezone(String timezoneData) {
        LocationEntity facility = new LocationEntity();
        LocationEntity city = new LocationEntity();
        city.setTimezone(timezoneData);
        LocationHierarchyEntity locationHierarchyEntity = new LocationHierarchyEntity();
        locationHierarchyEntity.setFacility(facility);
        locationHierarchyEntity.setCity(city);
        return locationHierarchyEntity;
    }
}
