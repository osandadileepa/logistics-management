package com.quincus.shipment.impl.web;

import com.quincus.shipment.api.PackageJourneyAirSegmentApi;
import com.quincus.shipment.api.filter.PackageJourneyAirSegmentFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PackageJourneyAirSegmentControllerImplTest {
    private final int page = 1;
    private final int perPage = 10;
    @InjectMocks
    private PackageJourneyAirSegmentControllerImpl packageJourneySegmentController;
    @Mock
    private PackageJourneyAirSegmentApi filterApi;
    @Captor
    private ArgumentCaptor<PackageJourneyAirSegmentFilter> captor;

    @Test
    void findAirlinesByOrganizationId_validParams_shouldCallFilterAPIAndHaveValidArgs() {

        packageJourneySegmentController.findAirlines(perPage, page);
        verify(filterApi, times(1)).findAirlinesByOrganizationId(any(PackageJourneyAirSegmentFilter.class));
        verify(filterApi).findAirlinesByOrganizationId(captor.capture());

        assertThat(captor.getValue().getPerPage()).isEqualTo(perPage);
        assertThat(captor.getValue().getPage()).isEqualTo(page);
    }

    @Test
    void findByAirlineAndFlightNumbersAndOrganizationId_validParams_shouldCallFilterAPIAndHaveValidArgs() {
        String airline = "Acacia Estates Airlines";

        packageJourneySegmentController.findFlightNumbersByAirline(airline, perPage, page);
        verify(filterApi, times(1)).findFlightNumbersByAirlineAndOrganizationId(any(PackageJourneyAirSegmentFilter.class));
        verify(filterApi).findFlightNumbersByAirlineAndOrganizationId(captor.capture());

        assertThat(captor.getValue().getAirline()).isEqualTo(airline);
        assertThat(captor.getValue().getPerPage()).isEqualTo(perPage);
        assertThat(captor.getValue().getPage()).isEqualTo(page);
    }

    @Test
    void findAirlineOrFlightNumberByKeywordAndOrganizationId_validParams_shouldCallFilterAPIAndHaveValidArgs() {
        String keyword = "air";
        int level = 1;

        packageJourneySegmentController.findAirlineOrFlightNumberByKeyword(keyword, perPage, page, level);
        verify(filterApi, times(1)).findAirlineOrFlightNumberByKeywordAndOrganizationId(any(PackageJourneyAirSegmentFilter.class));
        verify(filterApi).findAirlineOrFlightNumberByKeywordAndOrganizationId(captor.capture());

        assertThat(captor.getValue().getKey()).isEqualTo(keyword);
        assertThat(captor.getValue().getPerPage()).isEqualTo(perPage);
        assertThat(captor.getValue().getPage()).isEqualTo(page);
        assertThat(captor.getValue().getLevel()).isEqualTo(level);
    }
}