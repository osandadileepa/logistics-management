package com.quincus.shipment.impl.helper;

import com.quincus.shipment.api.constant.LocationType;
import com.quincus.shipment.impl.enricher.LocationCoverageCriteriaEnricher;
import com.quincus.shipment.impl.repository.criteria.LocationCoverageCriteria;
import com.quincus.shipment.impl.repository.criteria.ShipmentCriteria;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.service.LocationService;
import com.quincus.web.common.multitenant.QuincusLocationCoverage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationCoverageCriteriaEnricherTest {
    @InjectMocks
    private LocationCoverageCriteriaEnricher locationCoverageCriteriaEnricher;
    @Mock
    private LocationService locationService;
    @Mock
    private UserDetailsProvider userDetailsProvider;


    @Test
    @DisplayName("GIVEN userDetailsProvider returns null location coverage WHEN enrichCriteriaWithUserLocationCoverage THEN setUserLocationCoverageIdsByType is not triggered ")
    void enrichCriteriaWithUserLocationCoverage_noUserLocationCoverage_NoUserLocationCoverageCriteriaSet() {
        //given:
        LocationCoverageCriteria locationCoverageCriteria = Mockito.mock(LocationCoverageCriteria.class);
        when(userDetailsProvider.getCurrentLocationCoverages()).thenReturn(null);

        //when:
        locationCoverageCriteriaEnricher.enrichCriteriaWithUserLocationCoverage(locationCoverageCriteria);

        //then:
        verify(locationCoverageCriteria, times(0)).setUserLocationCoverageIdsByType(any());
    }

    @Test
    @DisplayName("GIVEN userDetailsProvider returns location coverage WHEN enrichCriteriaWithUserLocationCoverage THEN correctly set UserLocationCoverageIdsByType ")
    void enrichCriteriaWithUserLocationCoverage_LocationShouldBeSetInCriteria() {
        //given:
        ShipmentCriteria shipmentCriteria = new ShipmentCriteria();
        QuincusLocationCoverage locationCoverage1 = new QuincusLocationCoverage();
        locationCoverage1.setId(UUID.randomUUID().toString());
        QuincusLocationCoverage locationCoverage2 = new QuincusLocationCoverage();
        locationCoverage2.setId(UUID.randomUUID().toString());
        QuincusLocationCoverage locationCoverage3 = new QuincusLocationCoverage();
        locationCoverage3.setId(UUID.randomUUID().toString());

        String locationId1 = UUID.randomUUID().toString();
        String locationId2 = UUID.randomUUID().toString();
        String locationId3 = UUID.randomUUID().toString();
        LocationEntity location1 = new LocationEntity();
        location1.setId(locationId1);
        location1.setType(LocationType.STATE);
        LocationEntity location2 = new LocationEntity();
        location2.setId(locationId2);
        location2.setType(LocationType.FACILITY);
        LocationEntity location3 = new LocationEntity();
        location3.setId(locationId3);
        location3.setType(LocationType.FACILITY);

        when(userDetailsProvider.getCurrentLocationCoverages()).thenReturn(Arrays.asList(locationCoverage1, locationCoverage2, locationCoverage3));
        when(locationService.findByOrganizationIdAndExternalIds(anyList())).thenReturn(Arrays.asList(location1, location2, location3));

        //when:
        locationCoverageCriteriaEnricher.enrichCriteriaWithUserLocationCoverage(shipmentCriteria);

        //then:
        assertThat(shipmentCriteria.getUserLocationCoverageIdsByType().get(LocationType.STATE))
                .withFailMessage("State location coverage mismatch.")
                .hasSize(1);
        assertThat(shipmentCriteria.getUserLocationCoverageIdsByType().get(LocationType.FACILITY))
                .withFailMessage("Facility location coverage mismatch.")
                .hasSize(2);
        assertThat(shipmentCriteria.getUserLocationCoverageIdsByType().get(LocationType.COUNTRY))
                .withFailMessage("Country location coverage mismatch.")
                .isNull();
        assertThat(shipmentCriteria.getUserLocationCoverageIdsByType().get(LocationType.CITY))
                .withFailMessage("City location coverage mismatch.")
                .isNull();

    }


}
