package com.quincus.shipment.impl.repository.projection;

import com.quincus.shipment.api.constant.EtaStatus;
import com.quincus.shipment.api.constant.MeasurementUnit;
import com.quincus.shipment.api.constant.SegmentType;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.constant.UnitOfMeasure;
import com.quincus.shipment.impl.repository.entity.AddressEntity;
import com.quincus.shipment.impl.repository.entity.AddressEntity_;
import com.quincus.shipment.impl.repository.entity.CustomerEntity;
import com.quincus.shipment.impl.repository.entity.CustomerEntity_;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import com.quincus.shipment.impl.repository.entity.LocationEntity_;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity_;
import com.quincus.shipment.impl.repository.entity.MilestoneEntity;
import com.quincus.shipment.impl.repository.entity.PackageDimensionEntity;
import com.quincus.shipment.impl.repository.entity.PackageDimensionEntity_;
import com.quincus.shipment.impl.repository.entity.PackageEntity;
import com.quincus.shipment.impl.repository.entity.PackageEntity_;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity_;
import com.quincus.shipment.impl.repository.entity.PartnerEntity;
import com.quincus.shipment.impl.repository.entity.PartnerEntity_;
import com.quincus.shipment.impl.repository.entity.ServiceTypeEntity;
import com.quincus.shipment.impl.repository.entity.ServiceTypeEntity_;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity_;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity_;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity_;
import com.quincus.shipment.impl.service.MilestoneService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * This class is specific to Shipment-related export data.
 */
@Service
public class ShipmentProjectionExport extends BaseProjection<ShipmentEntity> {
    private static final List<String> fields = List.of(BaseEntity_.ID, ShipmentEntity_.SHIPMENT_TRACKING_ID, ShipmentEntity_.ETA_STATUS, BaseEntity_.CREATE_TIME, ShipmentEntity_.INSTRUCTIONS);

    private final MilestoneService milestoneService;

    @Autowired
    public ShipmentProjectionExport(EntityManager entityManager, MilestoneService milestoneService) {
        super(entityManager, ShipmentEntity.class);
        this.milestoneService = milestoneService;
    }

    public List<ShipmentEntity> findAll(Specification<ShipmentEntity> specs) {
        CriteriaSetup criteriaSetup = initCommonParts(specs);
        List<Selection<?>> selections = getSelections(criteriaSetup.root, fields);

        addOriginInJoinAndSelections(criteriaSetup.root, selections);
        addDestinationInJoinAndSelections(criteriaSetup.root, selections);
        addShipmentAndPackageJourneyInJoinAndSelections(criteriaSetup.root, selections);
        addServiceTypeInJoinAndSelections(criteriaSetup.root, selections);
        addCustomerInJoinAndSelections(criteriaSetup.root, selections);

        criteriaSetup.query.multiselect(selections);
        applySorting(criteriaSetup.builder, criteriaSetup.query, criteriaSetup.root, null);

        List<Tuple> result = getPageableResultList(criteriaSetup.query, null);
        return createShipmentEntityListFromResult(result);
    }

    public List<Map<String, String>> findAllShipmentTrackingIds(Specification<ShipmentEntity> specs, Pageable pageable) {
        CriteriaSetup criteriaSetup = initCommonParts(specs);
        List<Selection<?>> selections = getSelections(criteriaSetup.root, List.of(ShipmentEntity_.SHIPMENT_TRACKING_ID, BaseEntity_.CREATE_TIME));
        addPackageInJoinAndSelections(criteriaSetup.root, selections);

        criteriaSetup.query.multiselect(selections);
        applySorting(criteriaSetup.builder, criteriaSetup.query, criteriaSetup.root, pageable);

        List<Tuple> result = getPageableResultList(criteriaSetup.query, null);
        return createShipmentTrackingIdListFromResult(result);
    }

    private CriteriaSetup initCommonParts(Specification<ShipmentEntity> specs) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = builder.createTupleQuery();
        Root<ShipmentEntity> root = applySpecToCriteria(query, builder, specs);
        return new CriteriaSetup(builder, query, root);
    }

    private void addOriginInJoinAndSelections(Root<ShipmentEntity> root, List<Selection<?>> selections) {
        Join<ShipmentEntity, AddressEntity> originEntityJoin = root.join(ShipmentEntity_.origin, JoinType.LEFT);
        Join<AddressEntity, LocationHierarchyEntity> originLocationHierarchyEntityJoin = originEntityJoin.join(AddressEntity_.locationHierarchy, JoinType.LEFT);
        Join<LocationHierarchyEntity, LocationEntity> originCountryLocationEntityJoin = originLocationHierarchyEntityJoin.join(LocationHierarchyEntity_.country, JoinType.LEFT);
        Join<LocationHierarchyEntity, LocationEntity> originStateLocationEntityJoin = originLocationHierarchyEntityJoin.join(LocationHierarchyEntity_.state, JoinType.LEFT);
        Join<LocationHierarchyEntity, LocationEntity> originCityLocationEntityJoin = originLocationHierarchyEntityJoin.join(LocationHierarchyEntity_.city, JoinType.LEFT);

        selections.add(originCountryLocationEntityJoin.get(LocationEntity_.name).alias(ShipmentEntity_.ORIGIN.concat(LocationHierarchyEntity_.COUNTRY)));
        selections.add(originStateLocationEntityJoin.get(LocationEntity_.name).alias(ShipmentEntity_.ORIGIN.concat(LocationHierarchyEntity_.STATE)));
        selections.add(originCityLocationEntityJoin.get(LocationEntity_.name).alias(ShipmentEntity_.ORIGIN.concat(LocationHierarchyEntity_.CITY)));
    }

    private void addDestinationInJoinAndSelections(Root<ShipmentEntity> root, List<Selection<?>> selections) {
        Join<ShipmentEntity, AddressEntity> destinationEntityJoin = root.join(ShipmentEntity_.destination, JoinType.LEFT);
        Join<AddressEntity, LocationHierarchyEntity> destinationLocationHierarchyEntityJoin = destinationEntityJoin.join(AddressEntity_.locationHierarchy, JoinType.LEFT);
        Join<LocationHierarchyEntity, LocationEntity> destinationCountryLocationEntityJoin = destinationLocationHierarchyEntityJoin.join(LocationHierarchyEntity_.country, JoinType.LEFT);
        Join<LocationHierarchyEntity, LocationEntity> destinationStateLocationEntityJoin = destinationLocationHierarchyEntityJoin.join(LocationHierarchyEntity_.state, JoinType.LEFT);
        Join<LocationHierarchyEntity, LocationEntity> destinationCityLocationEntityJoin = destinationLocationHierarchyEntityJoin.join(LocationHierarchyEntity_.city, JoinType.LEFT);

        selections.add(destinationCountryLocationEntityJoin.get(LocationEntity_.name).alias(ShipmentEntity_.DESTINATION.concat(LocationHierarchyEntity_.COUNTRY)));
        selections.add(destinationStateLocationEntityJoin.get(LocationEntity_.name).alias(ShipmentEntity_.DESTINATION.concat(LocationHierarchyEntity_.STATE)));
        selections.add(destinationCityLocationEntityJoin.get(LocationEntity_.name).alias(ShipmentEntity_.DESTINATION.concat(LocationHierarchyEntity_.CITY)));
    }

    private void addShipmentAndPackageJourneyInJoinAndSelections(Root<ShipmentEntity> root, List<Selection<?>> selections) {
        Join<ShipmentEntity, ShipmentJourneyEntity> shipmentJourneyEntityJoin = root.join(ShipmentEntity_.shipmentJourney, JoinType.LEFT);
        Join<ShipmentJourneyEntity, PackageJourneySegmentEntity> packageJourneySegmentEntityJoin = shipmentJourneyEntityJoin.join(ShipmentJourneyEntity_.packageJourneySegments, JoinType.LEFT);
        Join<PackageJourneySegmentEntity, LocationHierarchyEntity> packageJourneyStartFacilityEntityJoin = packageJourneySegmentEntityJoin.join(PackageJourneySegmentEntity_.startLocationHierarchy, JoinType.LEFT);
        Join<PackageJourneySegmentEntity, LocationHierarchyEntity> packageJourneyEndFacilityEntityJoin = packageJourneySegmentEntityJoin.join(PackageJourneySegmentEntity_.endLocationHierarchy, JoinType.LEFT);
        Join<PackageJourneySegmentEntity, PartnerEntity> partnerEntityJoin = packageJourneySegmentEntityJoin.join(PackageJourneySegmentEntity_.partner, JoinType.LEFT);

        selections.add(packageJourneySegmentEntityJoin.get(PackageJourneySegmentEntity_.refId).alias(PackageJourneySegmentEntity_.REF_ID));
        selections.add(packageJourneySegmentEntityJoin.get(PackageJourneySegmentEntity_.type).alias(PackageJourneySegmentEntity_.TYPE));
        selections.add(packageJourneySegmentEntityJoin.get(PackageJourneySegmentEntity_.transportType).alias(PackageJourneySegmentEntity_.TRANSPORT_TYPE));
        selections.add(packageJourneySegmentEntityJoin.get(PackageJourneySegmentEntity_.vehicleInfo).alias(PackageJourneySegmentEntity_.VEHICLE_INFO));
        selections.add(packageJourneySegmentEntityJoin.get(PackageJourneySegmentEntity_.flightNumber).alias(PackageJourneySegmentEntity_.FLIGHT_NUMBER));
        selections.add(packageJourneySegmentEntityJoin.get(PackageJourneySegmentEntity_.airline).alias(PackageJourneySegmentEntity_.AIRLINE));
        selections.add(packageJourneySegmentEntityJoin.get(PackageJourneySegmentEntity_.airlineCode).alias(PackageJourneySegmentEntity_.AIRLINE_CODE));
        selections.add(packageJourneySegmentEntityJoin.get(PackageJourneySegmentEntity_.masterWaybill).alias(PackageJourneySegmentEntity_.MASTER_WAYBILL));
        selections.add(packageJourneyStartFacilityEntityJoin.get(BaseEntity_.id).alias(PackageJourneySegmentEntity_.START_LOCATION_HIERARCHY));
        selections.add(packageJourneyStartFacilityEntityJoin.get(LocationHierarchyEntity_.facilityName).alias(PackageJourneySegmentEntity_.START_LOCATION_HIERARCHY + "_" + LocationHierarchyEntity_.FACILITY_NAME));
        selections.add(packageJourneyEndFacilityEntityJoin.get(BaseEntity_.id).alias(PackageJourneySegmentEntity_.END_LOCATION_HIERARCHY));
        selections.add(packageJourneyEndFacilityEntityJoin.get(LocationHierarchyEntity_.facilityName).alias(PackageJourneySegmentEntity_.END_LOCATION_HIERARCHY + "_" + LocationHierarchyEntity_.FACILITY_NAME));
        selections.add(packageJourneySegmentEntityJoin.get(PackageJourneySegmentEntity_.instruction).alias(PackageJourneySegmentEntity_.INSTRUCTION));
        selections.add(packageJourneySegmentEntityJoin.get(PackageJourneySegmentEntity_.duration).alias(PackageJourneySegmentEntity_.DURATION));
        selections.add(packageJourneySegmentEntityJoin.get(PackageJourneySegmentEntity_.durationUnit).alias(PackageJourneySegmentEntity_.DURATION_UNIT));
        selections.add(packageJourneySegmentEntityJoin.get(PackageJourneySegmentEntity_.pickUpTime).alias(PackageJourneySegmentEntity_.PICK_UP_TIME));
        selections.add(packageJourneySegmentEntityJoin.get(PackageJourneySegmentEntity_.dropOffTime).alias(PackageJourneySegmentEntity_.DROP_OFF_TIME));
        selections.add(packageJourneySegmentEntityJoin.get(PackageJourneySegmentEntity_.lockOutTime).alias(PackageJourneySegmentEntity_.LOCK_OUT_TIME));
        selections.add(packageJourneySegmentEntityJoin.get(PackageJourneySegmentEntity_.departureTime).alias(PackageJourneySegmentEntity_.DEPARTURE_TIME));
        selections.add(packageJourneySegmentEntityJoin.get(PackageJourneySegmentEntity_.arrivalTime).alias(PackageJourneySegmentEntity_.ARRIVAL_TIME));
        selections.add(packageJourneySegmentEntityJoin.get(PackageJourneySegmentEntity_.recoveryTime).alias(PackageJourneySegmentEntity_.RECOVERY_TIME));
        selections.add(packageJourneySegmentEntityJoin.get(PackageJourneySegmentEntity_.calculatedMileage).alias(PackageJourneySegmentEntity_.CALCULATED_MILEAGE));
        selections.add(packageJourneySegmentEntityJoin.get(PackageJourneySegmentEntity_.calculatedMileageUnit).alias(PackageJourneySegmentEntity_.CALCULATED_MILEAGE_UNIT));
        selections.add(partnerEntityJoin.get(PartnerEntity_.name).alias(PackageJourneySegmentEntity_.PARTNER));
    }

    private void addServiceTypeInJoinAndSelections(Root<ShipmentEntity> root, List<Selection<?>> selections) {
        Join<ShipmentEntity, ServiceTypeEntity> serviceTypeEntityJoin = root.join(ShipmentEntity_.serviceType, JoinType.LEFT);
        selections.add(serviceTypeEntityJoin.get(ServiceTypeEntity_.name).alias(ShipmentEntity_.SERVICE_TYPE));
    }

    private void addCustomerInJoinAndSelections(Root<ShipmentEntity> root, List<Selection<?>> selections) {
        Join<ShipmentEntity, CustomerEntity> customerEntityJoin = root.join(ShipmentEntity_.customer, JoinType.LEFT);
        selections.add(customerEntityJoin.get(CustomerEntity_.name).alias(ShipmentEntity_.CUSTOMER));
    }

    private void addPackageInJoinAndSelections(Root<ShipmentEntity> root, List<Selection<?>> selections) {
        Join<ShipmentEntity, PackageEntity> packageEntityJoin = root.join(ShipmentEntity_.shipmentPackage, JoinType.LEFT);
        Join<PackageEntity, PackageDimensionEntity> packageDimensionEntityJoin = packageEntityJoin.join(PackageEntity_.dimension, JoinType.LEFT);

        selections.add(packageDimensionEntityJoin.get(PackageDimensionEntity_.measurementUnit).alias(PackageDimensionEntity_.MEASUREMENT_UNIT));
        selections.add(packageDimensionEntityJoin.get(PackageDimensionEntity_.volumeWeight).alias(PackageDimensionEntity_.VOLUME_WEIGHT));
        selections.add(packageDimensionEntityJoin.get(PackageDimensionEntity_.grossWeight).alias(PackageDimensionEntity_.GROSS_WEIGHT));
    }

    private List<ShipmentEntity> createShipmentEntityListFromResult(List<Tuple> tuples) {
        if (CollectionUtils.isEmpty(tuples)) {
            return Collections.emptyList();
        }

        List<MilestoneEntity> milestones = getMilestones(tuples);
        List<ShipmentEntity> shipmentEntities = new ArrayList<>();

        tuples.forEach(e -> {
            ShipmentEntity entity = convertTupleToShipmentEntity(e);
            MilestoneEntity milestone = getMilestonesByShipmentId(milestones, entity.getId());
            if (nonNull(milestone)) {
                entity.setMilestoneEvents(Set.of(milestone));
            }
            shipmentEntities.add(entity);
        });

        return shipmentEntities;
    }

    private List<Map<String, String>> createShipmentTrackingIdListFromResult(List<Tuple> tuples) {
        return tuples.stream()
                .map(this::getShipmentDetails)
                .toList();
    }

    private Map<String, String> getShipmentDetails(Tuple tuple) {
        Map<String, String> map = new HashMap<>();
        map.put(ShipmentEntity_.SHIPMENT_TRACKING_ID, tuple.get(ShipmentEntity_.SHIPMENT_TRACKING_ID, String.class));
        map.put(PackageDimensionEntity_.MEASUREMENT_UNIT, tuple.get(PackageDimensionEntity_.MEASUREMENT_UNIT, MeasurementUnit.class).getLabel());
        map.put(PackageDimensionEntity_.VOLUME_WEIGHT, tuple.get(PackageDimensionEntity_.VOLUME_WEIGHT, BigDecimal.class).toString());
        map.put(PackageDimensionEntity_.GROSS_WEIGHT, tuple.get(PackageDimensionEntity_.GROSS_WEIGHT, BigDecimal.class).toString());
        return map;
    }

    private List<MilestoneEntity> getMilestones(List<Tuple> tuples) {
        List<String> shipmentIds = getAllShipmentIds(tuples);
        return milestoneService.findRecentMilestoneByShipmentIds(shipmentIds);
    }

    private List<String> getAllShipmentIds(List<Tuple> tuples) {
        if (CollectionUtils.isEmpty(tuples)) return Collections.emptyList();
        List<String> ids = new ArrayList<>();
        tuples.forEach(e -> {
            if (nonNull(e)) {
                ids.add(e.get(BaseEntity_.ID, String.class));
            }
        });
        return ids;
    }

    private ShipmentEntity convertTupleToShipmentEntity(Tuple tuple) {
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId(tuple.get(BaseEntity_.ID, String.class));
        shipmentEntity.setShipmentTrackingId(tuple.get(ShipmentEntity_.SHIPMENT_TRACKING_ID, String.class));
        shipmentEntity.setEtaStatus(tuple.get(ShipmentEntity_.ETA_STATUS, EtaStatus.class));
        shipmentEntity.setCreateTime(tuple.get(BaseEntity_.CREATE_TIME, Instant.class));
        shipmentEntity.setInstructions(tuple.get(ShipmentEntity_.INSTRUCTIONS, List.class));
        createAndSetCustomer(tuple.get(ShipmentEntity_.CUSTOMER, String.class), shipmentEntity);
        createAndSetServiceType(tuple.get(ShipmentEntity_.SERVICE_TYPE, String.class), shipmentEntity);
        createAndSetOrigin(tuple, shipmentEntity);
        createAndSetDestination(tuple, shipmentEntity);
        createAndSetPackageJourneySegment(tuple, shipmentEntity);

        return shipmentEntity;
    }

    private void createAndSetCustomer(String name, ShipmentEntity shipmentEntity) {
        if (isNull(name)) return;
        CustomerEntity customer = new CustomerEntity();
        customer.setName(name);
        shipmentEntity.setCustomer(customer);
    }

    private void createAndSetServiceType(String name, ShipmentEntity shipmentEntity) {
        if (isNull(name)) return;
        ServiceTypeEntity serviceType = new ServiceTypeEntity();
        serviceType.setName(name);
        shipmentEntity.setServiceType(serviceType);
    }

    private void createAndSetOrigin(Tuple tuple, ShipmentEntity shipmentEntity) {
        String originCountry = tuple.get(ShipmentEntity_.ORIGIN.concat(LocationHierarchyEntity_.COUNTRY), String.class);
        String originState = tuple.get(ShipmentEntity_.ORIGIN.concat(LocationHierarchyEntity_.STATE), String.class);
        String originCity = tuple.get(ShipmentEntity_.ORIGIN.concat(LocationHierarchyEntity_.CITY), String.class);

        LocationHierarchyEntity locationHierarchyEntity = new LocationHierarchyEntity();
        locationHierarchyEntity.setCountry(createLocation(originCountry));
        locationHierarchyEntity.setState(createLocation(originState));
        locationHierarchyEntity.setCity(createLocation(originCity));

        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setLocationHierarchy(locationHierarchyEntity);

        shipmentEntity.setOrigin(addressEntity);
    }

    private void createAndSetDestination(Tuple tuple, ShipmentEntity shipmentEntity) {
        String destinationCountry = tuple.get(ShipmentEntity_.DESTINATION.concat(LocationHierarchyEntity_.COUNTRY), String.class);
        String destinationState = tuple.get(ShipmentEntity_.DESTINATION.concat(LocationHierarchyEntity_.STATE), String.class);
        String destinationCity = tuple.get(ShipmentEntity_.DESTINATION.concat(LocationHierarchyEntity_.CITY), String.class);

        LocationHierarchyEntity locationHierarchyEntity = new LocationHierarchyEntity();
        locationHierarchyEntity.setCountry(createLocation(destinationCountry));
        locationHierarchyEntity.setState(createLocation(destinationState));
        locationHierarchyEntity.setCity(createLocation(destinationCity));

        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setLocationHierarchy(locationHierarchyEntity);

        shipmentEntity.setDestination(addressEntity);
    }

    private LocationEntity createLocation(String name) {
        LocationEntity location = new LocationEntity();
        location.setName(name);
        return location;
    }

    private void createAndSetPackageJourneySegment(Tuple tuple, ShipmentEntity shipmentEntity) {
        PackageJourneySegmentEntity packageJourneySegmentEntity = new PackageJourneySegmentEntity();

        packageJourneySegmentEntity.setRefId(tuple.get(PackageJourneySegmentEntity_.REF_ID, String.class));
        packageJourneySegmentEntity.setType(tuple.get(PackageJourneySegmentEntity_.TYPE, SegmentType.class));
        packageJourneySegmentEntity.setVehicleInfo(tuple.get(PackageJourneySegmentEntity_.VEHICLE_INFO, String.class));
        packageJourneySegmentEntity.setFlightNumber(tuple.get(PackageJourneySegmentEntity_.FLIGHT_NUMBER, String.class));
        packageJourneySegmentEntity.setAirline(tuple.get(PackageJourneySegmentEntity_.AIRLINE, String.class));
        packageJourneySegmentEntity.setAirlineCode(tuple.get(PackageJourneySegmentEntity_.AIRLINE_CODE, String.class));
        packageJourneySegmentEntity.setMasterWaybill(tuple.get(PackageJourneySegmentEntity_.MASTER_WAYBILL, String.class));
        packageJourneySegmentEntity.setTransportType(tuple.get(PackageJourneySegmentEntity_.TRANSPORT_TYPE, TransportType.class));
        packageJourneySegmentEntity.setDuration(tuple.get(PackageJourneySegmentEntity_.DURATION, BigDecimal.class));
        packageJourneySegmentEntity.setDurationUnit(tuple.get(PackageJourneySegmentEntity_.DURATION_UNIT, UnitOfMeasure.class));
        packageJourneySegmentEntity.setPickUpTime(tuple.get(PackageJourneySegmentEntity_.PICK_UP_TIME, String.class));
        packageJourneySegmentEntity.setDropOffTime(tuple.get(PackageJourneySegmentEntity_.DROP_OFF_TIME, String.class));
        packageJourneySegmentEntity.setLockOutTime(tuple.get(PackageJourneySegmentEntity_.LOCK_OUT_TIME, String.class));
        packageJourneySegmentEntity.setDepartureTime(tuple.get(PackageJourneySegmentEntity_.DEPARTURE_TIME, String.class));
        packageJourneySegmentEntity.setArrivalTime(tuple.get(PackageJourneySegmentEntity_.ARRIVAL_TIME, String.class));
        packageJourneySegmentEntity.setRecoveryTime(tuple.get(PackageJourneySegmentEntity_.RECOVERY_TIME, String.class));
        packageJourneySegmentEntity.setCalculatedMileage(tuple.get(PackageJourneySegmentEntity_.CALCULATED_MILEAGE, BigDecimal.class));
        packageJourneySegmentEntity.setCalculatedMileageUnit(tuple.get(PackageJourneySegmentEntity_.CALCULATED_MILEAGE_UNIT, UnitOfMeasure.class));
        packageJourneySegmentEntity.setInstruction(tuple.get(PackageJourneySegmentEntity_.INSTRUCTION, String.class));

        String startFacilityId = tuple.get(PackageJourneySegmentEntity_.START_LOCATION_HIERARCHY, String.class);
        String startFacilityName = tuple.get(PackageJourneySegmentEntity_.START_LOCATION_HIERARCHY + "_" + LocationHierarchyEntity_.FACILITY_NAME, String.class);
        LocationHierarchyEntity startLocationHierarchyEntity = new LocationHierarchyEntity();
        startLocationHierarchyEntity.setId(startFacilityId);

        LocationEntity startLocationEntity = new LocationEntity();
        startLocationEntity.setId(startFacilityId);
        startLocationEntity.setName(startFacilityName);
        startLocationHierarchyEntity.setFacility(startLocationEntity);
        startLocationHierarchyEntity.setFacilityName(startFacilityName);
        startLocationHierarchyEntity.setCountry(createLocation(StringUtils.EMPTY));
        startLocationHierarchyEntity.setState(createLocation(StringUtils.EMPTY));
        startLocationHierarchyEntity.setCity(createLocation(StringUtils.EMPTY));
        packageJourneySegmentEntity.setStartLocationHierarchy(startLocationHierarchyEntity);

        String endFacilityId = tuple.get(PackageJourneySegmentEntity_.END_LOCATION_HIERARCHY, String.class);
        String endFacilityName = tuple.get(PackageJourneySegmentEntity_.END_LOCATION_HIERARCHY + "_" + LocationHierarchyEntity_.FACILITY_NAME, String.class);
        LocationHierarchyEntity endLocationHierarchyEntity = new LocationHierarchyEntity();
        endLocationHierarchyEntity.setId(endFacilityId);

        LocationEntity endLocationEntity = new LocationEntity();
        endLocationEntity.setId(endFacilityId);
        endLocationEntity.setName(endFacilityName);
        endLocationHierarchyEntity.setFacility(endLocationEntity);
        endLocationHierarchyEntity.setFacilityName(endFacilityName);
        endLocationHierarchyEntity.setCountry(createLocation(StringUtils.EMPTY));
        endLocationHierarchyEntity.setState(createLocation(StringUtils.EMPTY));
        endLocationHierarchyEntity.setCity(createLocation(StringUtils.EMPTY));
        packageJourneySegmentEntity.setEndLocationHierarchy(endLocationHierarchyEntity);

        PartnerEntity partnerEntity = new PartnerEntity();
        partnerEntity.setName(tuple.get(PackageJourneySegmentEntity_.PARTNER, String.class));
        packageJourneySegmentEntity.setPartner(partnerEntity);

        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        shipmentJourneyEntity.addPackageJourneySegment(packageJourneySegmentEntity);

        shipmentEntity.setShipmentJourney(shipmentJourneyEntity);
    }

    private MilestoneEntity getMilestonesByShipmentId(List<MilestoneEntity> milestoneEntities, String shipmentId) {
        if (CollectionUtils.isEmpty(milestoneEntities)) return null;
        return milestoneEntities.stream().filter(m -> StringUtils.equals(m.getShipmentId(), shipmentId)).findAny().orElse(null);
    }

    private record CriteriaSetup(CriteriaBuilder builder, CriteriaQuery<Tuple> query, Root<ShipmentEntity> root) {
    }
}