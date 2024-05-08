package com.quincus.shipment.impl.helper;

import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Partner;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.shipment.impl.repository.entity.PartnerEntity;
import com.quincus.shipment.impl.service.LocationHierarchyService;
import com.quincus.shipment.impl.service.PartnerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
@AllArgsConstructor
@Transactional(readOnly = true)
public class SegmentReferenceProvider {

    private final PartnerService partnerService;
    private final LocationHierarchyService locationHierarchyService;

    public SegmentReferenceHolder generateReference(List<PackageJourneySegment> packageJourneySegments) {
        List<String> partnerExternalIds = new ArrayList<>();
        List<String> facilityExternalIds = new ArrayList<>();
        packageJourneySegments.forEach(packageJourneySegment -> {
            addNonBlankPartnerToList(partnerExternalIds, packageJourneySegment.getPartner());
            addNonBlankFacilityToList(facilityExternalIds, packageJourneySegment.getStartFacility());
            addNonBlankFacilityToList(facilityExternalIds, packageJourneySegment.getEndFacility());
        });
        SegmentReferenceHolder segmentReferenceHolder = new SegmentReferenceHolder();
        segmentReferenceHolder.setPartnerBySegmentId(generatePartnerEntityReference(partnerExternalIds));
        segmentReferenceHolder.setLocationHierarchyByFacilityExtId(generateLocationHierarchy(facilityExternalIds));
        return segmentReferenceHolder;
    }

    private void addNonBlankPartnerToList(List<String> partnerExternalIds, Partner partner) {
        if (partner != null && StringUtils.isNotBlank(partner.getId())) {
            partnerExternalIds.add(partner.getId());
        }
    }

    private void addNonBlankFacilityToList(List<String> facilityExternalIds, Facility facility) {
        if (facility != null && StringUtils.isNotBlank(facility.getExternalId())) {
            facilityExternalIds.add(facility.getExternalId());
        }
    }

    private Map<String, PartnerEntity> generatePartnerEntityReference(List<String> partnerExternalIds) {
        if (CollectionUtils.isEmpty(partnerExternalIds)) {
            return Collections.emptyMap();
        }
        return partnerService.
                findAllByExternalIds(partnerExternalIds).stream()
                .collect(Collectors.toMap(PartnerEntity::getExternalId, Function.identity()));
    }

    private Map<String, LocationHierarchyEntity> generateLocationHierarchy(List<String> facilityExternalIds) {
        if (CollectionUtils.isEmpty(facilityExternalIds)) {
            return Collections.emptyMap();
        }
        return locationHierarchyService.
                findLocationHierarchyByFacilityExternalIds(facilityExternalIds).stream()
                .collect(Collectors.toMap(this::createUniqueLocationHierarchyUniqueKeyFromExtIds,
                        Function.identity()));
    }

    private String createUniqueLocationHierarchyUniqueKeyFromExtIds(LocationHierarchyEntity locationHierarchy) {
        return locationHierarchy.getCountry().getExternalId() +
                locationHierarchy.getState().getExternalId() +
                locationHierarchy.getCity().getExternalId() +
                locationHierarchy.getFacility().getExternalId();
    }
}