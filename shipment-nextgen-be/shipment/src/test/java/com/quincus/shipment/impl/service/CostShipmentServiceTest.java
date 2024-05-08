package com.quincus.shipment.impl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.constant.LocationType;
import com.quincus.shipment.api.filter.CostShipmentFilterResult;
import com.quincus.shipment.api.filter.ShipmentFilter;
import com.quincus.shipment.impl.enricher.LocationCoverageCriteriaEnricher;
import com.quincus.shipment.impl.enricher.UserPartnerCriteriaEnricher;
import com.quincus.shipment.impl.mapper.CostShipmentMapper;
import com.quincus.shipment.impl.mapper.ShipmentCriteriaMapper;
import com.quincus.shipment.impl.repository.ShipmentRepository;
import com.quincus.shipment.impl.repository.criteria.ShipmentCriteria;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.repository.projection.CostShipmentProjectionListingPage;
import com.quincus.shipment.impl.repository.specification.ShipmentSpecification;
import com.quincus.shipment.impl.repository.specification.predicate.ShipmentLocationCoveragePredicate;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CostShipmentServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private CostShipmentService costShipmentService;
    @Mock
    private ShipmentRepository shipmentRepository;
    @Mock
    private CostShipmentProjectionListingPage costShipmentProjectionListingPage;
    @Mock
    private UserDetailsProvider userDetailsProvider;
    @Mock
    private CostShipmentMapper costShipmentMapper;
    @Mock
    private ShipmentCriteriaMapper shipmentCriteriaMapper;
    @Mock
    private List<ShipmentLocationCoveragePredicate> shipmentLocationCoveragePredicates;
    @Mock
    private LocationCoverageCriteriaEnricher locationCoverageCriteriaEnricher;

    @Mock
    private UserPartnerCriteriaEnricher userPartnerCriteriaEnricher;

    @BeforeEach
    void setUp() {
        costShipmentService = new CostShipmentService(
                objectMapper,
                shipmentRepository,
                costShipmentProjectionListingPage,
                userDetailsProvider,
                costShipmentMapper,
                shipmentCriteriaMapper,
                shipmentLocationCoveragePredicates,
                locationCoverageCriteriaEnricher,
                userPartnerCriteriaEnricher
        );
    }

    @Test
    void shouldFindByShipmentFilter() {
        ShipmentFilter shipmentFilter = new ShipmentFilter();
        shipmentFilter.setPageNumber(1);
        shipmentFilter.setSize(10);
        shipmentFilter.setCostKeys(new String[]{"id1", "id2", "id3"});

        Map<LocationType, List<String>> locationTypeMap = new HashMap<>();
        locationTypeMap.put(LocationType.STATE, Arrays.asList(UUID.randomUUID().toString(), UUID.randomUUID().toString()));

        ShipmentCriteria shipmentCriteria = new ShipmentCriteria();
        shipmentCriteria.setPage(shipmentFilter.getPageNumber() + 1);
        shipmentCriteria.setPerPage(shipmentFilter.getSize());
        shipmentCriteria.setCostKeys(shipmentFilter.getCostKeys());
        shipmentCriteria.setUserLocationCoverageIdsByType(locationTypeMap);

        when(shipmentCriteriaMapper.mapFilterToCriteria(shipmentFilter, objectMapper, shipmentLocationCoveragePredicates)).thenReturn(shipmentCriteria);
        List<ShipmentEntity> entities = new ArrayList<>();
        entities.add(new ShipmentEntity());
        when(shipmentRepository.count(any(ShipmentSpecification.class))).thenReturn(1L);
        when(costShipmentProjectionListingPage.findAllWithPagination(any(ShipmentSpecification.class), any(Pageable.class)))
                .thenReturn(entities);
        when(costShipmentMapper.mapEntitiesForCostListing(entities)).thenReturn(new ArrayList<>());

        CostShipmentFilterResult result = costShipmentService.findAllCostShipmentByFilter(shipmentFilter);

        assertThat(result.filter()).isEqualTo(shipmentFilter);
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.currentPage()).isEqualTo(1);
        assertThat(result.getResult()).isNotNull().isEmpty();
        verify(locationCoverageCriteriaEnricher, times(1)).enrichCriteriaWithUserLocationCoverage(shipmentCriteria);
        verify(userPartnerCriteriaEnricher, times(1)).enrichCriteriaByPartners(shipmentCriteria);
    }

    @Test
    void shouldReturnEmptyResultForNullEntities() {
        ShipmentFilter shipmentFilter = new ShipmentFilter();
        shipmentFilter.setPageNumber(1);
        shipmentFilter.setSize(10);

        ShipmentCriteria shipmentCriteria = new ShipmentCriteria();
        shipmentCriteria.setPage(shipmentFilter.getPageNumber() + 1);
        shipmentCriteria.setPerPage(shipmentFilter.getSize());

        when(shipmentCriteriaMapper.mapFilterToCriteria(shipmentFilter, objectMapper, shipmentLocationCoveragePredicates)).thenReturn(shipmentCriteria);

        CostShipmentFilterResult result = costShipmentService.findAllCostShipmentByFilter(shipmentFilter);

        assertThat(result.filter()).isEqualTo(shipmentFilter);
        assertThat(result.totalElements()).isZero();
        verify(locationCoverageCriteriaEnricher, times(1)).enrichCriteriaWithUserLocationCoverage(shipmentCriteria);
        verifyNoInteractions(userPartnerCriteriaEnricher);
    }
}
