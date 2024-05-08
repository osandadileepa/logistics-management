package com.quincus.shipment.impl.helper.journey.generator;

import com.quincus.ext.DateTimeUtil;
import com.quincus.order.api.domain.Location;
import com.quincus.order.api.domain.Root;
import com.quincus.order.api.domain.SegmentsPayload;
import com.quincus.shipment.api.constant.JourneyStatus;
import com.quincus.shipment.api.constant.SegmentStatus;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.constant.UnitOfMeasure;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Partner;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.helper.EnumUtil;
import com.quincus.shipment.impl.config.ShipmentJourneyCreationProperties;
import com.quincus.shipment.impl.helper.ShipmentUtil;
import com.quincus.shipment.impl.helper.journey.PackageJourneySegmentTypeAssigner;
import com.quincus.shipment.impl.mapper.OMLocationMapper;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Order(1)
@Component
@AllArgsConstructor
public class OrderMessageShipmentJourneyGenerator implements ShipmentJourneyGenerator {

    private final PackageJourneySegmentTypeAssigner packageJourneySegmentTypeAssigner;
    private final ShipmentJourneyCreationProperties shipmentJourneyCreationProperties;
    private final UserDetailsProvider userDetailsProvider;

    @Override
    public ShipmentJourney generateShipmentJourney(Root root) {
        if (!Boolean.parseBoolean(root.getIsSegment()) || CollectionUtils.isEmpty(root.getSegmentsPayloads())
                || (isCurrentOrganizationShouldSkipCreationFromPayload() && !root.isSegmentsUpdated())) {
            return null;
        }
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        shipmentJourney.setStatus(JourneyStatus.PLANNED);
        shipmentJourney.setPackageJourneySegments(generatePackageJourneySegmentFromOM(root));
        return shipmentJourney;
    }

    private boolean isCurrentOrganizationShouldSkipCreationFromPayload() {
        if (CollectionUtils.isEmpty(shipmentJourneyCreationProperties.getOrganizationIdsToSkipSegmentCreationFromPayload())) {
            return false;
        }
        return shipmentJourneyCreationProperties.getOrganizationIdsToSkipSegmentCreationFromPayload()
                .contains(userDetailsProvider.getCurrentOrganization().getId());
    }

    private List<PackageJourneySegment> generatePackageJourneySegmentFromOM(Root root) {
        List<PackageJourneySegment> packageJourneySegments = new ArrayList<>();
        List<SegmentsPayload> segments = root.getSegmentsPayloads();
        int size = segments.size();
        IntStream.range(0, size).forEach(i -> packageJourneySegments.add(createSegment(segments.get(i), root, i, size)));
        packageJourneySegmentTypeAssigner.assignSegmentTypes(packageJourneySegments);
        return packageJourneySegments;
    }

    private PackageJourneySegment createSegment(SegmentsPayload payload, Root root, int index, int size) {
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setOrganizationId(root.getOrganisationId());
        segment.setStatus(SegmentStatus.PLANNED);
        segment.setCost(payload.getCost());
        segment.setRefId(payload.getRefId());
        segment.setSequence(String.valueOf(index));
        segment.setAirline(payload.getAirline());
        segment.setAirlineCode(payload.getAirlineCode());
        segment.setCurrencyId(payload.getCurrencyId());
        segment.setInstruction(payload.getInstruction());
        segment.setVehicleInfo(payload.getVehicleInfo());
        segment.setArrivalTime(DateTimeUtil.toIsoDateTimeFormat(payload.getArrivalTime()));
        segment.setPickUpTime(DateTimeUtil.toIsoDateTimeFormat(payload.getPickUpTime()));
        segment.setPickUpCommitTime(DateTimeUtil.toIsoDateTimeFormat(payload.getPickUpCommitTime()));
        segment.setDropOffTime(DateTimeUtil.toIsoDateTimeFormat(payload.getDropOffTime()));
        segment.setDropOffCommitTime(DateTimeUtil.toIsoDateTimeFormat(payload.getDropOffCommitTime()));
        segment.setFlightNumber(payload.getFlightNumber());
        segment.setLockOutTime(DateTimeUtil.toIsoDateTimeFormat(payload.getLockOutTime()));
        segment.setRecoveryTime(DateTimeUtil.toIsoDateTimeFormat(payload.getRecoveryTime()));
        segment.setDepartureTime(DateTimeUtil.toIsoDateTimeFormat(payload.getDepartureTime()));
        segment.setMasterWaybill(payload.getMasterWaybill());
        segment.setTransportType(EnumUtil.toEnum(TransportType.class, payload.getTransportCategory()));

        UnitOfMeasure calculatedMileageUom = ShipmentUtil.convertCalculatedMileageUomStringToUom(root.getDistanceUom());
        segment.setCalculatedMileage(payload.getCalculatedMileage());
        segment.setCalculatedMileageUnit(calculatedMileageUom);
        segment.setCalculatedMileageUnitLabel(calculatedMileageUom == null ? null : calculatedMileageUom.getLabel());
        segment.setDuration(payload.getDuration());
        segment.setDurationUnit(UnitOfMeasure.MINUTE);
        segment.setDurationUnitLabel(UnitOfMeasure.MINUTE.getLabel());

        String pickUpFacilityId = payload.getPickUpFacilityId();
        String dropOffFacilityId = payload.getDropOffFacilityId();
        Location origin = root.getOrigin();
        String startFacilityId;
        Location startFacility = null;
        if (index == 0 && StringUtils.isBlank(pickUpFacilityId)) {
            startFacility = origin;
            startFacilityId = Optional.of(origin).map(Location::getId).orElse(null);
        } else {
            startFacilityId = pickUpFacilityId;

        }
        Location destination = root.getDestination();
        String endFacilityId;
        Location endFacility = null;
        if (index == size - 1 && StringUtils.isBlank(dropOffFacilityId)) {
            endFacility = destination;
            endFacilityId = Optional.of(destination).map(Location::getId).orElse(null);
        } else {
            endFacilityId = dropOffFacilityId;
        }
        segment.setStartFacility(OMLocationMapper.mapLocationToFacility(startFacilityId, startFacility, Shipment.ORIGIN_PROPERTY_NAME));
        segment.setEndFacility(OMLocationMapper.mapLocationToFacility(endFacilityId, endFacility, Shipment.DESTINATION_PROPERTY_NAME));
        segment.setPartner(generatePartnerFromPayload(payload.getPartnerId(), root.getOrganisationId()));
        segment.setOpsType(root.getOpsType());
        segment.setHubId(payload.getHandleFacilityId());
        checkAndSetDefaultValuesForSingleSegmentWithoutFacilityIds(segment, payload, root, size);
        return segment;
    }

    private Partner generatePartnerFromPayload(String partnerId, String organizationId) {
        if (StringUtils.isBlank(partnerId)) {
            return null;
        }
        Partner partner = new Partner();
        partner.setId(partnerId);
        partner.setOrganizationId(organizationId);
        return partner;
    }

    private void checkAndSetDefaultValuesForSingleSegmentWithoutFacilityIds(final PackageJourneySegment segment, final SegmentsPayload payload, final Root root, final int size) {
        if (StringUtils.isEmpty(payload.getDropOffFacilityId()) &&
                StringUtils.isEmpty(payload.getPickUpFacilityId()) &&
                size == 1) {

            segment.setStartFacility(OMLocationMapper.mapLocationToFacility(null, root.getOrigin(), Shipment.ORIGIN_PROPERTY_NAME));
            segment.setEndFacility(OMLocationMapper.mapLocationToFacility(null, root.getDestination(), Shipment.DESTINATION_PROPERTY_NAME));
        }
    }
}
