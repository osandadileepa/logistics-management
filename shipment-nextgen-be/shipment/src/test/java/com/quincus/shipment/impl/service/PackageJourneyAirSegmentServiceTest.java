package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.dto.FlightDetailsResponse;
import com.quincus.shipment.api.dto.PackageJourneyAirSegmentResponse;
import com.quincus.shipment.api.filter.PackageJourneyAirSegmentFilter;
import com.quincus.shipment.api.filter.PackageJourneyAirSegmentFilterResult;
import com.quincus.shipment.impl.repository.PackageJourneySegmentRepository;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.test_utils.TupleDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import javax.persistence.Tuple;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackageJourneyAirSegmentServiceTest {

    @InjectMocks
    private PackageJourneyAirSegmentService packageJourneySegmentService;

    @Mock
    private PackageJourneySegmentRepository packageJourneySegmentRepository;

    @Mock
    private UserDetailsProvider userDetailsProvider;

    @Test
    @DisplayName("GIVEN filter with organization id WHEN findAirlines THEN return list")
    void shouldRetrieveWhenFindAirlines() {
        String orgId = "orgId";
        PackageJourneyAirSegmentFilter filter = generatePackageJourneyAirSegmentFilter(1);
        List<Tuple> airlines = mockAirlines();
        Page<Tuple> page = new PageImpl<>(airlines);

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(orgId);
        when(packageJourneySegmentRepository.findAirlinesByOrganizationId(eq(orgId), any())).thenReturn(page);

        PackageJourneyAirSegmentFilterResult result = packageJourneySegmentService.findAirlines(filter);

        assertThat(result.totalElements()).isEqualTo(airlines.size());

        verify(packageJourneySegmentRepository).findAirlinesByOrganizationId(eq(orgId), any());
    }

    @Test
    @DisplayName("GIVEN filter with non-existing organization id WHEN findAirlines THEN return empty")
    void shouldReturnEmptyWhenFindAirlines() {
        String orgId = "nonExistentOrgId";
        PackageJourneyAirSegmentFilter filter = generatePackageJourneyAirSegmentFilter(1);
        List<Tuple> airlines = new ArrayList<>();
        Page<Tuple> page = new PageImpl<>(airlines);

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(orgId);
        when(packageJourneySegmentRepository.findAirlinesByOrganizationId(eq(orgId), any())).thenReturn(page);

        PackageJourneyAirSegmentFilterResult result = packageJourneySegmentService.findAirlines(filter);

        assertThat(result.totalElements()).isZero();

        verify(packageJourneySegmentRepository).findAirlinesByOrganizationId(eq(orgId), any());
    }

    @Test
    @DisplayName("GIVEN filter with airline and organization id WHEN findFlightNumbers THEN return list")
    void shouldRetrieveWhenFindFlightNumbers() {
        String orgId = "orgId";
        String airline = "airline";
        PackageJourneyAirSegmentFilter filter = generatePackageJourneyAirSegmentFilter(2);
        filter.setAirline(airline);
        List<Tuple> airlines = mockAirlines();
        Page<Tuple> page = new PageImpl<>(airlines);

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(orgId);
        when(packageJourneySegmentRepository.findFlightNumbersByAirlineAndOrganizationId(eq(airline), eq(orgId), any())).thenReturn(page);

        PackageJourneyAirSegmentFilterResult result = packageJourneySegmentService.findFlightNumbers(filter);

        List<String> flightNumbers = result.result().stream().map(e -> ((FlightDetailsResponse) e).name()).toList();

        assertThat(flightNumbers).isEqualTo(List.of("1", "1b", "2", "3", "4", "5", "30", "200", "200a", "3000c"));
        verify(packageJourneySegmentRepository).findFlightNumbersByAirlineAndOrganizationId(eq(airline), eq(orgId), any());
    }

    @Test
    @DisplayName("GIVEN filter with non-existing airline, and organization id WHEN findFlightNumbers THEN return empty")
    void shouldReturnEmptyWhenFindFlightNumbers() {
        String orgId = "orgId";
        String nonExistentAirline = "nonExistentAirline";
        PackageJourneyAirSegmentFilter filter = generatePackageJourneyAirSegmentFilter(2);
        filter.setAirline(nonExistentAirline);
        List<Tuple> airlines = new ArrayList<>();
        Page<Tuple> page = new PageImpl<>(airlines);

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(orgId);
        when(packageJourneySegmentRepository.findFlightNumbersByAirlineAndOrganizationId(eq(nonExistentAirline), eq(orgId), any())).thenReturn(page);

        PackageJourneyAirSegmentFilterResult result = packageJourneySegmentService.findFlightNumbers(filter);

        assertThat(result.totalElements()).isZero();

        verify(packageJourneySegmentRepository).findFlightNumbersByAirlineAndOrganizationId(eq(nonExistentAirline), eq(orgId), any());
    }

    @Test
    @DisplayName("GIVEN filter valid keyword, level 1, and organization id WHEN findAirlinesOrFlightNumbers THEN return list")
    void shouldRetrieveWhenFindAirlinesOrFlightNumbers() {
        String orgId = "orgId";
        String keyword = "key";
        PackageJourneyAirSegmentFilter filter = generatePackageJourneyAirSegmentFilter(1);
        filter.setKey(keyword);
        List<Tuple> airlines = mockAirlines();
        Page<Tuple> page = new PageImpl<>(airlines);

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(orgId);
        when(packageJourneySegmentRepository.findAirlinesByKeyAndOrganizationId(eq(keyword), eq(orgId), any())).thenReturn(page);

        PackageJourneyAirSegmentFilterResult result = packageJourneySegmentService.findAirlinesOrFlightNumbers(filter);

        assertThat(result.totalElements()).isEqualTo(airlines.size());

        verify(packageJourneySegmentRepository).findAirlinesByKeyAndOrganizationId(eq(keyword), eq(orgId), any());
    }

    @Test
    @DisplayName("GIVEN filter unknown keyword ,level 1, and organization id WHEN findAirlinesOrFlightNumbers THEN return empty")
    void shouldReturnEmptyWhenFindAirlinesOrFlightNumbers() {
        String orgId = "orgId";
        String unknownKeyword = "unknownKeyword";
        PackageJourneyAirSegmentFilter filter = generatePackageJourneyAirSegmentFilter(1);
        filter.setKey(unknownKeyword);
        List<Tuple> airlines = new ArrayList<>();
        Page<Tuple> page = new PageImpl<>(airlines);

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(orgId);
        when(packageJourneySegmentRepository.findAirlinesByKeyAndOrganizationId(eq(unknownKeyword), eq(orgId), any())).thenReturn(page);

        PackageJourneyAirSegmentFilterResult result = packageJourneySegmentService.findAirlinesOrFlightNumbers(filter);

        assertThat(result.totalElements()).isZero();

        verify(packageJourneySegmentRepository).findAirlinesByKeyAndOrganizationId(eq(unknownKeyword), eq(orgId), any());
    }

    @Test
    @DisplayName("GIVEN filter valid keyword, level 2, and organization id WHEN findAirlinesOrFlightNumbers THEN return list")
    void shouldRetrieveWhenLevel2FindAirlinesOrFlightNumbers() {
        String orgId = "orgId";
        String keyword = "key";
        PackageJourneyAirSegmentFilter filter = generatePackageJourneyAirSegmentFilter(2);
        filter.setKey(keyword);
        List<Tuple> airlines = mockAirlines();
        Page<Tuple> page = new PageImpl<>(airlines);

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(orgId);
        when(packageJourneySegmentRepository.findFlightNumbersByKeyAndOrganizationId(eq(keyword), eq(orgId), any())).thenReturn(page);

        PackageJourneyAirSegmentFilterResult result = packageJourneySegmentService.findAirlinesOrFlightNumbers(filter);
        List<String> flightNumbers = ((PackageJourneyAirSegmentResponse) result.result().get(0)).flightNumbers().stream().map(FlightDetailsResponse::name).toList();

        assertThat(flightNumbers).isEqualTo(List.of("1", "1b", "30", "200", "200a", "3000c"));
        verify(packageJourneySegmentRepository).findFlightNumbersByKeyAndOrganizationId(eq(keyword), eq(orgId), any());
    }

    @Test
    @DisplayName("GIVEN filter unknown keyword ,level 2, and organization id WHEN findAirlinesOrFlightNumbers THEN return empty")
    void shouldReturnEmptyWhenLevel2FindAirlinesOrFlightNumbers() {
        String orgId = "orgId";
        String unknownKeyword = "unknownKeyword";
        PackageJourneyAirSegmentFilter filter = generatePackageJourneyAirSegmentFilter(2);
        filter.setKey(unknownKeyword);
        List<Tuple> airlines = new ArrayList<>();
        Page<Tuple> page = new PageImpl<>(airlines);

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(orgId);
        when(packageJourneySegmentRepository.findFlightNumbersByKeyAndOrganizationId(eq(unknownKeyword), eq(orgId), any())).thenReturn(page);

        PackageJourneyAirSegmentFilterResult result = packageJourneySegmentService.findAirlinesOrFlightNumbers(filter);

        assertThat(result.totalElements()).isZero();

        verify(packageJourneySegmentRepository).findFlightNumbersByKeyAndOrganizationId(eq(unknownKeyword), eq(orgId), any());
    }


    private PackageJourneyAirSegmentFilter generatePackageJourneyAirSegmentFilter(int level) {
        PackageJourneyAirSegmentFilter packageJourneySegmentFilter = new PackageJourneyAirSegmentFilter();
        packageJourneySegmentFilter.setPage(1);
        packageJourneySegmentFilter.setPerPage(10);
        packageJourneySegmentFilter.setKey("");
        packageJourneySegmentFilter.setLevel(level);
        return packageJourneySegmentFilter;
    }

    private List<Tuple> mockAirlines() {
        List<Tuple> tuples = new ArrayList<>();
        String uuid = UUID.randomUUID().toString();
        tuples.add(TupleDataFactory.ofPackageJourneyAirSegment(uuid, "American Airlines", "USA", "3000c"));
        tuples.add(TupleDataFactory.ofPackageJourneyAirSegment(uuid, "American Airlines", "USA", "200a"));
        tuples.add(TupleDataFactory.ofPackageJourneyAirSegment(uuid, "American Airlines", "USA", "1"));
        tuples.add(TupleDataFactory.ofPackageJourneyAirSegment(uuid, "American Airlines", "USA", "1b"));
        tuples.add(TupleDataFactory.ofPackageJourneyAirSegment(uuid, "American Airlines", "USA", "200"));
        tuples.add(TupleDataFactory.ofPackageJourneyAirSegment(uuid, "American Airlines", "USA", "30"));
        tuples.add(TupleDataFactory.ofPackageJourneyAirSegment(uuid, "Singapore Airlines", "SG", "2"));
        tuples.add(TupleDataFactory.ofPackageJourneyAirSegment(uuid, "Malaysia Airlines", "MY", "3"));
        tuples.add(TupleDataFactory.ofPackageJourneyAirSegment(uuid, "Philippine Airlines", "PR", "4"));
        tuples.add(TupleDataFactory.ofPackageJourneyAirSegment(uuid, "Tesla Airlines", "TESLA", "5"));
        return tuples;
    }
}
