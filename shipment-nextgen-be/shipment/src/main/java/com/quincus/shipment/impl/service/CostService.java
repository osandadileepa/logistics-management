package com.quincus.shipment.impl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.constant.CostCategory;
import com.quincus.shipment.api.constant.SegmentType;
import com.quincus.shipment.api.domain.Cost;
import com.quincus.shipment.api.domain.CostSegment;
import com.quincus.shipment.api.domain.CostShipment;
import com.quincus.shipment.api.domain.CostType;
import com.quincus.shipment.api.domain.Currency;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.User;
import com.quincus.shipment.api.dto.CostSearchResponse;
import com.quincus.shipment.api.exception.CostNotFoundException;
import com.quincus.shipment.api.exception.InvalidCostException;
import com.quincus.shipment.api.exception.QPortalUpsertException;
import com.quincus.shipment.api.filter.CostDateRange;
import com.quincus.shipment.api.filter.CostFilter;
import com.quincus.shipment.api.filter.CostFilterResult;
import com.quincus.shipment.api.filter.CostShipmentFilterResult;
import com.quincus.shipment.api.filter.ShipmentFilter;
import com.quincus.shipment.impl.enricher.UserPartnerCriteriaEnricher;
import com.quincus.shipment.impl.mapper.CostCriteriaMapper;
import com.quincus.shipment.impl.mapper.CostMapper;
import com.quincus.shipment.impl.repository.CostRepository;
import com.quincus.shipment.impl.repository.criteria.CostCriteria;
import com.quincus.shipment.impl.repository.entity.CostEntity;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.validator.ProofOfCostValidator;
import com.quincus.web.common.exception.model.QuincusValidationException;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static com.quincus.ext.DateTimeUtil.ZONE_UTC;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class CostService {
    private static final String INCURRED_DATE_RANGE_VALIDATION_MESSAGE = "Both 'Incurred date from' and 'Incurred date to' must be provided";
    private static final String ERR_QPORTAL_UPSERT_FAILED = "`%s` with id: `%s` of orgId: `%s` not found from QPortal";
    private static final String ERR_COST_NOT_FOUND = "Cost with id: `%s` not found";
    private static final String ERR_SHIPMENT_NOT_FOUND = "Shipment with id: `%s` not found";
    private static final String ERR_SEGMENT_NOT_FOUND = "Segment with id: `%s` does not belong to shipment with id: `%s`";
    private static final String ERR_INVALID_AMOUNT_DUE_TO_COST_CATEGORY = "Amount must not have decimal places when cost category is TIME_BASED";
    private static final String ERR_MANDATORY_PROOF_OF_COST = "Proof of cost is mandatory for specified cost type";
    private final QPortalService qPortalService;
    private final ShipmentService shipmentService;
    private final CostRepository costRepository;
    private final CostMapper costMapper;
    private final CostCriteriaMapper costCriteriaMapper;
    private final ObjectMapper objectMapper;
    private final UserDetailsProvider userDetailsProvider;
    private final CostShipmentService costShipmentService;
    private final ProofOfCostValidator proofOfCostValidator;
    private final UserPartnerCriteriaEnricher userPartnerCriteriaEnricher;
    private final CostPostProcessService costPostProcessService;

    public Cost find(String id) {
        return costMapper.toDomain(findByIdOrThrow(id));
    }

    @Transactional
    public Cost create(Cost cost) {
        enrichWithCreationDetails(cost);
        enrichAndValidateCost(cost);
        Cost response = costMapper.toDomain(costRepository.save(costMapper.toEntity(cost)));
        costPostProcessService.publishCostCreatedEventAndSendAdditionalCharges(cost);
        return response;
    }

    @Transactional
    public Cost update(Cost cost, String id) {
        enrichWithModificationDetails(cost);
        enrichAndValidateCost(cost);
        Cost response = costMapper.toDomain(costRepository.save(costMapper.update(cost, findByIdOrThrow(id))));
        costPostProcessService.publishCostUpdatedEventAndSendAdditionalCharges(cost);
        return response;
    }

    public CostFilterResult findAllByFilter(CostFilter costFilter) {
        validateIncurredDateRange(costFilter.getIncurredDateRange());
        CostCriteria costCriteria = costCriteriaMapper.mapFilterToCriteria(costFilter, userDetailsProvider.getCurrentOrganizationId());
        if (CollectionUtils.isEmpty(userDetailsProvider.getCurrentLocationCoverageIds())) {
            return emptyFilterResult(costFilter);
        }
        costCriteria.setPartnerId(userDetailsProvider.getCurrentPartnerId());
        costCriteria.setUserLocationsCoverage(new HashSet<>(userDetailsProvider.getCurrentLocationCoverageIds()));
        userPartnerCriteriaEnricher.enrichCriteriaByPartners(costCriteria);
        costCriteria.setObjectMapper(objectMapper);
        Page<CostEntity> costEntities = costRepository.findAll(costCriteria.buildSpecification(), costCriteria.pageRequest());
        List<CostSearchResponse> result = costEntities.map(costMapper::toCostResponse).stream().toList();
        return new CostFilterResult(result)
                .filter(costFilter)
                .totalPages(costEntities.getTotalPages())
                .totalElements(costEntities.getTotalElements())
                .page(costCriteria.getPage());
    }

    public CostShipmentFilterResult findAllCostShipmentByFilter(ShipmentFilter shipmentFilter) {
        shipmentFilter.setOrganization(userDetailsProvider.getCurrentOrganization());
        return costShipmentService.findAllCostShipmentByFilter(shipmentFilter);
    }

    private CostEntity findByIdOrThrow(String id) {
        return costRepository.findById(id)
                .orElseThrow(() -> new CostNotFoundException(String.format(ERR_COST_NOT_FOUND, id)));
    }

    private void validateIncurredDateRange(CostDateRange costDateRange) {
        Optional.ofNullable(costDateRange)
                .ifPresent(range -> {
                    if (range.getIncurredDateFrom() == null || range.getIncurredDateTo() == null) {
                        throw new QuincusValidationException(INCURRED_DATE_RANGE_VALIDATION_MESSAGE);
                    }
                });
    }

    private CostFilterResult emptyFilterResult(CostFilter costFilter) {
        return new CostFilterResult(List.of())
                .filter(costFilter)
                .totalElements(0)
                .totalPages(0)
                .page(1);
    }

    private void enrichAndValidateCost(Cost cost) {
        cost.setOrganizationId(userDetailsProvider.getCurrentOrganizationId());
        upsertCostType(cost);
        checkCostValidity(cost);
        upsertCurrencyWhenNonTimeBased(cost);
        upsertDriverAndPartner(cost);
        enrichShipments(cost);
    }

    private void enrichWithCreationDetails(Cost cost) {
        cost.setId(null);
        cost.setCreatedBy(userDetailsProvider.getCurrentUserFullName());
        cost.setModifiedBy(userDetailsProvider.getCurrentUserFullName());
        cost.setCreatedTimezone(ZONE_UTC);
    }

    private void enrichWithModificationDetails(Cost cost) {
        cost.setModifiedBy(userDetailsProvider.getCurrentUserFullName());
    }

    private void checkCostValidity(Cost cost) {
        ensureTimeBasedCostHasNoDecimals(cost);
        ensureMandatoryProofIsProvided(cost);
        validateProofOfCost(cost);
    }

    private void ensureTimeBasedCostHasNoDecimals(Cost cost) {
        if (cost.getCostType().getCategory() == CostCategory.TIME_BASED &&
                countDecimalPlaces(cost.getCostAmount()) != 0) {
            throw new InvalidCostException(ERR_INVALID_AMOUNT_DUE_TO_COST_CATEGORY);
        }
    }

    private void ensureMandatoryProofIsProvided(Cost cost) {
        if (StringUtils.equals("mandatory", cost.getCostType().getProof()) && cost.getProofOfCost().isEmpty()) {
            throw new InvalidCostException(ERR_MANDATORY_PROOF_OF_COST);
        }
    }

    private void validateProofOfCost(Cost cost) {
        proofOfCostValidator.validate(cost.getProofOfCost());
    }

    private int countDecimalPlaces(BigDecimal bigDecimal) {
        return Math.max(0, bigDecimal.stripTrailingZeros().scale());
    }

    private void upsertCostType(Cost cost) {
        String costTypeId = cost.getCostType().getId();
        CostType costType = qPortalService.getCostType(costTypeId);
        if (costType == null) {
            throw new QPortalUpsertException(String.format(ERR_QPORTAL_UPSERT_FAILED, "CostType", costTypeId, userDetailsProvider.getCurrentOrganizationId()));
        }
        cost.setCostType(costType);
    }

    private void upsertCurrencyWhenNonTimeBased(Cost cost) {
        if (cost.getCostType().getCategory() == CostCategory.NON_TIME_BASED) {
            String currencyId = cost.getCurrency().getId();
            Currency currency = qPortalService.getCurrency(currencyId);
            if (currency == null) {
                throw new QPortalUpsertException(String.format(ERR_QPORTAL_UPSERT_FAILED, "Currency", currencyId, userDetailsProvider.getCurrentOrganizationId()));
            }
            cost.setCurrency(currency);
        }
    }

    private void upsertDriverAndPartner(Cost cost) {
        String driverId = cost.getDriverId();
        if (StringUtils.isNotBlank(driverId)) {
            User user = qPortalService.getUser(driverId);
            if (user == null) {
                throw new QPortalUpsertException(String.format(ERR_QPORTAL_UPSERT_FAILED, "Driver", driverId, userDetailsProvider.getCurrentOrganizationId()));
            }
            cost.setDriverId(user.getId());
            cost.setDriverName(user.getName());
            cost.setPartnerId(user.getPartnerId());
            cost.setPartnerName(user.getPartner());
        }
    }

    private void enrichShipments(Cost cost) {
        List<Shipment> shipmentEntityList = findShipmentsByIds(cost);
        cost.getShipments().forEach(costShipment -> {
            Shipment shipmentDomain = findShipmentInList(costShipment, shipmentEntityList);
            enrichShipmentWithOrderDetails(costShipment, shipmentDomain);
            enrichShipmentWithLocationDetails(costShipment, shipmentDomain);
            costShipment.setShipmentTrackingId(shipmentDomain.getShipmentTrackingId());
            costShipment.setExternalOrderId(shipmentDomain.getExternalOrderId());
            enrichSegments(cost, costShipment, shipmentDomain);
        });
    }

    private List<Shipment> findShipmentsByIds(Cost cost) {
        List<String> shipmentIds = cost.getShipments()
                .stream()
                .map(CostShipment::getId)
                .toList();
        return shipmentService.findAllByIds(shipmentIds);
    }

    private Shipment findShipmentInList(CostShipment costShipment, List<Shipment> shipmentEntityList) {
        return shipmentEntityList
                .stream()
                .filter(shipment -> StringUtils.equals(shipment.getId(), costShipment.getId()))
                .findFirst()
                .orElseThrow(() -> new InvalidCostException(String.format(ERR_SHIPMENT_NOT_FOUND, costShipment.getId())));
    }

    private void enrichShipmentWithOrderDetails(CostShipment costShipment, Shipment shipmentDomain) {
        costShipment.setOrderId(shipmentDomain.getOrder().getId());
        costShipment.setOrderIdLabel(shipmentDomain.getOrder().getOrderIdLabel());
        costShipment.setOrderStatus(shipmentDomain.getOrder().getStatus());
    }

    private void enrichShipmentWithLocationDetails(CostShipment costShipment, Shipment shipmentDomain) {
        costShipment.setOrigin(shipmentDomain.getOrigin().getCityName());
        costShipment.setDestination(shipmentDomain.getDestination().getCityName());
    }


    private void enrichSegments(Cost cost, CostShipment costShipment, Shipment shipmentDomain) {
        int numberOfSegments = shipmentDomain.getShipmentJourney().getPackageJourneySegments().size();
        costShipment.getSegments().forEach(costSegment -> {
            PackageJourneySegment segmentDomain = findSegmentInShipment(costSegment, shipmentDomain);
            enrichSegmentWithFacilityDetails(cost, costSegment, segmentDomain);
            enrichSegmentWithAdditionalDetails(costSegment, segmentDomain);
            checkSegmentType(costSegment, segmentDomain, numberOfSegments);
        });
    }

    private PackageJourneySegment findSegmentInShipment(CostSegment costSegment, Shipment shipmentDomain) {
        return shipmentDomain.getShipmentJourney().getPackageJourneySegments()
                .stream()
                .filter(pjs -> StringUtils.equals(pjs.getSegmentId(), costSegment.getSegmentId()))
                .findFirst()
                .orElseThrow(() -> new InvalidCostException(String.format(ERR_SEGMENT_NOT_FOUND, costSegment.getSegmentId(), shipmentDomain.getId())));
    }

    private void enrichSegmentWithFacilityDetails(Cost cost, CostSegment costSegment, PackageJourneySegment segmentDomain) {
        enrichWithStartFacilityDetails(cost, costSegment, segmentDomain.getStartFacility());
        enrichWithEndFacilityDetails(cost, costSegment, segmentDomain.getEndFacility());
    }

    private void enrichWithStartFacilityDetails(Cost cost, CostSegment costSegment, Facility startFacility) {
        if (startFacility != null) {
            addLocationExternalIds(cost, startFacility);
            costSegment.setStartFacility(costMapper.toCostFacility(startFacility));
        }
    }

    private void enrichWithEndFacilityDetails(Cost cost, CostSegment costSegment, Facility endFacility) {
        if (endFacility != null) {
            addLocationExternalIds(cost, endFacility);
            costSegment.setEndFacility(costMapper.toCostFacility(endFacility));
        }
    }

    private void addLocationExternalIds(Cost cost, Facility facility) {
        cost.addLocationExternalIds(facility.getExternalId());
        cost.addLocationExternalIds(facility.getLocation().getCountryId());
        cost.addLocationExternalIds(facility.getLocation().getStateId());
        cost.addLocationExternalIds(facility.getLocation().getCityId());
    }

    private void enrichSegmentWithAdditionalDetails(CostSegment costSegment, PackageJourneySegment segmentDomain) {
        costSegment.setRefId(segmentDomain.getRefId());
        costSegment.setTransportType(segmentDomain.getTransportType());
        costSegment.setSequenceNo(segmentDomain.getSequence());
    }

    private void checkSegmentType(CostSegment costSegment, PackageJourneySegment segmentDomain, int numberOfSegments) {
        if (segmentDomain.getType() == SegmentType.FIRST_MILE) {
            costSegment.setFirstSegment(true);
            costSegment.setLastSegment(numberOfSegments == 1);
        } else if (segmentDomain.getType() == SegmentType.LAST_MILE) {
            costSegment.setFirstSegment(numberOfSegments == 1);
            costSegment.setLastSegment(true);
        }
    }
}