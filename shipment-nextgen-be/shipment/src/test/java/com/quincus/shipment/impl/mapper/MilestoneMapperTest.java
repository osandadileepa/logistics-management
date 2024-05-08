package com.quincus.shipment.impl.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.constant.MilestoneSource;
import com.quincus.shipment.api.domain.Coordinate;
import com.quincus.shipment.api.domain.HostedFile;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.MilestoneAdditionalInfo;
import com.quincus.shipment.impl.repository.entity.MilestoneEntity;
import com.quincus.shipment.impl.repository.entity.OrganizationEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.test_utils.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.quincus.shipment.api.constant.MilestoneCode.DSP_PICKUP_FAILED;
import static com.quincus.shipment.api.constant.MilestoneCode.OM_BOOKED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MilestoneMapperTest {

    private final TestUtil testUtil = TestUtil.getInstance();
    @InjectMocks
    private MilestoneMapperImpl mapper;
    @Spy
    private ObjectMapper objectMapper = testUtil.getObjectMapper();

    @Mock
    private EntityManager entityManager;

    @Test
    @DisplayName("given milestoneEvent when mapDomainToEntity then return expected milestoneEventEntity")
    void returnExpectedWhenMapDomainToEntity() {
        Milestone domain = new Milestone();
        domain.setId("0001");
        domain.setExternalOrderId("E-0001");
        domain.setMilestoneCode(OM_BOOKED);
        domain.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()));
        domain.setMilestoneName("Order Booked");
        String orgId = "org1";
        domain.setOrganizationId(orgId);
        String shipmentId = "shp1";
        domain.setShipmentId(shipmentId);
        String segmentId = "segment-1";
        domain.setSegmentId(segmentId);
        domain.setData(testUtil.getDataFromFile("samplepayload/dispatchmodule-milestone-event.json"));
        domain.setServiceType("pickup service");
        domain.setJobType("pickup job");
        domain.setFromLocationId("a1234");
        domain.setFromCountryId("b1234");
        domain.setFromStateId("c1234");
        domain.setFromCityId("d1234");
        domain.setFromWardId("e1234");
        domain.setFromDistrictId("f1234");
        Coordinate fromCoordinate = new Coordinate(new BigDecimal("1.1"), new BigDecimal("1.2"));
        domain.setFromCoordinates(fromCoordinate);
        domain.setToLocationId("a5678");
        domain.setToCountryId("b5678");
        domain.setToStateId("c5678");
        domain.setToCityId("d5678");
        domain.setToWardId("e5678");
        domain.setToDistrictId("f5678");
        Coordinate toCoordinate = new Coordinate(new BigDecimal("2.1"), new BigDecimal("2.2"));
        domain.setToCoordinates(toCoordinate);
        domain.setUserId("user1234");
        domain.setPartnerId("partner1234");
        domain.setHubId("hub1");
        domain.setDriverId("driver1");
        domain.setDriverName("Ben Parker");
        domain.setDriverPhoneCode("+1");
        domain.setDriverPhoneNumber("202-555-0000");
        domain.setDriverEmail("driver@example.com");
        domain.setVehicleId("car1");
        domain.setVehicleType("Station Wagon");
        domain.setVehicleName("1965 Ford Country Squire");
        domain.setVehicleNumber("500");
        domain.setSenderName("Peter Parker");
        domain.setSenderCompany("Stark Tower");
        domain.setSenderDepartment("Web Development");
        domain.setReceiverName("Norman Osborne");
        domain.setReceiverCompany("Osborne Tower");
        domain.setReceiverDepartment("Military Tech");
        domain.setEta(OffsetDateTime.now());
        domain.setFailedReason("Had some dirt in the eye");
        domain.setFailedReasonCode("missed the part where that's a problem");
        Coordinate pos1 = new Coordinate();
        pos1.setLat(new BigDecimal("1.123"));
        pos1.setLon(new BigDecimal("-90.123"));
        domain.setMilestoneCoordinates(pos1);
        MilestoneAdditionalInfo additionalInfo = new MilestoneAdditionalInfo();
        HostedFile file1 = new HostedFile();
        file1.setFileName("img1.jpg");
        file1.setFileUrl("https://example/img/1");
        file1.setFileSize(1000L);
        HostedFile file2 = new HostedFile();
        file2.setFileName("sig1.jpg");
        file2.setFileUrl("https://example/sig/1");
        file2.setFileSize(2000L);
        additionalInfo.setImages(List.of(file1));
        additionalInfo.setSignature(List.of(file2));
        additionalInfo.setRemarks("This is a milestone event description");
        domain.setAdditionalInfo(additionalInfo);
        domain.setUserName("Dr. Strange");
        domain.setSource(MilestoneSource.ORG);

        OrganizationEntity organizationEntityDummy = new OrganizationEntity();
        organizationEntityDummy.setId(orgId);
        ShipmentEntity shipmentEntityDummy = new ShipmentEntity();
        shipmentEntityDummy.setId(shipmentId);
        when(entityManager.getReference(ShipmentEntity.class, shipmentId)).thenReturn(shipmentEntityDummy);
        PackageJourneySegmentEntity segmentEntityDummy = new PackageJourneySegmentEntity();
        segmentEntityDummy.setId(segmentId);
        when(entityManager.getReference(PackageJourneySegmentEntity.class, segmentId)).thenReturn(segmentEntityDummy);
        MilestoneEntity entity = mapper.toEntity(domain);

        assertThat(entity)
                .usingRecursiveComparison()
                .ignoringFields("organizationId", "segment", "shipment", "organisationId", "version", "additionalInfo", "eta", "milestoneCoordinates", "milestoneTime")
                .isEqualTo(domain);

        assertThat(entity.getSegment().getId()).isEqualTo(domain.getSegmentId());
        assertThat(entity.getShipment().getId()).isEqualTo(domain.getShipmentId());
        assertThat(entity.getOrganizationId()).isEqualTo(domain.getOrganizationId());
        assertThat(entity.getAdditionalInfo()).isNotNull();
        assertThat(domain.getEta().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx"))).isEqualTo(entity.getEta());
        assertThat(entity.getMilestoneCoordinates()).isNotNull();
        assertThat(domain.getMilestoneTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx"))).isIn(entity.getMilestoneTime());
        assertThat(entity.getSource()).isEqualTo(domain.getSource());
    }

    @Test
    @DisplayName("given milestoneEventEntity when mapEntityToDomain then return expected milestoneEvent")
    void returnExpectedWhenMapEntityToDomain() {
        MilestoneEntity entity = new MilestoneEntity();
        entity.setId("0001");
        entity.setExternalOrderId("O-0001");
        entity.setMilestoneCode(OM_BOOKED);
        entity.setMilestoneTime("2023-01-31T14:31:59.000+08:00");
        entity.setMilestoneName("Order Booked");
        entity.setData(testUtil.getDataFromFile("samplepayload/dispatchmodule-milestone-event.json"));
        entity.setUserName("Dr. Strange");
        ShipmentEntity dummyShipment = new ShipmentEntity();
        dummyShipment.setId("SHIPMENT-ID-1");
        entity.setShipment(dummyShipment);
        PackageJourneySegmentEntity dummySegment = new PackageJourneySegmentEntity();
        dummySegment.setId("SEGMENT-ID-1");
        entity.setSegment(dummySegment);
        entity.setServiceType("pickup service");
        entity.setJobType("pickup job");
        entity.setFromLocationId("a1234");
        entity.setFromCountryId("b1234");
        entity.setFromStateId("c1234");
        entity.setFromCityId("d1234");
        entity.setFromWardId("e1234");
        entity.setFromDistrictId("f1234");
        Coordinate fromCoordinate = new Coordinate(new BigDecimal("1.1"), new BigDecimal("1.2"));
        entity.setFromCoordinates(fromCoordinate);
        entity.setToLocationId("a5678");
        entity.setToCountryId("b5678");
        entity.setToStateId("c5678");
        entity.setToCityId("d5678");
        entity.setToWardId("e5678");
        entity.setToDistrictId("f5678");
        Coordinate toCoordinate = new Coordinate(new BigDecimal("2.1"), new BigDecimal("2.2"));
        entity.setToCoordinates(toCoordinate);
        entity.setUserId("user1234");
        entity.setPartnerId("partner1234");
        entity.setHubId("hub1");
        entity.setDriverId("driver1");
        entity.setDriverName("Ben Parker");
        entity.setDriverPhoneCode("+1");
        entity.setDriverPhoneNumber("202-555-0000");
        entity.setDriverEmail("driver@example.com");
        entity.setVehicleId("car1");
        entity.setVehicleType("Station Wagon");
        entity.setVehicleName("1965 Ford Country Squire");
        entity.setVehicleNumber("500");
        entity.setSenderName("Peter Parker");
        entity.setSenderCompany("Stark Tower");
        entity.setSenderDepartment("Web Development");
        entity.setReceiverName("Norman Osborne");
        entity.setReceiverCompany("Osborne Tower");
        entity.setReceiverDepartment("Military Tech");
        entity.setEta("2023-03-31T14:31:59.000+08:00");
        entity.setFailedReason("Had some dirt in the eye");
        entity.setFailedReasonCode("missed the part where that's a problem");
        entity.setMilestoneCoordinates(new Coordinate(BigDecimal.valueOf(1.123), BigDecimal.valueOf(-90.456)));
        entity.setAdditionalInfo(new MilestoneAdditionalInfo());
        entity.getAdditionalInfo().setRemarks("This is a milestone event description");
        entity.setSource(MilestoneSource.ORG);

        Milestone domain = mapper.toDomain(entity);

        assertThat(domain)
                .usingRecursiveComparison()
                .ignoringFields("segmentId", "shipmentId", "organisationId", "additionalInfo",
                        "eta", "milestoneCoordinates", "milestoneTime", "segmentUpdatedFromMilestone", "hubCityId",
                        "hubStateId", "hubCountryId", "hubTimeZone", "shipmentTrackingId")
                .isEqualTo(entity);

        assertThat(domain.getSegmentId()).isEqualTo(entity.getSegment().getId());
        assertThat(domain.getShipmentId()).isEqualTo(entity.getShipment().getId());
        assertThat(domain.getAdditionalInfo()).isNotNull();
        assertThat(domain.getOrganizationId()).isEqualTo(entity.getOrganizationId());
        assertThat(domain.getEta()).isIn(entity.getEta());
        assertThat(domain.getMilestoneCoordinates()).isNotNull();
        assertThat(domain.getMilestoneTime()).isIn(entity.getMilestoneTime());
        assertThat(domain.getSource()).isEqualTo(entity.getSource());
    }

    @Test
    void convert_withValidMessage_shouldReturnMilestoneEvent() throws JsonProcessingException {
        JsonNode data = testUtil.getDataFromFile("samplepayload/dispatchmodule-milestone-event.json");
        Milestone result = mapper.convertMessageToDomain(data.toString());

        assertThat(result)
                .extracting(Milestone::getMilestoneCode, Milestone::getShipmentTrackingId, Milestone::getSegmentId)
                .containsExactly(DSP_PICKUP_FAILED, "QC0123081500006-01", "ff808081860ba4bd01860babcf380004");
    }

    @Test
    void test_mapNonEmptyDomainFieldsToEntity() {
        //Given
        MilestoneEntity entity = new MilestoneEntity();
        Milestone milestone = new Milestone();
        String milstoneDomainId = "milstoneDomainId";
        milestone.setId(milstoneDomainId);
        milestone.setExternalOrderId("external-order-id-1");
        String genericString = "updated";
        String driverId = "driverId";
        String milestoneEntityId = "milestoneEntityId";
        entity.setId(milestoneEntityId);
        entity.setServiceType("MilestoneEntity");
        entity.setDriverId(driverId);
        entity.setJobType("JobType");
        entity.setFromLocationId("LocationId");
        entity.setFromCountryId("CountryId");
        entity.setFromStateId("StateId");
        entity.setFromCityId("CityId");
        entity.setToLocationId("LocationId");
        entity.setToCountryId("CountryId");
        entity.setToStateId("StateId");
        entity.setToCityId("CityId");
        entity.setPartnerId("PartnerId");
        entity.setUserId("UserId");
        entity.setHubId("HubId");
        entity.setDriverName("DriverName");
        entity.setDriverPhoneCode("PhoneCode");
        entity.setDriverPhoneNumber("PhoneNumber");
        entity.setVehicleId("VehicleId");
        entity.setVehicleType("VehicleTy");
        entity.setVehicleName("VehicleName");
        entity.setVehicleNumber("VehicleNumer");
        entity.setSenderName("SenderName");
        entity.setReceiverName("ReceiverName");
        entity.setFailedReason("FAiledReason");
        entity.setFailedReasonCode("failedReasonCode");
        milestone.setServiceType("Milestone");

        //When
        mapper.toEntity(entity, milestone);

        //Then
        mapNonEmptyDomainFieldsToEntity_assertBeforeChange(entity, milestone);
        milestone.setDriverId(genericString);
        milestone.setJobType(genericString);
        milestone.setFromLocationId(genericString);
        milestone.setFromCountryId(genericString);
        milestone.setFromStateId(genericString);
        milestone.setFromCityId(genericString);
        milestone.setToLocationId(genericString);
        milestone.setToCountryId(genericString);
        milestone.setToStateId(genericString);
        milestone.setToCityId(genericString);
        milestone.setPartnerId(genericString);
        milestone.setUserId(genericString);
        milestone.setHubId(genericString);
        milestone.setDriverName(genericString);
        milestone.setDriverPhoneCode(genericString);
        milestone.setDriverPhoneNumber(genericString);
        milestone.setVehicleId(genericString);
        milestone.setVehicleType(genericString);
        milestone.setVehicleName(genericString);
        milestone.setVehicleNumber(genericString);
        milestone.setSenderName(genericString);
        milestone.setReceiverName(genericString);
        milestone.setFailedReason(genericString);
        milestone.setFailedReasonCode(genericString);
        mapper.toEntity(entity, milestone);
        mapNonEmptyDomainFieldsToEntity_assertAfterChange(entity, milestone);
    }

    private void mapNonEmptyDomainFieldsToEntity_assertBeforeChange(MilestoneEntity entity, Milestone milestone) {
        assertThat(entity.getId()).isEqualTo("milestoneEntityId");
        assertThat(entity.getDriverId()).isNotEqualTo(milestone.getDriverId());
        assertThat(entity.getJobType()).isNotEqualTo(milestone.getJobType());
        assertThat(entity.getFromLocationId()).isNotEqualTo(milestone.getFromLocationId());
        assertThat(entity.getFromCountryId()).isNotEqualTo(milestone.getFromCountryId());
        assertThat(entity.getFromStateId()).isNotEqualTo(milestone.getFromStateId());
        assertThat(entity.getFromCityId()).isNotEqualTo(milestone.getFromCityId());
        assertThat(entity.getToLocationId()).isNotEqualTo(milestone.getToLocationId());
        assertThat(entity.getToCountryId()).isNotEqualTo(milestone.getToCountryId());
        assertThat(entity.getToStateId()).isNotEqualTo(milestone.getToStateId());
        assertThat(entity.getToCityId()).isNotEqualTo(milestone.getToCityId());
        assertThat(entity.getPartnerId()).isNotEqualTo(milestone.getPartnerId());
        assertThat(entity.getUserId()).isNotEqualTo(milestone.getUserId());
        assertThat(entity.getHubId()).isNotEqualTo(milestone.getHubId());
        assertThat(entity.getDriverName()).isNotEqualTo(milestone.getDriverName());
        assertThat(entity.getDriverPhoneCode()).isNotEqualTo(milestone.getDriverPhoneCode());
        assertThat(entity.getDriverPhoneNumber()).isNotEqualTo(milestone.getDriverPhoneNumber());
        assertThat(entity.getVehicleId()).isNotEqualTo(milestone.getVehicleId());
        assertThat(entity.getVehicleType()).isNotEqualTo(milestone.getVehicleType());
        assertThat(entity.getVehicleName()).isNotEqualTo(milestone.getVehicleName());
        assertThat(entity.getVehicleNumber()).isNotEqualTo(milestone.getVehicleNumber());
        assertThat(entity.getSenderName()).isNotEqualTo(milestone.getSenderName());
        assertThat(entity.getReceiverName()).isNotEqualTo(milestone.getReceiverName());
        assertThat(entity.getFailedReason()).isNotEqualTo(milestone.getFailedReason());
        assertThat(entity.getFailedReasonCode()).isNotEqualTo(milestone.getFailedReasonCode());
        assertThat(entity.getAdditionalInfo()).isNull();
        assertThat(entity.getMilestoneCoordinates()).isNull();
    }

    private void mapNonEmptyDomainFieldsToEntity_assertAfterChange(MilestoneEntity entity, Milestone milestone) {
        assertThat(entity.getDriverId()).isEqualTo(milestone.getDriverId());
        assertThat(entity.getJobType()).isEqualTo(milestone.getJobType());
        assertThat(entity.getFromLocationId()).isEqualTo(milestone.getFromLocationId());
        assertThat(entity.getFromCountryId()).isEqualTo(milestone.getFromCountryId());
        assertThat(entity.getFromStateId()).isEqualTo(milestone.getFromStateId());
        assertThat(entity.getFromCityId()).isEqualTo(milestone.getFromCityId());
        assertThat(entity.getToLocationId()).isEqualTo(milestone.getToLocationId());
        assertThat(entity.getToCountryId()).isEqualTo(milestone.getToCountryId());
        assertThat(entity.getToStateId()).isEqualTo(milestone.getToStateId());
        assertThat(entity.getToCityId()).isEqualTo(milestone.getToCityId());
        assertThat(entity.getPartnerId()).isEqualTo(milestone.getPartnerId());
        assertThat(entity.getUserId()).isEqualTo(milestone.getUserId());
        assertThat(entity.getHubId()).isEqualTo(milestone.getHubId());
        assertThat(entity.getDriverName()).isEqualTo(milestone.getDriverName());
        assertThat(entity.getDriverPhoneCode()).isEqualTo(milestone.getDriverPhoneCode());
        assertThat(entity.getDriverPhoneNumber()).isEqualTo(milestone.getDriverPhoneNumber());
        assertThat(entity.getVehicleId()).isEqualTo(milestone.getVehicleId());
        assertThat(entity.getVehicleType()).isEqualTo(milestone.getVehicleType());
        assertThat(entity.getVehicleName()).isEqualTo(milestone.getVehicleName());
        assertThat(entity.getVehicleNumber()).isEqualTo(milestone.getVehicleNumber());
        assertThat(entity.getSenderName()).isEqualTo(milestone.getSenderName());
        assertThat(entity.getReceiverName()).isEqualTo(milestone.getReceiverName());
        assertThat(entity.getFailedReason()).isEqualTo(milestone.getFailedReason());
        assertThat(entity.getFailedReasonCode()).isEqualTo(milestone.getFailedReasonCode());
        assertThat(entity.getExternalOrderId()).isEqualTo(milestone.getExternalOrderId());
    }
}
