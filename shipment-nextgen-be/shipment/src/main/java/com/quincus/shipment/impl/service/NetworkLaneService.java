package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.NetworkLane;
import com.quincus.shipment.api.domain.NetworkLaneSegment;
import com.quincus.shipment.api.exception.NetworkLaneNotFoundException;
import com.quincus.shipment.api.filter.NetworkLaneFilter;
import com.quincus.shipment.api.filter.NetworkLaneFilterResult;
import com.quincus.shipment.impl.helper.NetworkLaneDataHelper;
import com.quincus.shipment.impl.mapper.NetworkLaneCriteriaMapper;
import com.quincus.shipment.impl.mapper.NetworkLaneMapper;
import com.quincus.shipment.impl.mapper.NetworkLaneSegmentMapper;
import com.quincus.shipment.impl.repository.NetworkLaneRepository;
import com.quincus.shipment.impl.repository.criteria.NetworkLaneCriteria;
import com.quincus.shipment.impl.repository.entity.NetworkLaneEntity;
import com.quincus.shipment.impl.repository.entity.NetworkLaneSegmentEntity;
import com.quincus.shipment.impl.repository.entity.PartnerEntity;
import com.quincus.shipment.impl.repository.entity.ServiceTypeEntity;
import com.quincus.shipment.impl.repository.projection.NetworkLaneProjectionListingPage;
import com.quincus.shipment.impl.repository.specification.NetworkLaneSpecification;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.web.common.exception.model.QuincusException;
import com.quincus.web.common.exception.model.QuincusValidationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
@Slf4j
@AllArgsConstructor
@Transactional(readOnly = true)
public class NetworkLaneService {
    private static final String LOCATION_MISMATCH_ERROR = "Location: Mismatch `%s` , `%s` , `%s`";
    private static final String FACILITY_MISMATCH_ERROR = "Location: Mismatch `%s` , `%s` , `%s`, `%s`";
    private static final String UNEXPECTED_ERROR = "Unexpected error happened during saving this record";
    private static final String ERR_NETWORK_LANE_NOT_FOUND = "NetworkLane Id %s not found.";

    private final NetworkLaneRepository networkLaneRepository;
    private final NetworkLaneMapper networkLaneMapper;
    private final NetworkLaneSegmentMapper networkLaneSegmentMapper;
    private final ServiceTypeAsyncService serviceTypeAsyncService;
    private final FacilityAsyncService facilityService;
    private final LocationHierarchyAsyncService locationHierarchyAsyncService;
    private final AddressAsyncService addressAsyncService;
    private final PartnerAsyncService partnerAsyncService;
    private final NetworkLaneCriteriaMapper networkLaneCriteriaMapper;
    private final NetworkLaneProjectionListingPage projectionListingPage;
    private final NetworkLaneEnrichmentService networkLaneEnrichmentService;
    private final UserDetailsProvider userDetailsProvider;
    private final NetworkLaneDataHelper networkLaneDataHelper;

    private void assignExistingServiceType(NetworkLaneEntity networkLane) {
        ServiceTypeEntity serviceType = networkLane.getServiceType();
        if (Objects.isNull(serviceType)) {
            return;
        }
        networkLane.setServiceType(Optional.ofNullable(serviceTypeAsyncService.find(serviceType.getCode(), serviceType.getOrganizationId()))
                .orElse(serviceType));
    }

    @Transactional(noRollbackFor = {QuincusValidationException.class, QuincusException.class})
    public void saveFromBulkUpload(NetworkLane networkLane, String organizationId) {
        try {
            validateAndEnrichNetworkLane(networkLane, organizationId);
            NetworkLaneEntity entity = networkLaneMapper.mapDomainToEntity(networkLane);
            assignExistingServiceType(entity);
            setupOriginAndDestinationLocationHierarchy(networkLane, entity);
            entity.addAllNetworkLaneSegments(createNetworkLaneSegmentEntitiesWithOrganizationId(networkLane, entity, organizationId));
            networkLaneDataHelper.setSegmentTypeOnNetworkLaneSegments(entity);
            networkLaneDataHelper.enrichLaneSegmentTimezoneFields(entity);
            networkLaneRepository.saveAndFlush(entity);
        } catch (QuincusValidationException qe) {
            throw qe;
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            throw new QuincusException(UNEXPECTED_ERROR);
        }
    }

    private void setupOriginAndDestinationLocationHierarchy(NetworkLane domain, NetworkLaneEntity entity) {
        if (domain.getOriginFacility() != null) {
            entity.setOrigin(locationHierarchyAsyncService.setUpLocationHierarchies(domain.getOriginFacility(),
                    entity.getOrganizationId()));
        } else if (domain.getOrigin() != null) {
            entity.setOrigin(locationHierarchyAsyncService.setUpLocationHierarchies(domain.getOrigin(),
                    entity.getOrganizationId()));
        }
        if (domain.getDestinationFacility() != null) {
            entity.setDestination(locationHierarchyAsyncService.setUpLocationHierarchies(domain.getDestinationFacility(),
                    entity.getOrganizationId()));
        } else if (domain.getDestination() != null) {
            entity.setDestination(locationHierarchyAsyncService.setUpLocationHierarchies(domain.getDestination(),
                    entity.getOrganizationId()));
        }
    }

    private void setUpStartAndEndFacility(
            final NetworkLaneEntity networkLaneEntity,
            final NetworkLaneSegment segmentDomain,
            final NetworkLaneSegmentEntity segmentEntity,
            final String organizationId,
            final int currentIndex,
            final int segmentSize) {

        if (currentIndex == 0) {
            segmentEntity.setStartLocationHierarchy(networkLaneEntity.getOrigin());
        } else if (Optional.ofNullable(segmentDomain.getStartFacility()).map(Facility::getExternalId).isPresent()) {
            segmentEntity.setStartLocationHierarchy(locationHierarchyAsyncService.setUpLocationHierarchies(segmentDomain.getStartFacility(), organizationId));
        } else {
            throw new QuincusValidationException(String.format("Start Facility on Network lane Connection sequence: %s is mandatory", segmentDomain.getSequence()));
        }

        if (currentIndex == segmentSize - 1) {
            segmentEntity.setEndLocationHierarchy(networkLaneEntity.getDestination());
        } else if (Optional.ofNullable(segmentDomain.getEndFacility()).map(Facility::getExternalId).isPresent()) {
            segmentEntity.setEndLocationHierarchy(locationHierarchyAsyncService.setUpLocationHierarchies(segmentDomain.getEndFacility(), organizationId));
        } else {
            throw new QuincusValidationException(String.format("End Facility on Network lane Connection sequence: %s is mandatory", segmentDomain.getSequence()));
        }
    }


    private void validateAndEnrichNetworkLane(NetworkLane networkLane, String organizationId) {
        enrichAndValidateAddressWithQportal(networkLane.getOrigin(), organizationId);
        enrichAndValidateAddressWithQportal(networkLane.getDestination(), organizationId);

        enrichAndValidateFacility(networkLane.getOriginFacility(), organizationId);
        enrichAndValidateFacility(networkLane.getDestinationFacility(), organizationId);

        networkLane.getNetworkLaneSegments().forEach(this::enrichNetworkLaneSegments);
    }

    private void enrichAndValidateAddressWithQportal(Address address, String organizationId) {
        if (address == null) {
            return;
        }
        Address addressGeneratedFromQPortal = addressAsyncService
                .generateCityAddressFromQPortal(address.getCityName(), address.getStateName(), address.getCountryName(), organizationId);
        validateMatchLocationTree(address, addressGeneratedFromQPortal);
        address.setCityId(addressGeneratedFromQPortal.getExternalId());
        address.setStateId(addressGeneratedFromQPortal.getStateId());
        address.setCountryId(addressGeneratedFromQPortal.getCountryId());
        address.setExternalId(addressGeneratedFromQPortal.getExternalId());
    }

    private void enrichAndValidateFacility(Facility facility, String organizationId) {
        if (facility == null || StringUtils.isBlank(facility.getName())) {
            return;
        }
        Facility facilityGeneratedFromQportal = facilityService.generateFacilityFromQPortal(facility.getName(), organizationId);
        if (facility.getLocation() != null) {
            validateMatchFacilityLocationTree(facility, facilityGeneratedFromQportal.getLocation());
        }

        facility.setExternalId(facilityGeneratedFromQportal.getExternalId());
        facility.setName(facilityGeneratedFromQportal.getName());
        facility.setCode(facilityGeneratedFromQportal.getCode());
        facility.setLocationCode(facilityGeneratedFromQportal.getLocationCode());
        facility.setLocation(facilityGeneratedFromQportal.getLocation());

    }

    private void setupNetworkLaneSegmentPartner(NetworkLaneSegment domain, NetworkLaneSegmentEntity entity, String orgnanizationId) {
        PartnerEntity partnerEntity = partnerAsyncService.findOrCreatePartnerByName(domain.getPartner().getName(), orgnanizationId);
        entity.setPartner(partnerEntity);
    }

    private void validateMatchLocationTree(Address addressDomain, Address qportalGeneratedAddress) {

        boolean locationTreeMatch = addressDomain.getStateName().equalsIgnoreCase(qportalGeneratedAddress.getStateName())
                && addressDomain.getCountryName().equalsIgnoreCase(qportalGeneratedAddress.getCountryName())
                && addressDomain.getCityName().equalsIgnoreCase(qportalGeneratedAddress.getCityName());

        if (!locationTreeMatch) {
            throw new QuincusValidationException(String.format(LOCATION_MISMATCH_ERROR, addressDomain.getCountryName()
                    , addressDomain.getStateName(), addressDomain.getCityName()));
        }
    }

    private void validateMatchFacilityLocationTree(Facility facility, Address qportalGeneratedAddress) {
        Address addressDomain = facility.getLocation();
        boolean locationTreeMatch = addressDomain.getStateName().equalsIgnoreCase(qportalGeneratedAddress.getStateName())
                && addressDomain.getCountryName().equalsIgnoreCase(qportalGeneratedAddress.getCountryName())
                && addressDomain.getCityName().equalsIgnoreCase(qportalGeneratedAddress.getCityName());

        if (!locationTreeMatch) {
            throw new QuincusValidationException(String.format(FACILITY_MISMATCH_ERROR, addressDomain.getCountryName()
                    , addressDomain.getStateName(), addressDomain.getCityName(), facility.getExternalId()));
        }
    }

    private void enrichNetworkLaneSegments(NetworkLaneSegment networkLaneSegment) {
        enrichAndValidateFacility(networkLaneSegment.getStartFacility(), networkLaneSegment.getOrganizationId());
        enrichAndValidateFacility(networkLaneSegment.getEndFacility(), networkLaneSegment.getOrganizationId());
    }

    public NetworkLaneFilterResult findAll(NetworkLaneFilter networkLaneFilter, String organizationId) {
        NetworkLaneCriteria criteria = networkLaneCriteriaMapper.mapFilterToCriteria(networkLaneFilter, organizationId);
        NetworkLaneSpecification networkLaneSpecification = criteria.buildSpecification();
        Pageable page = networkLaneSpecification.buildPageable();
        long resultCount = networkLaneRepository.count(networkLaneSpecification);
        List<NetworkLaneEntity> result = projectionListingPage.findAllWithPagination(networkLaneSpecification, page);
        List<NetworkLane> networkLanes = networkLaneMapper.mapEntitiesToDomain(result);
        networkLaneEnrichmentService.enrichNetworkLanesDurationCalculation(networkLanes);
        int currentPage = networkLaneFilter.getPageNumber() + 1;
        networkLaneFilter.setPageNumber(currentPage + 1);
        return createNetworkLaneFilterResult(networkLaneFilter, criteria, resultCount, page, networkLanes);
    }

    private NetworkLaneFilterResult createNetworkLaneFilterResult(NetworkLaneFilter networkLaneFilter, NetworkLaneCriteria networkLaneCriteria, long resultCount, Pageable page, List<NetworkLane> networkLanes) {
        return new NetworkLaneFilterResult(networkLanes)
                .filter(networkLaneFilter)
                .totalElements(resultCount)
                .totalPages(projectionListingPage.getTotalNumberOfPages(resultCount, page.getPageSize()))
                .currentPage(networkLaneCriteria.getPage());
    }

    @Transactional
    public NetworkLane update(NetworkLane networkLane) {
        final NetworkLaneEntity existingNetworkLane = networkLaneRepository.findById(networkLane.getId())
                .orElseThrow(() -> new NetworkLaneNotFoundException(String.format(ERR_NETWORK_LANE_NOT_FOUND, networkLane.getId())));
        networkLaneDataHelper.setupOriginUsingNetworkLaneSegment(networkLane, existingNetworkLane);
        networkLaneDataHelper.setupDestinationUsingNetworkLaneSegment(networkLane, existingNetworkLane);
        existingNetworkLane.resetAndAddAllNetworkLaneSegments(createNetworkLaneSegmentEntities(networkLane, existingNetworkLane));
        networkLaneDataHelper.setSegmentTypeOnNetworkLaneSegments(existingNetworkLane);
        networkLaneDataHelper.enrichLaneSegmentTimezoneFields(existingNetworkLane);
        return networkLaneMapper.mapEntityToDomain(networkLaneRepository.save(existingNetworkLane));
    }

    private List<NetworkLaneSegmentEntity> createNetworkLaneSegmentEntitiesWithOrganizationId(
            final NetworkLane networkLane,
            final NetworkLaneEntity networkLaneEntity,
            final String organizationId) {
        final List<NetworkLaneSegmentEntity> networkLaneSegments = new ArrayList<>();
        IntStream.range(0, networkLane.getNetworkLaneSegments().size()).forEach(index -> {
            final NetworkLaneSegment networkLaneSegment = networkLane.getNetworkLaneSegments().get(index);
            networkLaneSegment.setId(null);
            final NetworkLaneSegmentEntity segmentEntity = networkLaneSegmentMapper.mapDomainToEntity(networkLaneSegment);
            setUpStartAndEndFacility(networkLaneEntity, networkLaneSegment, segmentEntity, organizationId, index, networkLane.getNetworkLaneSegments().size());
            setupNetworkLaneSegmentPartner(networkLaneSegment, segmentEntity, organizationId);
            segmentEntity.setNetworkLane(networkLaneEntity);
            networkLaneSegments.add(segmentEntity);
        });
        return networkLaneSegments;
    }

    private List<NetworkLaneSegmentEntity> createNetworkLaneSegmentEntities(
            final NetworkLane networkLane,
            final NetworkLaneEntity networkLaneEntity) {
        return createNetworkLaneSegmentEntitiesWithOrganizationId(networkLane, networkLaneEntity, userDetailsProvider.getCurrentOrganizationId());
    }

    public NetworkLane findById(String id) {
        final NetworkLaneEntity networkLaneEntity = networkLaneRepository.findById(id)
                .orElseThrow(() -> new NetworkLaneNotFoundException(String.format(ERR_NETWORK_LANE_NOT_FOUND, id)));
        return networkLaneMapper.mapEntityToDomain(networkLaneEntity);
    }
}
