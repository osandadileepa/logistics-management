package com.quincus.shipment.impl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import lombok.AllArgsConstructor;
import org.apache.commons.collections.MapUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class CostShipmentService {

    private final ObjectMapper objectMapper;
    private final ShipmentRepository shipmentRepository;
    private final CostShipmentProjectionListingPage costShipmentProjectionListingPage;
    private final UserDetailsProvider userDetailsProvider;
    private final CostShipmentMapper costShipmentMapper;
    private final ShipmentCriteriaMapper shipmentCriteriaMapper;
    private final List<ShipmentLocationCoveragePredicate> shipmentLocationCoveragePredicates;
    private final LocationCoverageCriteriaEnricher locationCoverageCriteriaEnricher;
    private final UserPartnerCriteriaEnricher userPartnerCriteriaEnricher;

    public CostShipmentFilterResult findAllCostShipmentByFilter(ShipmentFilter shipmentFilter) {
        ShipmentCriteria shipmentCriteria = shipmentCriteriaMapper.mapFilterToCriteria(shipmentFilter, objectMapper, shipmentLocationCoveragePredicates);
        shipmentCriteria.setPartnerId(userDetailsProvider.getCurrentPartnerId());
        locationCoverageCriteriaEnricher.enrichCriteriaWithUserLocationCoverage(shipmentCriteria);
        if (MapUtils.isEmpty(shipmentCriteria.getUserLocationCoverageIdsByType())) {
            return createEmptyShipmentFilter(shipmentFilter);
        }
        userPartnerCriteriaEnricher.enrichCriteriaByPartners(shipmentCriteria);
        ShipmentSpecification shipmentSpecification = shipmentCriteria.buildSpecification();
        Pageable page = shipmentSpecification.buildPageable();
        List<ShipmentEntity> result = costShipmentProjectionListingPage.findAllWithPagination(shipmentSpecification, page);
        long resultCount = shipmentRepository.count(shipmentSpecification);
        int currentPage = shipmentFilter.getPageNumber() + 1;
        shipmentFilter.setPageNumber(currentPage + 1);
        return new CostShipmentFilterResult(costShipmentMapper.mapEntitiesForCostListing(result))
                .filter(shipmentFilter)
                .totalElements(resultCount)
                .totalPages(costShipmentProjectionListingPage.getTotalNumberOfPages(resultCount, page.getPageSize()))
                .currentPage(currentPage);
    }

    private CostShipmentFilterResult createEmptyShipmentFilter(ShipmentFilter shipmentFilter) {
        return new CostShipmentFilterResult(List.of())
                .filter(shipmentFilter)
                .totalElements(0)
                .totalPages(0)
                .currentPage(1);
    }
}
