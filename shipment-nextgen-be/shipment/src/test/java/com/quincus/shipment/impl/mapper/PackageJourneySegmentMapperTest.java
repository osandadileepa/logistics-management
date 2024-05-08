package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.constant.AlertType;
import com.quincus.shipment.api.constant.BookingStatus;
import com.quincus.shipment.api.constant.InstructionApplyToType;
import com.quincus.shipment.api.constant.SegmentStatus;
import com.quincus.shipment.api.constant.SegmentType;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.constant.UnitOfMeasure;
import com.quincus.shipment.api.domain.Alert;
import com.quincus.shipment.api.domain.Driver;
import com.quincus.shipment.api.domain.Instruction;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Vehicle;
import com.quincus.shipment.impl.repository.entity.AlertEntity;
import com.quincus.shipment.impl.repository.entity.InstructionEntity;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity_;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity_;
import com.quincus.shipment.impl.test_utils.CustomTuple;
import com.quincus.shipment.impl.test_utils.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PackageJourneySegmentMapperTest {

    private final TestUtil testUtil = TestUtil.getInstance();
    private final MapperTestUtil mapperTestUtil = MapperTestUtil.getInstance();

    @Test
    void mapDomainToEntity_packageJourneySegmentDomain_shouldReturnPackageJourneySegmentEntity() {
        PackageJourneySegment domain = new PackageJourneySegment();
        domain.setJourneyId("JOURNEY1");
        domain.setType(SegmentType.LAST_MILE);
        domain.setOpsType("OPS-TYPE-1");
        domain.setVehicle(createVehicleTestData());
        domain.setDriver(createDriverTestData());
        domain.setStatus(SegmentStatus.PLANNED);
        domain.setTransportType(TransportType.GROUND);
        domain.setServicedBy("SVC - 01");
        domain.setOrganizationId("Org 1");
        domain.setHubId("HUB-1");
        domain.setInstructions(createInstructions());

        Alert dummyAlert = new Alert("This is a warning message", AlertType.WARNING);
        domain.setAlerts(List.of(dummyAlert));

        final PackageJourneySegmentEntity entity = PackageJourneySegmentMapper.mapDomainToEntity(domain);

        assertThat(entity.getAlerts().get(0).getShortMessage()).isEqualTo(dummyAlert.getShortMessage());
        assertThat(entity.getAlerts().get(0).getType()).isEqualTo(dummyAlert.getType());
        assertThat(entity.getServicedBy()).isEqualTo(domain.getServicedBy());
        assertThat(entity.getOrganizationId()).isEqualTo(domain.getOrganizationId());
        assertThat(entity.getId()).isEqualTo(domain.getSegmentId());
        assertThat(entity.getOpsType()).isEqualTo(domain.getOpsType());
        assertThat(entity.getStatus()).isEqualTo(domain.getStatus());
        assertThat(entity.getTransportType()).isEqualTo(domain.getTransportType());
        assertThat(entity.getServicedBy()).isEqualTo(domain.getServicedBy());
        assertThat(entity.getHubId()).isEqualTo(domain.getHubId());
        assertThat(entity.getInstructions()).isNotEmpty();
    }

    @Test
    void mapDomainToEntity_packageJourneySegmentDomainNull_shouldReturnNull() {
        assertThat(PackageJourneySegmentMapper.mapDomainToEntity(null)).isNull();
    }

    @Test
    void mapDomainToExistingEntity_packageJourneySegmentDomain_shouldReturnPackageJourneySegmentEntity() {
        PackageJourneySegment domain = new PackageJourneySegment();
        domain.setJourneyId("JOURNEY1");
        domain.setType(SegmentType.LAST_MILE);
        domain.setOpsType("OPS-TYPE-1");
        domain.setVehicle(createVehicleTestData());
        domain.setDriver(createDriverTestData());
        domain.setStatus(SegmentStatus.PLANNED);
        domain.setTransportType(TransportType.GROUND);
        domain.setServicedBy("SVC - 01");
        domain.setOrganizationId("Org 1");
        domain.setHubId("HUB-1");

        Alert dummyAlert = new Alert("This is a warning message", AlertType.WARNING);
        domain.setAlerts(List.of(dummyAlert));

        final PackageJourneySegmentEntity entity = new PackageJourneySegmentEntity();
        PackageJourneySegmentMapper.mapDomainToExistingEntity(entity, domain);

        assertThat(entity.getServicedBy()).isEqualTo(domain.getServicedBy());
        assertThat(entity.getOrganizationId()).isEqualTo(domain.getOrganizationId());
        assertThat(entity.getId()).isEqualTo(domain.getSegmentId());
        assertThat(entity.getOpsType()).isEqualTo(domain.getOpsType());
        assertThat(entity.getStatus()).isEqualTo(domain.getStatus());
        assertThat(entity.getTransportType()).isEqualTo(domain.getTransportType());
        assertThat(entity.getServicedBy()).isEqualTo(domain.getServicedBy());
        assertThat(entity.getHubId()).isEqualTo(domain.getHubId());
        assertThat(entity.getExternalBookingReference()).isEqualTo(domain.getExternalBookingReference());
        assertThat(entity.getInternalBookingReference()).isEqualTo(domain.getInternalBookingReference());
        assertThat(entity.getBookingStatus()).isEqualTo(domain.getBookingStatus());
        assertThat(entity.getRejectionReason()).isEqualTo(domain.getRejectionReason());
        assertThat(entity.getAssignmentStatus()).isEqualTo(domain.getAssignmentStatus());
    }

    @Test
    void mapEntityToDomain_packageJourneySegmentEntityWithOthers_shouldReturnPackageJourneySegmentDomain() {
        PackageJourneySegmentEntity entity = new PackageJourneySegmentEntity();
        entity.setId("segment-id-1");
        entity.setOpsType("ops-type-1");
        entity.setStatus(SegmentStatus.PLANNED);
        entity.setType(SegmentType.LAST_MILE);
        entity.setTransportType(TransportType.GROUND);
        entity.setServicedBy("svc-01");
        entity.setOrganizationId("Org 1");
        entity.setHubId("hub-1");
        entity.setDriver(createDriverTestData());
        entity.setVehicle(createVehicleTestData());
        entity.setDurationUnit(UnitOfMeasure.MINUTE);
        entity.setCalculatedMileageUnit(UnitOfMeasure.MILE);

        AlertEntity dummyAlertEntity = new AlertEntity();
        dummyAlertEntity.setShortMessage("This is an error message");
        dummyAlertEntity.setType(AlertType.ERROR);
        entity.setAlerts(List.of(dummyAlertEntity));

        LocationHierarchyEntity startLocationHierarchyEntity = new LocationHierarchyEntity();
        LocationEntity startFacility = new LocationEntity();
        startFacility.setName("start facility name");
        startFacility.setCode("facility_loc_code_1");
        startLocationHierarchyEntity.setFacility(startFacility);
        startLocationHierarchyEntity.setCountry(startFacility);
        startLocationHierarchyEntity.setState(startFacility);
        startLocationHierarchyEntity.setCity(startFacility);
        startLocationHierarchyEntity.setFacilityCode("facility_code_1");
        startLocationHierarchyEntity.setFacilityLocationCode(startFacility.getCode());
        entity.setStartLocationHierarchy(startLocationHierarchyEntity);

        LocationHierarchyEntity endLocationHierarchyEntity = new LocationHierarchyEntity();
        LocationEntity endFacility = new LocationEntity();
        endFacility.setName("end facility name");
        endFacility.setCode("facility_loc_code_2");
        endLocationHierarchyEntity.setFacility(endFacility);
        endLocationHierarchyEntity.setCountry(endFacility);
        endLocationHierarchyEntity.setState(endFacility);
        endLocationHierarchyEntity.setCity(endFacility);
        endLocationHierarchyEntity.setFacilityCode("facility_code_2");
        endLocationHierarchyEntity.setFacilityLocationCode(endFacility.getCode());
        entity.setEndLocationHierarchy(endLocationHierarchyEntity);

        entity.setInstructions(createInstructionsEntity());

        final PackageJourneySegment domain = PackageJourneySegmentMapper.mapEntityToDomain(true, entity, 0, 1);

        assertThat(domain.getAlerts().get(0).getShortMessage()).isEqualTo(dummyAlertEntity.getShortMessage());
        assertThat(domain.getAlerts().get(0).getType()).isEqualTo(dummyAlertEntity.getType());
        assertThat(domain.getServicedBy()).isEqualTo(entity.getServicedBy());
        assertThat(domain.getOrganizationId()).isEqualTo(entity.getOrganizationId());
        assertThat(domain.getSegmentId()).isEqualTo(entity.getId());
        assertThat(domain.getOpsType()).isEqualTo(entity.getOpsType());
        assertThat(domain.getStatus()).isEqualTo(entity.getStatus());
        assertThat(domain.getTransportType()).isEqualTo(entity.getTransportType());
        assertThat(domain.getServicedBy()).isEqualTo(entity.getServicedBy());
        assertThat(domain.getStartFacility().getName()).withFailMessage("Start Facility name mismatch.").isEqualTo(entity.getStartLocationHierarchy().getFacility().getName());
        assertThat(domain.getStartFacility().getCode()).withFailMessage("Start Facility code mismatch.").isEqualTo(entity.getStartLocationHierarchy().getFacilityCode());
        assertThat(domain.getEndFacility().getName()).withFailMessage("End Facility name mismatch.").isEqualTo(entity.getEndLocationHierarchy().getFacility().getName());
        assertThat(domain.getEndFacility().getCode()).withFailMessage("End Facility code mismatch.").isEqualTo(entity.getEndLocationHierarchy().getFacilityCode());
        assertThat(domain.getStartFacility().getLocationCode()).withFailMessage("Start Facility code mismatch.").isEqualTo(entity.getStartLocationHierarchy().getFacility().getCode());
        assertThat(domain.getEndFacility().getLocationCode()).withFailMessage("End Facility code mismatch.").isEqualTo(entity.getEndLocationHierarchy().getFacility().getCode());
        assertThat(domain.getHubId()).isEqualTo(entity.getHubId());
        assertThat(domain.getVehicle()).isEqualTo(entity.getVehicle());
        assertThat(domain.getDriver()).isEqualTo(entity.getDriver());
        assertThat(domain.getInstructions()).isNotEmpty();
        assertThat(domain.getDurationUnitLabel()).isEqualTo(entity.getDurationUnit().getLabel());
        assertThat(domain.getCalculatedMileageUnitLabel()).isEqualTo(entity.getCalculatedMileageUnit().getLabel());
    }

    @Test
    void mapEntityToDomain_packageJourneySegmentEntity_shouldReturnPackageJourneySegmentDomain() {
        PackageJourneySegmentEntity entity = new PackageJourneySegmentEntity();
        entity.setId("segment-id-1");
        entity.setOpsType("ops-type-1");
        entity.setStatus(SegmentStatus.PLANNED);
        entity.setType(SegmentType.LAST_MILE);
        entity.setTransportType(TransportType.GROUND);
        entity.setServicedBy("svc-01");
        entity.setOrganizationId("Org 1");
        entity.setHubId("hub-1");
        entity.setDriver(createDriverTestData());
        entity.setVehicle(createVehicleTestData());
        entity.setExternalBookingReference("externalBookingRef");
        entity.setInternalBookingReference("internalBookingRef");
        entity.setRejectionReason("Sample rejection reason");
        entity.setBookingStatus(BookingStatus.CONFIRMED);
        entity.setAssignmentStatus("Confirmed");

        AlertEntity dummyAlertEntity = new AlertEntity();
        dummyAlertEntity.setShortMessage("This is an error message");
        dummyAlertEntity.setType(AlertType.ERROR);
        entity.setAlerts(List.of(dummyAlertEntity));

        LocationHierarchyEntity startLocationHierarchyEntity = new LocationHierarchyEntity();
        LocationEntity startFacility = new LocationEntity();
        startFacility.setName("start facility name");
        startFacility.setCode("facility_loc_code_1");
        startLocationHierarchyEntity.setFacility(startFacility);
        startLocationHierarchyEntity.setCountry(startFacility);
        startLocationHierarchyEntity.setState(startFacility);
        startLocationHierarchyEntity.setCity(startFacility);
        startLocationHierarchyEntity.setFacilityCode("facility_code_1");
        startLocationHierarchyEntity.setFacilityLocationCode(startFacility.getCode());
        entity.setStartLocationHierarchy(startLocationHierarchyEntity);

        LocationHierarchyEntity endLocationHierarchyEntity = new LocationHierarchyEntity();
        LocationEntity endFacility = new LocationEntity();
        endFacility.setName("end facility name");
        endFacility.setCode("facility_loc_code_2");
        endLocationHierarchyEntity.setFacility(endFacility);
        endLocationHierarchyEntity.setCountry(endFacility);
        endLocationHierarchyEntity.setState(endFacility);
        endLocationHierarchyEntity.setCity(endFacility);
        endLocationHierarchyEntity.setFacilityCode("facility_code_2");
        endLocationHierarchyEntity.setFacilityLocationCode(endFacility.getCode());
        entity.setEndLocationHierarchy(endLocationHierarchyEntity);

        entity.setInstructions(createInstructionsEntity());

        final PackageJourneySegment domain = PackageJourneySegmentMapper.mapEntityToDomain(entity);

        assertThat(domain.getAlerts().get(0).getShortMessage()).isEqualTo(dummyAlertEntity.getShortMessage());
        assertThat(domain.getAlerts().get(0).getType()).isEqualTo(dummyAlertEntity.getType());
        assertThat(domain.getServicedBy()).isEqualTo(entity.getServicedBy());
        assertThat(domain.getOrganizationId()).isEqualTo(entity.getOrganizationId());
        assertThat(domain.getSegmentId()).isEqualTo(entity.getId());
        assertThat(domain.getOpsType()).isEqualTo(entity.getOpsType());
        assertThat(domain.getStatus()).isEqualTo(entity.getStatus());
        assertThat(domain.getTransportType()).isEqualTo(entity.getTransportType());
        assertThat(domain.getServicedBy()).isEqualTo(entity.getServicedBy());
        assertThat(domain.getStartFacility().getName()).withFailMessage("Start Facility name mismatch.").isEqualTo(entity.getStartLocationHierarchy().getFacility().getName());
        assertThat(domain.getStartFacility().getCode()).withFailMessage("Start Facility code mismatch.").isEqualTo(entity.getStartLocationHierarchy().getFacilityCode());
        assertThat(domain.getEndFacility().getName()).withFailMessage("End Facility name mismatch.").isEqualTo(entity.getEndLocationHierarchy().getFacility().getName());
        assertThat(domain.getEndFacility().getCode()).withFailMessage("End Facility code mismatch.").isEqualTo(entity.getEndLocationHierarchy().getFacilityCode());
        assertThat(domain.getStartFacility().getLocationCode()).withFailMessage("Start Facility code mismatch.").isEqualTo(entity.getStartLocationHierarchy().getFacility().getCode());
        assertThat(domain.getEndFacility().getLocationCode()).withFailMessage("End Facility code mismatch.").isEqualTo(entity.getEndLocationHierarchy().getFacility().getCode());
        assertThat(domain.getHubId()).isEqualTo(entity.getHubId());
        assertThat(domain.getVehicle()).isEqualTo(entity.getVehicle());
        assertThat(domain.getDriver()).isEqualTo(entity.getDriver());
        assertThat(domain.getInstructions()).isNotEmpty();

        assertThat(domain.getBookingStatus()).isEqualTo(entity.getBookingStatus());
        assertThat(domain.getExternalBookingReference()).isEqualTo(entity.getExternalBookingReference());
        assertThat(domain.getInternalBookingReference()).isEqualTo(entity.getInternalBookingReference());
        assertThat(domain.getRejectionReason()).isEqualTo(entity.getRejectionReason());
        assertThat(domain.getAssignmentStatus()).isEqualTo("Confirmed");
    }

    @Test
    void mapEntityToDomain_noAlerts_shouldReturnPackageJourneySegmentDomain() {
        PackageJourneySegmentEntity entity = new PackageJourneySegmentEntity();
        entity.setId("segment-id-1");
        entity.setOpsType("ops-type-1");
        entity.setStatus(SegmentStatus.PLANNED);
        entity.setType(SegmentType.LAST_MILE);
        entity.setTransportType(TransportType.GROUND);
        entity.setServicedBy("svc-01");
        entity.setOrganizationId("Org 1");
        entity.setHubId("hub-1");

        LocationHierarchyEntity startLocationHierarchyEntity = new LocationHierarchyEntity();
        LocationEntity startFacility = new LocationEntity();
        startFacility.setName("start facility name");
        startFacility.setCode("facility_loc_code_1");
        startLocationHierarchyEntity.setFacility(startFacility);
        startLocationHierarchyEntity.setCountry(startFacility);
        startLocationHierarchyEntity.setState(startFacility);
        startLocationHierarchyEntity.setCity(startFacility);
        startLocationHierarchyEntity.setFacilityCode("facility_code_1");
        startLocationHierarchyEntity.setFacilityLocationCode(startFacility.getCode());
        entity.setStartLocationHierarchy(startLocationHierarchyEntity);

        LocationHierarchyEntity endLocationHierarchyEntity = new LocationHierarchyEntity();
        LocationEntity endFacility = new LocationEntity();
        endFacility.setName("end facility name");
        endFacility.setCode("facility_loc_code_2");
        endLocationHierarchyEntity.setFacility(endFacility);
        endLocationHierarchyEntity.setCountry(endFacility);
        endLocationHierarchyEntity.setState(endFacility);
        endLocationHierarchyEntity.setCity(endFacility);
        endLocationHierarchyEntity.setFacilityCode("facility_code_2");
        endLocationHierarchyEntity.setFacilityLocationCode(endFacility.getCode());
        entity.setEndLocationHierarchy(endLocationHierarchyEntity);

        final PackageJourneySegment domain = PackageJourneySegmentMapper.mapEntityToDomain(true, entity, 0, 1);

        assertThat(domain.getAlerts()).isNotNull();
        assertThat(domain.getAlerts()).isEmpty();
        assertThat(domain.getServicedBy()).isEqualTo(entity.getServicedBy());
        assertThat(domain.getOrganizationId()).isEqualTo(entity.getOrganizationId());
        assertThat(domain.getSegmentId()).isEqualTo(entity.getId());
        assertThat(domain.getOpsType()).isEqualTo(entity.getOpsType());
        assertThat(domain.getStatus()).isEqualTo(entity.getStatus());
        assertThat(domain.getTransportType()).isEqualTo(entity.getTransportType());
        assertThat(domain.getServicedBy()).isEqualTo(entity.getServicedBy());
        assertThat(domain.getStartFacility().getName()).withFailMessage("Start Facility name mismatch.").isEqualTo(entity.getStartLocationHierarchy().getFacility().getName());
        assertThat(domain.getStartFacility().getCode()).withFailMessage("Start Facility code mismatch.").isEqualTo(entity.getStartLocationHierarchy().getFacilityCode());
        assertThat(domain.getEndFacility().getName()).withFailMessage("End Facility name mismatch.").isEqualTo(entity.getEndLocationHierarchy().getFacility().getName());
        assertThat(domain.getEndFacility().getCode()).withFailMessage("End Facility code mismatch.").isEqualTo(entity.getEndLocationHierarchy().getFacilityCode());
        assertThat(domain.getStartFacility().getLocationCode()).withFailMessage("Start Facility code mismatch.").isEqualTo(entity.getStartLocationHierarchy().getFacility().getCode());
        assertThat(domain.getEndFacility().getLocationCode()).withFailMessage("End Facility code mismatch.").isEqualTo(entity.getEndLocationHierarchy().getFacility().getCode());
        assertThat(domain.getHubId()).isEqualTo(entity.getHubId());
    }

    @Test
    void mapEntityToDomain_packageJourneySegmentEntityNull_shouldReturnNull() {
        assertThat(PackageJourneySegmentMapper.mapEntityToDomain(true, null, 0, 0)).isNull();
    }

    @Test
    void mapDomainListToEntityListPackageJourneySegment_packageJourneySegmentDomainList_shouldReturnPackageJourneySegmentList() {
        final List<PackageJourneySegment> domainList = testUtil.createSingleShipmentData().getShipmentJourney().getPackageJourneySegments();

        final List<PackageJourneySegmentEntity> entityList = PackageJourneySegmentMapper.mapDomainListToEntityListPackageJourneySegment(domainList);
        assertThat(entityList).hasSameSizeAs(domainList);
        mapperTestUtil.packageJourneySegmentDomainListToEntityList_commonAsserts(domainList, entityList);
    }

    @Test
    void mapDomainListToEntityListPackageJourneySegment_packageJourneySegmentDomainListNull_shouldReturnNull() {
        final List<PackageJourneySegmentEntity> entityList = PackageJourneySegmentMapper.mapDomainListToEntityListPackageJourneySegment(null);
        assertThat(entityList).isEmpty();
    }

    @Test
    void mapEntityListToDomainListPackageJourneySegment_packageJourneySegmentEntityList_shouldReturnPackageJourneySegmentList() {
        final List<PackageJourneySegmentEntity> entityList = List.of(mapperTestUtil.createSamplePackageJourneySegment());

        final List<PackageJourneySegment> domainList = PackageJourneySegmentMapper.mapEntityListToDomainListPackageJourneySegment(entityList);
        assertThat(entityList).hasSameSizeAs(domainList);
        mapperTestUtil.packageJourneySegmentEntityListToDomainList_commonAsserts(entityList, domainList);
    }

    @Test
    void mapEntityListToDomainListPackageJourneySegment_packageJourneySegmentEntityListNull_shouldReturnNull() {
        final List<PackageJourneySegment> domainList = PackageJourneySegmentMapper.mapEntityListToDomainListPackageJourneySegment(null);
        assertThat(domainList).isEmpty();
    }

    @Test
    void mapTupleToEntity() {
        CustomTuple tuple = new CustomTuple();
        tuple.put(BaseEntity_.ID, "id");
        tuple.put(PackageJourneySegmentEntity_.TYPE, SegmentType.LAST_MILE);
        tuple.put(PackageJourneySegmentEntity_.STATUS, SegmentStatus.PLANNED);
        tuple.put(PackageJourneySegmentEntity_.TRANSPORT_TYPE, TransportType.AIR);
        tuple.put(PackageJourneySegmentEntity_.SHIPMENT_JOURNEY_ID, "shipmentJourneyId");
        tuple.put(PackageJourneySegmentEntity_.REF_ID, "refId");
        tuple.put(PackageJourneySegmentEntity_.SEQUENCE, "sequence");

        PackageJourneySegmentEntity entity = PackageJourneySegmentMapper.mapTupleToEntity(tuple);

        assertThat(entity.getId()).isEqualTo("id");
        assertThat(entity.getType()).isEqualTo(SegmentType.LAST_MILE);
        assertThat(entity.getStatus()).isEqualTo(SegmentStatus.PLANNED);
        assertThat(entity.getTransportType()).isEqualTo(TransportType.AIR);
        assertThat(entity.getShipmentJourneyId()).isEqualTo("shipmentJourneyId");
        assertThat(entity.getRefId()).isEqualTo("refId");
        assertThat(entity.getSequence()).isEqualTo("sequence");
    }

    private Vehicle createVehicleTestData() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId("vehicleId");
        vehicle.setName("vehicleName");
        vehicle.setNumber("vehicleNumber");
        vehicle.setType("vehicleType");
        return vehicle;
    }

    private Driver createDriverTestData() {
        Driver driver = new Driver();
        driver.setId("driverId");
        driver.setName("driverName");
        driver.setPhoneCode("driverPhoneCode");
        driver.setPhoneNumber("driverPhoneNumber");
        return driver;
    }

    private List<Instruction> createInstructions() {
        List<Instruction> instructions = new ArrayList<>();

        Instruction pickUpInstruction = new Instruction();
        pickUpInstruction.setId("2678bdeb-73a5-4679-8d11-b169867cd901");
        pickUpInstruction.setLabel("pickup label");
        pickUpInstruction.setSource(Instruction.SOURCE_ORDER);
        pickUpInstruction.setApplyTo(InstructionApplyToType.PICKUP);
        pickUpInstruction.setCreatedAt("2023-04-19T09:10:43.614Z");
        pickUpInstruction.setUpdatedAt("2023-04-19T10:10:43.614Z");
        instructions.add(pickUpInstruction);

        Instruction deliveryInstruction = new Instruction();
        deliveryInstruction.setId("2678bdeb-73a5-4679-8d11-b169867cd111");
        deliveryInstruction.setLabel("delivery label");
        deliveryInstruction.setSource(Instruction.SOURCE_ORDER);
        deliveryInstruction.setApplyTo(InstructionApplyToType.DELIVERY);
        deliveryInstruction.setCreatedAt("2023-04-20T11:10:43.614Z");
        deliveryInstruction.setUpdatedAt("2023-04-21T01:10:43.614Z");
        instructions.add(deliveryInstruction);

        Instruction journeyInstruction = new Instruction();
        journeyInstruction.setId("2678bdeb-73a5-4679-8d11-b169867cd111");
        journeyInstruction.setLabel("journey label");
        journeyInstruction.setSource(Instruction.SOURCE_ORDER);
        journeyInstruction.setApplyTo(InstructionApplyToType.JOURNEY);
        journeyInstruction.setCreatedAt("2023-04-20T11:10:43.614Z");
        journeyInstruction.setUpdatedAt("2023-04-21T01:10:43.614Z");
        instructions.add(journeyInstruction);

        return instructions;
    }

    private List<InstructionEntity> createInstructionsEntity() {
        List<InstructionEntity> instructions = new ArrayList<>();

        InstructionEntity pickUpInstruction = new InstructionEntity();
        pickUpInstruction.setId("2678bdeb-73a5-4679-8d11-b169867cd901");
        pickUpInstruction.setLabel("pickup label");
        pickUpInstruction.setSource(Instruction.SOURCE_ORDER);
        pickUpInstruction.setApplyTo(InstructionApplyToType.PICKUP);
        pickUpInstruction.setCreatedAt("2023-04-19T09:10:43.614Z");
        pickUpInstruction.setUpdatedAt("2023-04-19T10:10:43.614Z");
        instructions.add(pickUpInstruction);

        InstructionEntity deliveryInstruction = new InstructionEntity();
        deliveryInstruction.setId("2678bdeb-73a5-4679-8d11-b169867cd111");
        deliveryInstruction.setLabel("delivery label");
        deliveryInstruction.setSource(Instruction.SOURCE_ORDER);
        deliveryInstruction.setApplyTo(InstructionApplyToType.DELIVERY);
        deliveryInstruction.setCreatedAt("2023-04-20T11:10:43.614Z");
        deliveryInstruction.setUpdatedAt("2023-04-21T01:10:43.614Z");
        instructions.add(deliveryInstruction);

        InstructionEntity journeyInstruction = new InstructionEntity();
        journeyInstruction.setId("2678bdeb-73a5-4679-8d11-b169867cd111");
        journeyInstruction.setLabel("journey label");
        journeyInstruction.setSource(Instruction.SOURCE_ORDER);
        journeyInstruction.setApplyTo(InstructionApplyToType.JOURNEY);
        journeyInstruction.setCreatedAt("2023-04-20T11:10:43.614Z");
        journeyInstruction.setUpdatedAt("2023-04-21T01:10:43.614Z");
        instructions.add(journeyInstruction);

        return instructions;
    }
}
