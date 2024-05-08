package com.quincus.shipment.impl.mapper;

import com.quincus.ext.DateTimeUtil;
import com.quincus.shipment.api.constant.SegmentStatus;
import com.quincus.shipment.api.constant.SegmentType;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.constant.UnitOfMeasure;
import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Alert;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.Flight;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.impl.repository.entity.FlightEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity_;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity_;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.factory.Mappers;
import org.springframework.util.CollectionUtils;

import javax.persistence.Tuple;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.quincus.shipment.api.domain.Shipment.DESTINATION_PROPERTY_NAME;
import static com.quincus.shipment.api.domain.Shipment.ORIGIN_PROPERTY_NAME;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@NoArgsConstructor(access = AccessLevel.NONE)
public class PackageJourneySegmentMapper {

    private static final AlertMapper ALERT_MAPPER = Mappers.getMapper(AlertMapper.class);
    private static final FlightMapper FLIGHT_MAPPER = Mappers.getMapper(FlightMapper.class);
    private static final InstructionMapper INSTRUCTION_MAPPER = Mappers.getMapper(InstructionMapper.class);

    public static PackageJourneySegmentEntity mapDomainToEntity(PackageJourneySegment packageJourneySegmentDomain) {
        if (packageJourneySegmentDomain == null) {
            return null;
        }
        PackageJourneySegmentEntity packageJourneySegmentEntity = new PackageJourneySegmentEntity();

        mapDomainToExistingEntity(packageJourneySegmentEntity, packageJourneySegmentDomain);

        if (packageJourneySegmentDomain.getAlerts() != null) {
            packageJourneySegmentEntity.setAlerts(
                    packageJourneySegmentDomain.getAlerts().stream()
                            .map(ALERT_MAPPER::toEntity)
                            .collect(Collectors.toCollection(ArrayList::new))
            );
        }

        if (!CollectionUtils.isEmpty(packageJourneySegmentDomain.getInstructions())) {
            packageJourneySegmentEntity.setInstructions(
                    packageJourneySegmentDomain.getInstructions().stream()
                            .map(INSTRUCTION_MAPPER::toEntity)
                            .collect(Collectors.toCollection(ArrayList::new))
            );
        }
        return packageJourneySegmentEntity;
    }

    public static void mapDomainToExistingEntity(@NotNull PackageJourneySegmentEntity packageJourneySegmentEntity,
                                                 @NotNull PackageJourneySegment packageJourneySegmentDomain) {
        packageJourneySegmentEntity.setOrganizationId(packageJourneySegmentDomain.getOrganizationId());
        packageJourneySegmentEntity.setType(packageJourneySegmentDomain.getType());
        packageJourneySegmentEntity.setOpsType(packageJourneySegmentDomain.getOpsType());
        packageJourneySegmentEntity.setStatus(packageJourneySegmentDomain.getStatus());
        packageJourneySegmentEntity.setTransportType(packageJourneySegmentDomain.getTransportType());
        packageJourneySegmentEntity.setServicedBy(packageJourneySegmentDomain.getServicedBy());
        packageJourneySegmentEntity.setCost(packageJourneySegmentDomain.getCost());
        packageJourneySegmentEntity.setRefId(packageJourneySegmentDomain.getRefId());
        packageJourneySegmentEntity.setSequence(packageJourneySegmentDomain.getSequence());
        packageJourneySegmentEntity.setAirlineCode(packageJourneySegmentDomain.getAirlineCode());
        packageJourneySegmentEntity.setAirline(packageJourneySegmentDomain.getAirline());
        packageJourneySegmentEntity.setCurrencyId(packageJourneySegmentDomain.getCurrencyId());
        packageJourneySegmentEntity.setInstruction(packageJourneySegmentDomain.getInstruction());
        packageJourneySegmentEntity.setArrivalTime(DateTimeUtil.toIsoDateTimeFormat(packageJourneySegmentDomain.getArrivalTime()));
        packageJourneySegmentEntity.setPickUpTime(DateTimeUtil.toIsoDateTimeFormat(packageJourneySegmentDomain.getPickUpTime()));
        packageJourneySegmentEntity.setPickUpCommitTime(DateTimeUtil.toIsoDateTimeFormat(packageJourneySegmentDomain.getPickUpCommitTime()));
        packageJourneySegmentEntity.setPickUpActualTime(DateTimeUtil.toIsoDateTimeFormat(packageJourneySegmentDomain.getPickUpActualTime()));
        packageJourneySegmentEntity.setPickUpOnSiteTime(DateTimeUtil.toIsoDateTimeFormat(packageJourneySegmentDomain.getPickUpOnSiteTime()));
        packageJourneySegmentEntity.setPickUpOnSiteTimezone(packageJourneySegmentDomain.getPickUpOnSiteTimezone());
        packageJourneySegmentEntity.setDropOffTime(DateTimeUtil.toIsoDateTimeFormat(packageJourneySegmentDomain.getDropOffTime()));
        packageJourneySegmentEntity.setDropOffCommitTime(DateTimeUtil.toIsoDateTimeFormat(packageJourneySegmentDomain.getDropOffCommitTime()));
        packageJourneySegmentEntity.setDropOffActualTime(DateTimeUtil.toIsoDateTimeFormat(packageJourneySegmentDomain.getDropOffActualTime()));
        packageJourneySegmentEntity.setDropOffOnSiteTime(DateTimeUtil.toIsoDateTimeFormat(packageJourneySegmentDomain.getDropOffOnSiteTime()));
        packageJourneySegmentEntity.setDropOffOnSiteTimezone(packageJourneySegmentDomain.getDropOffOnSiteTimezone());
        packageJourneySegmentEntity.setFlightNumber(packageJourneySegmentDomain.getFlightNumber());
        packageJourneySegmentEntity.setLockOutTime(DateTimeUtil.toIsoDateTimeFormat(packageJourneySegmentDomain.getLockOutTime()));
        packageJourneySegmentEntity.setRecoveryTime(DateTimeUtil.toIsoDateTimeFormat(packageJourneySegmentDomain.getRecoveryTime()));
        packageJourneySegmentEntity.setDepartureTime(DateTimeUtil.toIsoDateTimeFormat(packageJourneySegmentDomain.getDepartureTime()));
        packageJourneySegmentEntity.setMasterWaybill(packageJourneySegmentDomain.getMasterWaybill());
        packageJourneySegmentEntity.setVehicleInfo(packageJourneySegmentDomain.getVehicleInfo());
        packageJourneySegmentEntity.setCalculatedMileage(packageJourneySegmentDomain.getCalculatedMileage());
        packageJourneySegmentEntity.setCalculatedMileageUnit(packageJourneySegmentDomain.getCalculatedMileageUnit());

        packageJourneySegmentEntity.setDuration(packageJourneySegmentDomain.getDuration());
        packageJourneySegmentEntity.setDurationUnit(packageJourneySegmentDomain.getDurationUnit());
        packageJourneySegmentEntity.setFlightSubscriptionStatus(packageJourneySegmentDomain.getFlightSubscriptionStatus());
        packageJourneySegmentEntity.setFlightOrigin(packageJourneySegmentDomain.getFlightOrigin());
        packageJourneySegmentEntity.setFlightDestination(packageJourneySegmentDomain.getFlightDestination());
        packageJourneySegmentEntity.setHubId(packageJourneySegmentDomain.getHubId());
        packageJourneySegmentEntity.setVehicle(packageJourneySegmentDomain.getVehicle());
        packageJourneySegmentEntity.setDriver(packageJourneySegmentDomain.getDriver());
        packageJourneySegmentEntity.setExternalBookingReference(packageJourneySegmentDomain.getExternalBookingReference());
        packageJourneySegmentEntity.setInternalBookingReference(packageJourneySegmentDomain.getInternalBookingReference());
        packageJourneySegmentEntity.setBookingStatus(packageJourneySegmentDomain.getBookingStatus());
        packageJourneySegmentEntity.setRejectionReason(packageJourneySegmentDomain.getRejectionReason());
        packageJourneySegmentEntity.setAssignmentStatus(packageJourneySegmentDomain.getAssignmentStatus());

        FlightEntity flightEntity = FLIGHT_MAPPER.mapDomainToEntity(packageJourneySegmentDomain.getFlight());
        packageJourneySegmentEntity.setFlight(flightEntity);
    }

    public static PackageJourneySegment mapEntityToDomain(boolean isIncluded, PackageJourneySegmentEntity packageJourneySegmentEntity,
                                                          int index, int size) {

        PackageJourneySegment domain = mapEntityToDomain(isIncluded, packageJourneySegmentEntity);
        if (domain != null) {
            domain.setStartFacility(createFacility(packageJourneySegmentEntity.getStartLocationHierarchy(), getNameBySegmentOrder(ORIGIN_PROPERTY_NAME, index, size)));
            domain.setEndFacility(createFacility(packageJourneySegmentEntity.getEndLocationHierarchy(), getNameBySegmentOrder(DESTINATION_PROPERTY_NAME, index, size)));
        }

        return domain;
    }

    public static PackageJourneySegment mapEntityToDomain(PackageJourneySegmentEntity packageJourneySegmentEntity) {

        PackageJourneySegment domain = mapEntityToDomain(true, packageJourneySegmentEntity);
        if (domain != null) {
            domain.setStartFacility(createFacility(packageJourneySegmentEntity.getStartLocationHierarchy(), getNameBySegmentType(domain.getType())));
            domain.setEndFacility(createFacility(packageJourneySegmentEntity.getEndLocationHierarchy(), getNameBySegmentType(domain.getType())));
        }

        return domain;
    }

    private static PackageJourneySegment mapEntityToDomain(boolean isIncluded, PackageJourneySegmentEntity packageJourneySegmentEntity) {

        if (packageJourneySegmentEntity == null) {
            return null;
        }
        PackageJourneySegment packageJourneySegmentDomain = new PackageJourneySegment();
        packageJourneySegmentDomain.setJourneyId(packageJourneySegmentEntity.getShipmentJourneyId());
        packageJourneySegmentDomain.setOrganizationId(packageJourneySegmentEntity.getOrganizationId());
        packageJourneySegmentDomain.setStatus(packageJourneySegmentEntity.getStatus());
        packageJourneySegmentDomain.setSegmentId(packageJourneySegmentEntity.getId());
        packageJourneySegmentDomain.setType(packageJourneySegmentEntity.getType());
        packageJourneySegmentDomain.setOpsType(packageJourneySegmentEntity.getOpsType());
        packageJourneySegmentDomain.setServicedBy(packageJourneySegmentEntity.getServicedBy());
        packageJourneySegmentDomain.setCost(packageJourneySegmentEntity.getCost());
        packageJourneySegmentDomain.setRefId(packageJourneySegmentEntity.getRefId());
        packageJourneySegmentDomain.setSequence(packageJourneySegmentEntity.getSequence());
        packageJourneySegmentDomain.setAirline(packageJourneySegmentEntity.getAirline());
        packageJourneySegmentDomain.setAirlineCode(packageJourneySegmentEntity.getAirlineCode());
        packageJourneySegmentDomain.setCurrencyId(packageJourneySegmentEntity.getCurrencyId());
        packageJourneySegmentDomain.setFlightNumber(packageJourneySegmentEntity.getFlightNumber());
        packageJourneySegmentDomain.setInstruction(packageJourneySegmentEntity.getInstruction());
        packageJourneySegmentDomain.setArrivalTime(packageJourneySegmentEntity.getArrivalTime());
        packageJourneySegmentDomain.setArrivalTimezone(packageJourneySegmentEntity.getArrivalTimezone());
        packageJourneySegmentDomain.setPickUpTime(packageJourneySegmentEntity.getPickUpTime());
        packageJourneySegmentDomain.setPickUpTimezone(packageJourneySegmentEntity.getPickUpTimezone());
        packageJourneySegmentDomain.setPickUpCommitTime(packageJourneySegmentEntity.getPickUpCommitTime());
        packageJourneySegmentDomain.setPickUpCommitTimezone(packageJourneySegmentEntity.getPickUpCommitTimezone());
        packageJourneySegmentDomain.setPickUpActualTime(packageJourneySegmentEntity.getPickUpActualTime());
        packageJourneySegmentDomain.setPickUpActualTimezone(packageJourneySegmentEntity.getPickUpActualTimezone());
        packageJourneySegmentDomain.setDropOffTime(packageJourneySegmentEntity.getDropOffTime());
        packageJourneySegmentDomain.setDropOffTimezone(packageJourneySegmentEntity.getDropOffTimezone());
        packageJourneySegmentDomain.setDropOffCommitTime(packageJourneySegmentEntity.getDropOffCommitTime());
        packageJourneySegmentDomain.setDropOffCommitTimezone(packageJourneySegmentEntity.getDropOffCommitTimezone());
        packageJourneySegmentDomain.setDropOffActualTime(packageJourneySegmentEntity.getDropOffActualTime());
        packageJourneySegmentDomain.setDropOffActualTimezone(packageJourneySegmentEntity.getDropOffActualTimezone());
        packageJourneySegmentDomain.setLockOutTime(packageJourneySegmentEntity.getLockOutTime());
        packageJourneySegmentDomain.setLockOutTimezone(packageJourneySegmentEntity.getLockOutTimezone());
        packageJourneySegmentDomain.setRecoveryTime(packageJourneySegmentEntity.getRecoveryTime());
        packageJourneySegmentDomain.setRecoveryTimezone(packageJourneySegmentEntity.getRecoveryTimezone());
        packageJourneySegmentDomain.setDepartureTime(packageJourneySegmentEntity.getDepartureTime());
        packageJourneySegmentDomain.setDepartureTimezone(packageJourneySegmentEntity.getDepartureTimezone());
        packageJourneySegmentDomain.setMasterWaybill(packageJourneySegmentEntity.getMasterWaybill());
        packageJourneySegmentDomain.setVehicleInfo(packageJourneySegmentEntity.getVehicleInfo());

        UnitOfMeasure calculatedMileageUnit = packageJourneySegmentEntity.getCalculatedMileageUnit();
        UnitOfMeasure durationUnit = packageJourneySegmentEntity.getDurationUnit();
        packageJourneySegmentDomain.setCalculatedMileage(packageJourneySegmentEntity.getCalculatedMileage());
        packageJourneySegmentDomain.setCalculatedMileageUnit(calculatedMileageUnit);
        packageJourneySegmentDomain.setCalculatedMileageUnitLabel(calculatedMileageUnit == null ? null : calculatedMileageUnit.getLabel());
        packageJourneySegmentDomain.setDuration(packageJourneySegmentEntity.getDuration());
        packageJourneySegmentDomain.setDurationUnit(durationUnit);
        packageJourneySegmentDomain.setDurationUnitLabel(durationUnit == null ? null : durationUnit.getLabel());

        packageJourneySegmentDomain.setVehicle(packageJourneySegmentEntity.getVehicle());
        packageJourneySegmentDomain.setDriver(packageJourneySegmentEntity.getDriver());
        packageJourneySegmentDomain.setHubId(packageJourneySegmentEntity.getHubId());
        packageJourneySegmentDomain.setDropOffOnSiteTime(packageJourneySegmentEntity.getDropOffOnSiteTime());
        packageJourneySegmentDomain.setDropOffOnSiteTimezone(packageJourneySegmentEntity.getDropOffOnSiteTimezone());
        packageJourneySegmentDomain.setPickUpOnSiteTime(packageJourneySegmentEntity.getPickUpOnSiteTime());
        packageJourneySegmentDomain.setPickUpOnSiteTimezone(packageJourneySegmentEntity.getPickUpOnSiteTimezone());
        packageJourneySegmentDomain.setDropOffActualTime(packageJourneySegmentEntity.getDropOffActualTime());
        packageJourneySegmentDomain.setDropOffActualTimezone(packageJourneySegmentEntity.getDropOffActualTimezone());
        packageJourneySegmentDomain.setPickUpActualTime(packageJourneySegmentEntity.getPickUpActualTime());
        packageJourneySegmentDomain.setPickUpActualTimezone(packageJourneySegmentEntity.getPickUpActualTimezone());
        packageJourneySegmentDomain.setDeleted(packageJourneySegmentEntity.isDeleted());
        packageJourneySegmentDomain.setModifyTime(packageJourneySegmentEntity.getModifyTime());
        packageJourneySegmentDomain.setExternalBookingReference(packageJourneySegmentEntity.getExternalBookingReference());
        packageJourneySegmentDomain.setInternalBookingReference(packageJourneySegmentEntity.getInternalBookingReference());
        packageJourneySegmentDomain.setBookingStatus(packageJourneySegmentEntity.getBookingStatus());
        packageJourneySegmentDomain.setRejectionReason(packageJourneySegmentEntity.getRejectionReason());
        packageJourneySegmentDomain.setAssignmentStatus(packageJourneySegmentEntity.getAssignmentStatus());

        mapFlightDetails(packageJourneySegmentEntity, packageJourneySegmentDomain);
        mapPartner(isIncluded, packageJourneySegmentEntity, packageJourneySegmentDomain);
        mapAlerts(packageJourneySegmentEntity, packageJourneySegmentDomain);
        mapTransportType(packageJourneySegmentEntity, packageJourneySegmentDomain);
        mapIntructions(packageJourneySegmentEntity, packageJourneySegmentDomain);

        return packageJourneySegmentDomain;
    }

    public static List<PackageJourneySegment> mapEntityListToDomainList(List<PackageJourneySegmentEntity> entityList) {
        if (CollectionUtils.isEmpty(entityList)) return Collections.emptyList();
        int size = entityList.size();
        return IntStream.range(0, size).mapToObj(i -> mapEntityToDomain(true, entityList.get(i), i, size)).toList();
    }

    private static void mapFlightDetails(PackageJourneySegmentEntity packageJourneySegmentEntity, PackageJourneySegment packageJourneySegmentDomain) {
        packageJourneySegmentDomain.setFlightOrigin(packageJourneySegmentEntity.getFlightOrigin());
        packageJourneySegmentDomain.setFlightDestination(packageJourneySegmentEntity.getFlightDestination());
        packageJourneySegmentDomain.setFlightSubscriptionStatus(packageJourneySegmentEntity.getFlightSubscriptionStatus());
        Flight flight = FLIGHT_MAPPER.mapEntityToDomain(packageJourneySegmentEntity.getFlight());
        packageJourneySegmentDomain.setFlight(flight);
    }

    private static void mapIntructions(PackageJourneySegmentEntity packageJourneySegmentEntity, PackageJourneySegment packageJourneySegmentDomain) {
        if (!CollectionUtils.isEmpty(packageJourneySegmentEntity.getInstructions())) {
            packageJourneySegmentDomain.setInstructions(
                    packageJourneySegmentEntity.getInstructions().stream()
                            .map(INSTRUCTION_MAPPER::toDomain)
                            .collect(Collectors.toCollection(ArrayList::new))
            );
        }
    }

    private static void mapTransportType(PackageJourneySegmentEntity packageJourneySegmentEntity, PackageJourneySegment packageJourneySegmentDomain) {
        if (packageJourneySegmentEntity.getTransportType() != null) {
            packageJourneySegmentDomain.setTransportType(packageJourneySegmentEntity.getTransportType());
        }
    }

    private static void mapAlerts(PackageJourneySegmentEntity packageJourneySegmentEntity, PackageJourneySegment packageJourneySegmentDomain) {
        List<Alert> alerts = new ArrayList<>();
        if (!CollectionUtils.isEmpty(packageJourneySegmentEntity.getAlerts())) {
            alerts = packageJourneySegmentEntity.getAlerts().stream()
                    .map(ALERT_MAPPER::toDomain)
                    .sorted()
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        packageJourneySegmentDomain.setAlerts(alerts);
    }

    private static void mapPartner(boolean isIncluded, PackageJourneySegmentEntity packageJourneySegmentEntity, PackageJourneySegment packageJourneySegmentDomain) {
        if (isIncluded) {
            packageJourneySegmentDomain.setPartner(PartnerMapper.INSTANCE.mapEntityToDomain(packageJourneySegmentEntity.getPartner()));
        }
    }

    public static Facility createFacility(LocationHierarchyEntity lh, String facilityName) {
        if (isNull(lh)) return null;
        Facility facility = new Facility();

        if (nonNull(lh.getFacility())) {
            facility.setId(lh.getFacility().getId());
            String lhFacilityExternalId = lh.getFacility().getExternalId();
            String lhFacilityName = lh.getFacility().getName();
            facility.setExternalId(lh.getFacility().getExternalId());
            if (StringUtils.isNotEmpty(lhFacilityName) && (!lhFacilityName.equals(lhFacilityExternalId))) {
                facility.setName(lhFacilityName);
            } else {
                facility.setName(facilityName);
            }
            facility.setCode(lh.getFacilityCode());
            facility.setLocationCode(lh.getFacilityLocationCode());
            facility.setTimezone(lh.getFacility().getTimezone());
            if (StringUtils.isBlank(facility.getTimezone())) {
                facility.setTimezone(lh.getCity().getTimezone());
            }
        } else {
            facility.setId(lh.getId());
            facility.setExternalId(lh.getExternalId());
            facility.setName(facilityName);
            facility.setCode(facilityName);
            facility.setLocationCode(facilityName);
            facility.setTimezone(lh.getCity().getTimezone());
        }

        createFacilityAddressFromLocationHierarchy(lh, facility);

        return facility;
    }

    private static void createFacilityAddressFromLocationHierarchy(LocationHierarchyEntity lh, Facility facility) {
        Address location = new Address();
        location.setId(lh.getExternalId());
        location.setLocationHierarchyId(lh.getId());
        location.setCountry(lh.getCountry().getId());
        location.setState(lh.getState().getId());
        location.setCity(lh.getCity().getId());
        location.setCountryId(lh.getCountry().getExternalId());
        location.setStateId(lh.getState().getExternalId());
        location.setCityId(lh.getCity().getExternalId());
        location.setCountryName(lh.getCountryCode());
        location.setStateName(lh.getStateCode());
        location.setCityName(lh.getCityCode());
        facility.setLocation(location);
    }

    public static List<PackageJourneySegmentEntity> mapDomainListToEntityListPackageJourneySegment(List<PackageJourneySegment> packageJourneySegmentDomainList) {
        if (CollectionUtils.isEmpty(packageJourneySegmentDomainList)) {
            return Collections.emptyList();
        }

        List<PackageJourneySegmentEntity> packageJourneySegmentEntityList = new ArrayList<>(packageJourneySegmentDomainList.size());
        for (PackageJourneySegment packageJourneySegment : packageJourneySegmentDomainList) {
            packageJourneySegmentEntityList.add(mapDomainToEntity(packageJourneySegment));
        }

        return packageJourneySegmentEntityList;
    }

    public static List<PackageJourneySegment> mapSegmentListing(List<PackageJourneySegmentEntity> packageJourneySegmentEntityList) {
        if (CollectionUtils.isEmpty(packageJourneySegmentEntityList)) {
            return Collections.emptyList();
        }

        int size = packageJourneySegmentEntityList.size();
        List<PackageJourneySegment> packageJourneySegmentDomainList = new ArrayList<>(size);

        IntStream.range(0, size).forEach(i ->
                packageJourneySegmentDomainList.add(mapEntityToDomain(false, packageJourneySegmentEntityList.get(i), i, size)));

        return packageJourneySegmentDomainList;
    }

    public static List<PackageJourneySegment> mapEntityListToDomainListPackageJourneySegment(List<PackageJourneySegmentEntity> packageJourneySegmentEntityList) {
        if (CollectionUtils.isEmpty(packageJourneySegmentEntityList)) {
            return Collections.emptyList();
        }

        int size = packageJourneySegmentEntityList.size();
        List<PackageJourneySegment> packageJourneySegmentDomainList = new ArrayList<>(size);

        IntStream.range(0, size).forEach(i ->
                packageJourneySegmentDomainList.add(mapEntityToDomain(true, packageJourneySegmentEntityList.get(i), i, size)));

        return packageJourneySegmentDomainList;
    }

    private static String getNameBySegmentOrder(String tag, int index, int size) {
        if (index == 0 && ORIGIN_PROPERTY_NAME.equals(tag)) return ORIGIN_PROPERTY_NAME;
        if (index == 0 && size == 1 && DESTINATION_PROPERTY_NAME.equals(tag)) return DESTINATION_PROPERTY_NAME;
        if (index == size - 1 && DESTINATION_PROPERTY_NAME.equals(tag)) return DESTINATION_PROPERTY_NAME;
        return null;
    }

    public static String getNameBySegmentType(SegmentType type) {
        if (type == null) {
            return null;
        }

        return switch (type) {
            case FIRST_MILE -> ORIGIN_PROPERTY_NAME;
            case LAST_MILE -> DESTINATION_PROPERTY_NAME;
            default -> null;
        };
    }

    public static PackageJourneySegmentEntity mapTupleToEntity(Tuple packageJourneySegmentTuple) {
        PackageJourneySegmentEntity entity = new PackageJourneySegmentEntity();
        entity.setId(packageJourneySegmentTuple.get(BaseEntity_.ID, String.class));
        entity.setShipmentJourneyId(packageJourneySegmentTuple.get(PackageJourneySegmentEntity_.SHIPMENT_JOURNEY_ID, String.class));
        entity.setRefId(packageJourneySegmentTuple.get(PackageJourneySegmentEntity_.REF_ID, String.class));
        entity.setSequence(packageJourneySegmentTuple.get(PackageJourneySegmentEntity_.SEQUENCE, String.class));
        entity.setType(packageJourneySegmentTuple.get(PackageJourneySegmentEntity_.TYPE, SegmentType.class));
        entity.setStatus(packageJourneySegmentTuple.get(PackageJourneySegmentEntity_.STATUS, SegmentStatus.class));
        entity.setTransportType(packageJourneySegmentTuple.get(PackageJourneySegmentEntity_.TRANSPORT_TYPE, TransportType.class));
        entity.setAirline(packageJourneySegmentTuple.get(PackageJourneySegmentEntity_.AIRLINE, String.class));
        entity.setFlightNumber(packageJourneySegmentTuple.get(PackageJourneySegmentEntity_.FLIGHT_NUMBER, String.class));
        return entity;
    }

}
