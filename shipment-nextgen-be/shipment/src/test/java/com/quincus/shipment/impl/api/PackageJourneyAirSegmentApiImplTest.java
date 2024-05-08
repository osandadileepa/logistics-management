package com.quincus.shipment.impl.api;

import com.quincus.shipment.api.filter.PackageJourneyAirSegmentFilter;
import com.quincus.shipment.impl.service.PackageJourneyAirSegmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PackageJourneyAirSegmentApiImplTest {

    @InjectMocks
    private PackageJourneyAirSegmentApiImpl filterApi;

    @Mock
    private PackageJourneyAirSegmentService packageJourneyAirSegmentService;

    @Test
    void shouldCallFindAirlinesByOrganizationIdOnce() {
        filterApi.findAirlinesByOrganizationId(new PackageJourneyAirSegmentFilter());
        verify(packageJourneyAirSegmentService, times(1)).findAirlines(any(PackageJourneyAirSegmentFilter.class));
    }

    @Test
    void shouldCallFindByAirlineAndOrganizationIdOnce() {
        filterApi.findFlightNumbersByAirlineAndOrganizationId(new PackageJourneyAirSegmentFilter());
        verify(packageJourneyAirSegmentService, times(1)).findFlightNumbers(any(PackageJourneyAirSegmentFilter.class));
    }

    @Test
    void shouldCallFindFlightNumbersByAirlineAndOrganizationIdOnce() {
        filterApi.findAirlineOrFlightNumberByKeywordAndOrganizationId(new PackageJourneyAirSegmentFilter());
        verify(packageJourneyAirSegmentService, times(1)).findAirlinesOrFlightNumbers(any(PackageJourneyAirSegmentFilter.class));
    }
}
