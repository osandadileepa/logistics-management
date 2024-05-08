package com.quincus.shipment.kafka.producers.mapper;

import com.quincus.shipment.api.constant.MilestoneCode;
import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Cod;
import com.quincus.shipment.api.domain.Coordinate;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.HostedFile;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.MilestoneAdditionalInfo;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.api.domain.Package;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.dto.ShipmentMessageDto;
import com.quincus.shipment.kafka.producers.message.MilestoneAdditionalInfoMsgPart;
import com.quincus.shipment.kafka.producers.message.MilestoneMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static com.quincus.shipment.api.constant.MilestoneCode.DSP_ON_ROUTE_TO_PICKUP;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MilestoneMessageMapperTest {

    @Test
    void createMilestoneMessage_shouldNotThrowNPE() {
        PackageJourneySegment segment = new PackageJourneySegment();
        Milestone milestone = new Milestone();
        milestone.setMilestoneRefId("milestone1");
        milestone.setMilestoneCode(MilestoneCode.OM_BOOKED);
        milestone.setMilestoneTime(OffsetDateTime.now());
        MilestoneMessage message = MilestoneMessageMapper.createMilestoneMessage(createShipment(), segment, milestone);
        assertThat(message).isNotNull();
    }

    @Test
    void createMilestoneMessage_shouldMatchShipmentValues() {
        PackageJourneySegment segment = new PackageJourneySegment();
        Milestone milestone = new Milestone();
        milestone.setMilestoneRefId("milestone1");
        milestone.setUserId("otherUserId");
        milestone.setMilestoneCode(MilestoneCode.OM_BOOKED);
        milestone.setMilestoneTime(OffsetDateTime.now());
        MilestoneMessage message = MilestoneMessageMapper.createMilestoneMessage(createShipment(), segment, milestone);
        assertThat(message.getPackageId()).isEqualTo("shipmentPackageId");
        assertThat(message.getUserId()).isEqualTo("otherUserId");
        assertThat(message.getShipmentId()).isEqualTo("shipmentId");
        assertThat(message.getOrganizationId()).isEqualTo("organizationId");
    }

    @Test
    void createMilestoneMessage_shouldMatchEndFacilityValues() {
        PackageJourneySegment segment = new PackageJourneySegment();
        Milestone milestone = new Milestone();
        milestone.setMilestoneRefId("milestone1");
        milestone.setMilestoneCode(MilestoneCode.OM_BOOKED);
        milestone.setMilestoneTime(OffsetDateTime.now());
        segment.setEndFacility(createFacility("EndCountryId", "EndStateId", "EndCityId", "EndExternalId"));
        MilestoneMessage message = MilestoneMessageMapper.createMilestoneMessage(createShipment(), segment, milestone);
        assertThat(message.getToLocationId()).isEqualTo("EndExternalId");
        assertThat(message.getToCountryId()).isEqualTo("EndCountryId");
        assertThat(message.getToStateId()).isEqualTo("EndStateId");
        assertThat(message.getToCityId()).isEqualTo("EndCityId");
    }

    @Test
    void createMilestoneMessage_shouldMatchStartFacilityValues() {
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setStartFacility(createFacility("StartCountryId", "StartStateId", "StartCityId", "StartExternalId"));
        Milestone milestone = new Milestone();
        milestone.setMilestoneRefId("milestone1");
        milestone.setMilestoneCode(MilestoneCode.OM_BOOKED);
        milestone.setMilestoneTime(OffsetDateTime.now());
        MilestoneMessage message = MilestoneMessageMapper.createMilestoneMessage(createShipment(), segment, milestone);
        assertThat(message.getFromLocationId()).isEqualTo("StartExternalId");
        assertThat(message.getFromCountryId()).isEqualTo("StartCountryId");
        assertThat(message.getFromStateId()).isEqualTo("StartStateId");
        assertThat(message.getFromCityId()).isEqualTo("StartCityId");
    }

    @Test
    void createMilestoneMessage_shouldMatchMilestoneValues() {
        PackageJourneySegment segment = new PackageJourneySegment();
        Milestone milestone = new Milestone();
        milestone.setMilestoneTime(OffsetDateTime.now());
        Coordinate coordinate = new Coordinate();
        BigDecimal latitude = new BigDecimal("12.12345");
        coordinate.setLat(latitude);
        BigDecimal longitude = new BigDecimal("56.2245");
        coordinate.setLon(longitude);
        milestone.setMilestoneCoordinates(coordinate);
        milestone.setMilestoneCode(MilestoneCode.DSP_AIR_ARRIVED_AT_DELIVERY_LOCATION);
        milestone.setMilestoneName("MilestoneName");
        milestone.setHubId("hubid");
        milestone.setId("milestoneId");
        milestone.setSenderName("Sender");
        milestone.setReceiverName("Receiver");
        MilestoneAdditionalInfo additionalInfo = new MilestoneAdditionalInfo();
        HostedFile hostedFile = new HostedFile();
        hostedFile.setFileName("test-file.jpg");
        hostedFile.setFileTimestamp(OffsetDateTime.now());
        HostedFile hostedFile2 = new HostedFile();
        hostedFile2.setFileName("test-file2.jpg");
        hostedFile2.setFileTimestamp(OffsetDateTime.now());
        additionalInfo.setImages(List.of(hostedFile));
        additionalInfo.setSignature(List.of(hostedFile2));
        Cod cod = new Cod();
        cod.setAmount(new BigDecimal("100"));
        additionalInfo.setCod(cod);
        milestone.setAdditionalInfo(additionalInfo);
        MilestoneMessage message = MilestoneMessageMapper.createMilestoneMessage(createShipment(), segment, milestone);
        assertThat(message.getId()).isEqualTo("milestoneId");
        assertThat(message.getHubId()).isEqualTo("hubid");
        assertThat(message.getMilestoneName()).isEqualTo("MilestoneName");
        assertThat(message.getMilestoneCode()).isEqualTo(MilestoneCode.DSP_AIR_ARRIVED_AT_DELIVERY_LOCATION.toString());
        assertThat(message.getLatitude()).isEqualTo(latitude.toString());
        assertThat(message.getLongitude()).isEqualTo(longitude.toString());
        assertThat(message.getSenderName()).isEqualTo(milestone.getSenderName());
        assertThat(message.getReceiverName()).isEqualTo(milestone.getReceiverName());
        MilestoneAdditionalInfoMsgPart additionalInfoMsgPart = message.getAdditionalInfo();
        assertThat(additionalInfoMsgPart).isNotNull();
        assertThat(additionalInfoMsgPart.getCod().getAmount()).isEqualTo("100");
        assertThat(message.getAdditionalInfo().getImages()).isEqualTo(additionalInfo.getImages());
        assertThat(message.getAdditionalInfo().getSignature()).isEqualTo(additionalInfo.getSignature());
        assertThat(message.getAdditionalInfo().getAttachments()).isEqualTo(additionalInfo.getAttachments());
        assertThat(message.getAdditionalInfo().getImages().get(0).getFileTimestamp()).isEqualTo(hostedFile.getFileTimestamp());
        assertThat(message.getAdditionalInfo().getSignature().get(0).getFileTimestamp()).isEqualTo(hostedFile2.getFileTimestamp());
    }

    @Test
    void createFlightMilestoneMessage_shouldMatchMilestoneValues() {
        PackageJourneySegment segment = new PackageJourneySegment();
        Milestone milestone = new Milestone();
        milestone.setMilestoneTime(OffsetDateTime.now());
        Coordinate coordinate = new Coordinate();
        BigDecimal latitude = new BigDecimal("12.12345");
        coordinate.setLat(latitude);
        BigDecimal longitude = new BigDecimal("56.2245");
        coordinate.setLon(longitude);
        milestone.setUserId("otherUserId");
        milestone.setMilestoneCoordinates(coordinate);
        milestone.setMilestoneCode(MilestoneCode.SHP_FLIGHT_DEPARTED);
        milestone.setMilestoneName("MilestoneName");
        milestone.setHubId("hubid");
        milestone.setId("milestoneId");
        milestone.setSenderName("Sender");
        milestone.setReceiverName("Receiver");
        MilestoneMessage message = MilestoneMessageMapper.createFlightMilestoneMessage(createDummyShipmentDto(), segment, milestone);
        assertThat(message.getId()).isEqualTo("milestoneId");
        assertThat(message.getHubId()).isEqualTo("hubid");
        assertThat(message.getMilestoneName()).isEqualTo("MilestoneName");
        assertThat(message.getMilestoneCode()).isEqualTo(MilestoneCode.SHP_FLIGHT_DEPARTED.toString());
        assertThat(message.getLatitude()).isEqualTo(latitude.toString());
        assertThat(message.getLongitude()).isEqualTo(longitude.toString());
        assertThat(message.getSenderName()).isEqualTo(milestone.getSenderName());
        assertThat(message.getReceiverName()).isEqualTo(milestone.getReceiverName());
        assertThat(message.getUserId()).isNull();
    }

    @Test
    void createMilestoneMessage_pickupSuccessful_shouldMatchMilestoneValues() {
        PackageJourneySegment segment = new PackageJourneySegment();
        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(MilestoneCode.DSP_PICKUP_SUCCESSFUL);
        milestone.setMilestoneTime(OffsetDateTime.now());
        milestone.setSenderName("Sender");
        milestone.setReceiverName("Receiver");
        milestone.setUserId("otherUserId");
        MilestoneAdditionalInfo additionalInfo = new MilestoneAdditionalInfo();
        HostedFile popImage = new HostedFile();
        popImage.setFileName("test-file.jpg");
        popImage.setFileUrl("http://1");
        popImage.setFileSize(1000L);
        popImage.setFileTimestamp(OffsetDateTime.now());
        HostedFile popSignature = new HostedFile();
        popSignature.setFileName("test-file2.jpg");
        popSignature.setFileUrl("http://2");
        popSignature.setFileSize(200L);
        popSignature.setFileTimestamp(OffsetDateTime.now());
        HostedFile popAttachment = new HostedFile();
        popAttachment.setFileName("test-file3.jpg");
        popAttachment.setFileUrl("http://3");
        popAttachment.setFileSize(30L);
        popAttachment.setFileTimestamp(OffsetDateTime.now());
        additionalInfo.setImages(List.of(popImage));
        additionalInfo.setSignature(List.of(popSignature));
        additionalInfo.setAttachments(List.of(popAttachment));
        milestone.setAdditionalInfo(additionalInfo);
        Shipment shipment = createShipment();
        MilestoneMessage message = MilestoneMessageMapper.createMilestoneMessage(shipment, segment, milestone);
        assertThat(message).isNotNull();
        assertThat(message.getUserId()).isEqualTo("otherUserId");
        MilestoneAdditionalInfoMsgPart additionalInfoMsgPart = message.getAdditionalInfo();
        assertThat(additionalInfoMsgPart).isNotNull();
        HostedFile actualPopImage = message.getAdditionalInfo().getImages().get(0);
        assertThat(actualPopImage.getFileName()).isEqualTo(popImage.getFileName());
        assertThat(actualPopImage.getFileUrl()).isEqualTo(popImage.getFileUrl());
        assertThat(actualPopImage.getFileSize()).isEqualTo(popImage.getFileSize());
        assertThat(actualPopImage.getFileTimestamp()).isEqualTo(popImage.getFileTimestamp());
        HostedFile actualPopSignature = message.getAdditionalInfo().getSignature().get(0);
        assertThat(actualPopSignature.getFileName()).isEqualTo(popSignature.getFileName());
        assertThat(actualPopSignature.getFileUrl()).isEqualTo(popSignature.getFileUrl());
        assertThat(actualPopSignature.getFileSize()).isEqualTo(popSignature.getFileSize());
        assertThat(actualPopSignature.getFileTimestamp()).isEqualTo(popSignature.getFileTimestamp());
        HostedFile actualPopAttachment = message.getAdditionalInfo().getAttachments().get(0);
        assertThat(actualPopAttachment.getFileName()).isEqualTo(popAttachment.getFileName());
        assertThat(actualPopAttachment.getFileUrl()).isEqualTo(popAttachment.getFileUrl());
        assertThat(actualPopAttachment.getFileSize()).isEqualTo(popAttachment.getFileSize());
        assertThat(actualPopAttachment.getFileTimestamp()).isEqualTo(popAttachment.getFileTimestamp());
    }

    @Test
    void createMilestoneMessage_deliverySuccessful_shouldMatchMilestoneValues() {
        PackageJourneySegment segment = new PackageJourneySegment();
        Milestone milestone = new Milestone();
        milestone.setUserId("otherUserId");
        milestone.setMilestoneCode(MilestoneCode.DSP_DELIVERY_SUCCESSFUL);
        milestone.setMilestoneTime(OffsetDateTime.now());
        milestone.setSenderName("Sender");
        milestone.setReceiverName("Receiver");
        MilestoneAdditionalInfo additionalInfo = new MilestoneAdditionalInfo();
        HostedFile podImage = new HostedFile();
        podImage.setFileName("test-file.jpg");
        podImage.setFileUrl("http://1");
        podImage.setFileSize(1000L);
        podImage.setFileTimestamp(OffsetDateTime.now());
        HostedFile podSignature = new HostedFile();
        podSignature.setFileName("test-file2.jpg");
        podSignature.setFileUrl("http://2");
        podSignature.setFileSize(200L);
        podSignature.setFileTimestamp(OffsetDateTime.now());
        HostedFile podAttachment = new HostedFile();
        podAttachment.setFileName("test-file3.jpg");
        podAttachment.setFileUrl("http://3");
        podAttachment.setFileSize(30L);
        podAttachment.setFileTimestamp(OffsetDateTime.now());
        additionalInfo.setImages(List.of(podImage));
        additionalInfo.setSignature(List.of(podSignature));
        additionalInfo.setAttachments(List.of(podAttachment));
        milestone.setAdditionalInfo(additionalInfo);
        Shipment shipment = createShipment();
        MilestoneMessage message = MilestoneMessageMapper.createMilestoneMessage(shipment, segment, milestone);
        assertThat(message).isNotNull();
        assertThat(message.getUserId()).isEqualTo("otherUserId");
        MilestoneAdditionalInfoMsgPart additionalInfoMsgPart = message.getAdditionalInfo();
        assertThat(additionalInfoMsgPart).isNotNull();
        HostedFile actualPodImage = message.getAdditionalInfo().getImages().get(0);
        assertThat(actualPodImage.getFileName()).isEqualTo(podImage.getFileName());
        assertThat(actualPodImage.getFileUrl()).isEqualTo(podImage.getFileUrl());
        assertThat(actualPodImage.getFileSize()).isEqualTo(podImage.getFileSize());
        assertThat(actualPodImage.getFileTimestamp()).isEqualTo(podImage.getFileTimestamp());
        HostedFile actualPodSignature = message.getAdditionalInfo().getSignature().get(0);
        assertThat(actualPodSignature.getFileName()).isEqualTo(podSignature.getFileName());
        assertThat(actualPodSignature.getFileUrl()).isEqualTo(podSignature.getFileUrl());
        assertThat(actualPodSignature.getFileSize()).isEqualTo(podSignature.getFileSize());
        assertThat(actualPodSignature.getFileTimestamp()).isEqualTo(podSignature.getFileTimestamp());
        HostedFile actualPodAttachment = message.getAdditionalInfo().getAttachments().get(0);
        assertThat(actualPodAttachment.getFileName()).isEqualTo(podAttachment.getFileName());
        assertThat(actualPodAttachment.getFileUrl()).isEqualTo(podAttachment.getFileUrl());
        assertThat(actualPodAttachment.getFileSize()).isEqualTo(podAttachment.getFileSize());
        assertThat(actualPodAttachment.getFileTimestamp()).isEqualTo(podAttachment.getFileTimestamp());
    }

    @Test
    void createMilestoneMessage_failedPickup_shouldMatchMilestoneValues() {
        PackageJourneySegment segment = new PackageJourneySegment();
        Milestone milestone = new Milestone();
        milestone.setUserId("otherUserId");
        milestone.setMilestoneCode(MilestoneCode.DSP_PICKUP_FAILED);
        milestone.setMilestoneTime(OffsetDateTime.now());
        milestone.setSenderName("Sender");
        milestone.setReceiverName("Receiver");
        MilestoneAdditionalInfo additionalInfo = new MilestoneAdditionalInfo();
        HostedFile image = new HostedFile();
        image.setFileName("test-file.jpg");
        image.setFileUrl("http://1");
        image.setFileSize(1000L);
        image.setFileTimestamp(OffsetDateTime.now());
        HostedFile signature = new HostedFile();
        signature.setFileName("test-file2.jpg");
        signature.setFileUrl("http://2");
        signature.setFileSize(200L);
        signature.setFileTimestamp(OffsetDateTime.now());
        additionalInfo.setImages(List.of(image));
        additionalInfo.setSignature(List.of(signature));
        milestone.setAdditionalInfo(additionalInfo);
        Shipment shipment = createShipment();
        MilestoneMessage message = MilestoneMessageMapper.createMilestoneMessage(shipment, segment, milestone);
        assertThat(message).isNotNull();
        assertThat(message.getUserId()).isEqualTo("otherUserId");
        MilestoneAdditionalInfoMsgPart additionalInfoMsgPart = message.getAdditionalInfo();
        assertThat(additionalInfoMsgPart).isNotNull();
        HostedFile actualFailedPickupImage = message.getAdditionalInfo().getImages().get(0);
        assertThat(actualFailedPickupImage.getFileName()).isEqualTo(image.getFileName());
        assertThat(actualFailedPickupImage.getFileUrl()).isEqualTo(image.getFileUrl());
        assertThat(actualFailedPickupImage.getFileSize()).isEqualTo(image.getFileSize());
        assertThat(actualFailedPickupImage.getFileTimestamp()).isEqualTo(image.getFileTimestamp());
        HostedFile actualFailedPickupSignature = message.getAdditionalInfo().getSignature().get(0);
        assertThat(actualFailedPickupSignature.getFileName()).isEqualTo(signature.getFileName());
        assertThat(actualFailedPickupSignature.getFileUrl()).isEqualTo(signature.getFileUrl());
        assertThat(actualFailedPickupSignature.getFileSize()).isEqualTo(signature.getFileSize());
        assertThat(actualFailedPickupSignature.getFileTimestamp()).isEqualTo(signature.getFileTimestamp());
        assertThat(message.getAdditionalInfo().getAttachments()).isNull();
    }

    @Test
    void createMilestoneMessage_failedDelivery_shouldMatchMilestoneValues() {
        PackageJourneySegment segment = new PackageJourneySegment();
        Milestone milestone = new Milestone();
        milestone.setUserId("otherUserId");
        milestone.setMilestoneCode(MilestoneCode.DSP_DELIVERY_FAILED);
        milestone.setMilestoneTime(OffsetDateTime.now());
        milestone.setSenderName("Sender");
        milestone.setReceiverName("Receiver");
        MilestoneAdditionalInfo additionalInfo = new MilestoneAdditionalInfo();
        HostedFile image = new HostedFile();
        image.setFileName("test-file.jpg");
        image.setFileUrl("http://1");
        image.setFileSize(1000L);
        image.setFileTimestamp(OffsetDateTime.now());
        HostedFile signature = new HostedFile();
        signature.setFileName("test-file2.jpg");
        signature.setFileUrl("http://2");
        signature.setFileSize(200L);
        signature.setFileTimestamp(OffsetDateTime.now());
        additionalInfo.setImages(List.of(image));
        additionalInfo.setSignature(List.of(signature));
        milestone.setAdditionalInfo(additionalInfo);
        Shipment shipment = createShipment();
        MilestoneMessage message = MilestoneMessageMapper.createMilestoneMessage(shipment, segment, milestone);
        assertThat(message).isNotNull();
        assertThat(message.getUserId()).isEqualTo("otherUserId");
        MilestoneAdditionalInfoMsgPart additionalInfoMsgPart = message.getAdditionalInfo();
        assertThat(additionalInfoMsgPart).isNotNull();
        HostedFile actualFailedDeliveryImage = message.getAdditionalInfo().getImages().get(0);
        assertThat(actualFailedDeliveryImage.getFileName()).isEqualTo(image.getFileName());
        assertThat(actualFailedDeliveryImage.getFileUrl()).isEqualTo(image.getFileUrl());
        assertThat(actualFailedDeliveryImage.getFileSize()).isEqualTo(image.getFileSize());
        assertThat(actualFailedDeliveryImage.getFileTimestamp()).isEqualTo(image.getFileTimestamp());
        HostedFile actualFailedDeliverySignature = message.getAdditionalInfo().getSignature().get(0);
        assertThat(actualFailedDeliverySignature.getFileName()).isEqualTo(signature.getFileName());
        assertThat(actualFailedDeliverySignature.getFileUrl()).isEqualTo(signature.getFileUrl());
        assertThat(actualFailedDeliverySignature.getFileSize()).isEqualTo(signature.getFileSize());
        assertThat(actualFailedDeliverySignature.getFileTimestamp()).isEqualTo(signature.getFileTimestamp());
        assertThat(message.getAdditionalInfo().getAttachments()).isNull();
    }

    @Test
    void createMilestoneMessage_noSegmentId_WhenOrderBooked() {
        PackageJourneySegment segment = new PackageJourneySegment();
        Milestone milestone = new Milestone();
        milestone.setMilestoneRefId("milestone1");
        milestone.setMilestoneCode(MilestoneCode.OM_BOOKED);
        milestone.setMilestoneTime(OffsetDateTime.now());
        MilestoneMessage message = MilestoneMessageMapper.createMilestoneMessage(createShipment(), segment, milestone);
        assertThat(message).isNotNull();
        assertThat(message.getSegmentId()).isNull();
    }

    @Test
    void createMilestoneMessage_driverAndVehicleInfo_shouldMatchMilestoneValues() {
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId("segment-1");
        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(DSP_ON_ROUTE_TO_PICKUP);
        milestone.setMilestoneTime(OffsetDateTime.now());
        String driverId = "driver-id-1";
        String driverName = "Mr Bushido";
        String driverPhoneCode = "+69";
        String driverPhoneNumber = "2221110000";
        String driverEmail = "mb@example.com";
        milestone.setDriverId(driverId);
        milestone.setDriverName(driverName);
        milestone.setDriverPhoneCode(driverPhoneCode);
        milestone.setDriverPhoneNumber(driverPhoneNumber);
        milestone.setDriverEmail(driverEmail);
        String vehicleId = "car-id-1";
        String vehicleType = "sedan";
        String vehicleName = "Civic";
        String vehicleNumber = "ABC123";
        milestone.setVehicleId(vehicleId);
        milestone.setVehicleType(vehicleType);
        milestone.setVehicleName(vehicleName);
        milestone.setVehicleNumber(vehicleNumber);
        MilestoneMessage message = MilestoneMessageMapper.createMilestoneMessage(createShipment(), segment, milestone);
        assertThat(message).isNotNull();
        assertThat(message.getDriverId()).isEqualTo(driverId);
        assertThat(message.getDriverName()).isEqualTo(driverName);
        assertThat(message.getDriverPhoneCode()).isEqualTo(driverPhoneCode);
        assertThat(message.getDriverPhoneNumber()).isEqualTo(driverPhoneNumber);
        assertThat(message.getDriverEmail()).isEqualTo(driverEmail);
        assertThat(message.getVehicleId()).isEqualTo(vehicleId);
        assertThat(message.getVehicleType()).isEqualTo(vehicleType);
        assertThat(message.getVehicleName()).isEqualTo(vehicleName);
        assertThat(message.getVehicleNumber()).isEqualTo(vehicleNumber);
    }

    @Test
    void createMilestoneMessage_senderAndReceiverDetails_shouldMatchMilestoneValues() {
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId("segment-1");
        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(DSP_ON_ROUTE_TO_PICKUP);
        milestone.setMilestoneTime(OffsetDateTime.now());
        String senderName = "Tony Stark";
        String senderCompany = "sender-company";
        String senderDepartment = "sender-department";
        milestone.setSenderName(senderName);
        milestone.setSenderCompany(senderCompany);
        milestone.setSenderDepartment(senderDepartment);
        String receiverName = "Peter Parker";
        String receiverCompany = "receiver-company";
        String receiverDepartment = "receiver-department";
        milestone.setReceiverName(receiverName);
        milestone.setReceiverCompany(receiverCompany);
        milestone.setReceiverDepartment(receiverDepartment);
        MilestoneMessage message = MilestoneMessageMapper.createMilestoneMessage(createShipment(), segment, milestone);
        assertThat(message).isNotNull();
        assertThat(message.getSenderName()).isEqualTo(senderName);
        assertThat(message.getSenderCompany()).isEqualTo(senderCompany);
        assertThat(message.getSenderDepartment()).isEqualTo(senderDepartment);
        assertThat(message.getReceiverName()).isEqualTo(receiverName);
        assertThat(message.getReceiverCompany()).isEqualTo(receiverCompany);
        assertThat(message.getReceiverDepartment()).isEqualTo(receiverDepartment);
    }

    private Shipment createShipment() {
        Shipment shipment = new Shipment();
        shipment.setId("shipmentId");
        shipment.setUserId("userId");
        Package shipmentPackage = new Package();
        shipmentPackage.setId("shipmentPackageId");
        shipment.setShipmentPackage(shipmentPackage);
        Organization organization = new Organization();
        organization.setId("organizationId");
        shipment.setOrganization(organization);
        HostedFile attachment = new HostedFile();
        attachment.setFileName("attachment1.png");
        attachment.setFileUrl("http://a1");
        attachment.setFileSize(300L);
        shipment.setShipmentAttachments(List.of(attachment));
        return shipment;
    }

    private ShipmentMessageDto createDummyShipmentDto() {
        Organization organization = new Organization();
        organization.setId("shpv2");
        ShipmentMessageDto shipment = new ShipmentMessageDto();
        shipment.setOrganizationId(organization.getId());
        shipment.setUserId(UUID.randomUUID().toString());
        return shipment;
    }

    private Facility createFacility(String country, String state, String city, String ext) {
        Facility endFacility = new Facility();
        Address address = new Address();
        address.setCountryId(country);
        address.setStateId(state);
        address.setCityId(city);
        endFacility.setLocation(address);
        endFacility.setExternalId(ext);
        return endFacility;
    }
}
