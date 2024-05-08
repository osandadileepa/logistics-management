package com.quincus.shipment.impl.helper;

import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Partner;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.shipment.impl.repository.entity.PartnerEntity;
import com.quincus.shipment.impl.service.LocationHierarchyService;
import com.quincus.shipment.impl.service.PartnerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SegmentReferenceProviderTest {

    @Mock
    private PartnerService partnerService;
    @Mock
    LocationHierarchyService locationHierarchyService;
    @InjectMocks
    private SegmentReferenceProvider segmentReferenceProvider;

    @Test
    void givenSegments_whenGenerateReference_thenPartnerAndLocationHierarchyEntitiesWillBeLoadedForReference() {

        Partner partner1 = createPartner("p1");
        Partner partner2 = createPartner("p2");

        PackageJourneySegment segment1 = new PackageJourneySegment();
        segment1.setPartner(partner1);
        segment1.setStartFacility(createFacility("startFacility1"));
        PackageJourneySegment segment2 = new PackageJourneySegment();
        segment2.setPartner(partner2);
        PackageJourneySegment segment3 = new PackageJourneySegment();
        segment3.setEndFacility(createFacility("endFacilityId3"));

        PartnerEntity partnerEntity = createPartnerEntity("p1");
        PartnerEntity partnerEntity2 = createPartnerEntity("p2");

        LocationHierarchyEntity lh1 = createLocationHierarchyEntity("startFacility1");
        LocationHierarchyEntity lh2 = createLocationHierarchyEntity("endFacilityId3");

        when(partnerService.findAllByExternalIds(List.of("p1", "p2"))).thenReturn(List.of(partnerEntity, partnerEntity2));
        when(locationHierarchyService.findLocationHierarchyByFacilityExternalIds(List.of("startFacility1", "endFacilityId3"))).thenReturn(List.of(lh1, lh2));

        SegmentReferenceHolder segmentReferenceHolder = segmentReferenceProvider.generateReference(List.of(segment1, segment2, segment3));
        assertThat(segmentReferenceHolder).isNotNull();
        assertThat(segmentReferenceHolder.getPartnerBySegmentId()).containsKey("p1").containsKey("p2");
        assertThat(segmentReferenceHolder.getLocationHierarchyByFacilityExtId()).containsKey("countryExtIdstateExtIdcityExtIdstartFacility1").containsKey("countryExtIdstateExtIdcityExtIdendFacilityId3");
    }

    private Facility createFacility(String externalId) {
        Facility facility = new Facility();
        facility.setExternalId(externalId);

        Address facilityAddress = new Address();
        facilityAddress.setCountryId("countryExtId");
        facilityAddress.setStateId("stateExtId");
        facilityAddress.setCityId("cityExtId");
        facility.setLocation(facilityAddress);
        return facility;
    }

    private Partner createPartner(String externalId) {
        Partner partner = new Partner();
        partner.setId(externalId);
        return partner;
    }

    private PartnerEntity createPartnerEntity(String externalId) {
        PartnerEntity partnerEntity = new PartnerEntity();
        partnerEntity.setExternalId(externalId);
        return partnerEntity;
    }

    private LocationHierarchyEntity createLocationHierarchyEntity(String externalId) {
        LocationEntity facility = new LocationEntity();
        facility.setExternalId(externalId);

        LocationEntity country = new LocationEntity();
        country.setExternalId("countryExtId");

        LocationEntity state = new LocationEntity();
        state.setExternalId("stateExtId");

        LocationEntity city = new LocationEntity();
        city.setExternalId("cityExtId");

        LocationHierarchyEntity locationHierarchy = new LocationHierarchyEntity();
        locationHierarchy.setFacility(facility);
        locationHierarchy.setCountry(country);
        locationHierarchy.setState(state);
        locationHierarchy.setCity(city);
        locationHierarchy.setExternalId(externalId);
        return locationHierarchy;
    }
}