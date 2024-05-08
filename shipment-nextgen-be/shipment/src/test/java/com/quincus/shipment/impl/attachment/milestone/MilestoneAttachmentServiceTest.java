package com.quincus.shipment.impl.attachment.milestone;

import com.quincus.shipment.api.constant.AttachmentType;
import com.quincus.shipment.api.dto.csv.MilestoneCsv;
import com.quincus.shipment.impl.attachment.JobTemplateStrategy;
import com.quincus.shipment.impl.mapper.JobMetricsMapper;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.service.JobMetricsService;
import com.quincus.web.common.exception.model.QuincusValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MilestoneAttachmentServiceTest {

    private MilestoneAttachmentService milestoneAttachmentService;

    @Mock
    private JobMetricsService<MilestoneCsv> jobMetricsService;

    @Mock
    private JobTemplateStrategy<MilestoneCsv> jobTemplateStrategy;

    @Mock
    private JobMetricsMapper<MilestoneCsv> milestoneJobMetricsMapper;

    @Mock
    private UserDetailsProvider userDetailsProvider;

    @Mock
    private MultipartFile mockFile;

    @BeforeEach
    void initializeService() {
        milestoneAttachmentService = new MilestoneAttachmentService(jobMetricsService,
                jobTemplateStrategy,
                milestoneJobMetricsMapper,
                userDetailsProvider);
    }

    @Test
    void parseToDomain_validFile_shouldReturnCsvList() throws Exception {
        String shipmentTrackingId = "QC2023022330607325-0102";
        String name = "Pick Up Scheduled";
        String code = "1400";
        String dateTime = "2023-05-08T16:00:00+08:00";
        String fromCountry = "Philippines";
        String fromState = "Metro Manila";
        String fromCity = "Makati";
        String fromWard = "Ward X";
        String fromDistrict = "District Y";
        String fromFacility = "Warehouse A";
        String fromLatitude = "10.001";
        String fromLongitude = "-10.001";
        String toCountry = "USA";
        String toState = "California";
        String toCity = "Los Angeles";
        String toWard = "Ward Q";
        String toDistrict = "District R";
        String toFacility = "Warehouse B";
        String toLatitude = "20.001";
        String toLongitude = "-20.001";
        String latitude = "34.0522";
        String longitude = "-118.2437";
        String hub = "LA Hub";
        String driverName = "John Doe";
        String driverPhoneCode = "+1";
        String driverPhoneNumber = "1234567890";
        String driverEmail = "johnDoe@example.com";
        String vehicleType = "Truck";
        String vehicleName = "Truck A";
        String vehicleNumber = "ABC123";
        String senderName = "Person A";
        String senderCompany = "Company A";
        String senderDepartment = "Department A";
        String receiverName = "Person B";
        String receiverCompany = "Company B";
        String receiverDepartment = "Department B";
        String eta = "2023-05-10T16:00:00+08:00";
        String notes = "Handle with care.";
        String header = String.join(", ", MilestoneCsv.getCsvHeaders());
        String content = String.join(", ", shipmentTrackingId, name, code, dateTime,
                fromCountry, fromState, fromCity, fromWard, fromDistrict, fromFacility, fromLatitude, fromLongitude,
                toCountry, toState, toCity, toWard, toDistrict, toFacility, toLatitude, toLongitude, latitude, longitude,
                hub, driverName, driverPhoneCode, driverPhoneNumber, driverEmail, vehicleType, vehicleName, vehicleNumber,
                senderName, senderCompany, senderDepartment, receiverName, receiverCompany, receiverDepartment, eta, notes);
        String csvContent = String.join("\n", header, content);
        String orgId = "TEST-ORG-1";
        String userId = "TEST-USER-1";

        InputStream is = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

        when(mockFile.getInputStream()).thenReturn(is);
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(orgId);
        when(userDetailsProvider.getCurrentUserId()).thenReturn(userId);

        List<MilestoneCsv> milestoneCsvList = milestoneAttachmentService.parseToDomain(mockFile);

        assertThat(milestoneCsvList).isNotEmpty();

        MilestoneCsv firstMilestoneCsv = milestoneCsvList.get(0);
        assertThat(firstMilestoneCsv.getRecordNumber()).isEqualTo(2);
        assertThat(firstMilestoneCsv.getSize()).isEqualTo(38);
        assertThat(firstMilestoneCsv.getOrganizationId()).isEqualTo(orgId);
        assertThat(firstMilestoneCsv.getUserId()).isEqualTo(userId);
        assertThat(firstMilestoneCsv.getShipmentTrackingId()).isEqualTo(shipmentTrackingId);
        assertThat(firstMilestoneCsv.getMilestoneName()).isEqualTo(name);
        assertThat(firstMilestoneCsv.getMilestoneCode()).isEqualTo(code);
        assertThat(firstMilestoneCsv.getMilestoneTime()).isEqualTo(dateTime);
    }

    @Test
    void parseToDomain_validFile_shouldMapLocationFields() throws Exception {
        String shipmentTrackingId = "QC2023022330607325-0102";
        String name = "Pick Up Scheduled";
        String code = "1400";
        String dateTime = "2023-05-08T16:00:00+08:00";
        String fromCountry = "Philippines";
        String fromState = "Metro Manila";
        String fromCity = "Makati";
        String fromWard = "Ward X";
        String fromDistrict = "District Y";
        String fromFacility = "Warehouse A";
        String fromLatitude = "10.001";
        String fromLongitude = "-10.001";
        String toCountry = "USA";
        String toState = "California";
        String toCity = "Los Angeles";
        String toWard = "Ward Q";
        String toDistrict = "District R";
        String toFacility = "Warehouse B";
        String toLatitude = "20.001";
        String toLongitude = "-20.001";
        String latitude = "34.0522";
        String longitude = "-118.2437";
        String hub = "LA Hub";
        String driverName = "John Doe";
        String driverPhoneCode = "+1";
        String driverPhoneNumber = "1234567890";
        String driverEmail = "johnDoe@example.com";
        String vehicleType = "Truck";
        String vehicleName = "Truck A";
        String vehicleNumber = "ABC123";
        String senderName = "Person A";
        String senderCompany = "Company A";
        String senderDepartment = "Department A";
        String receiverName = "Person B";
        String receiverCompany = "Company B";
        String receiverDepartment = "Department B";
        String eta = "2023-05-10T16:00:00+08:00";
        String notes = "Handle with care.";
        String header = String.join(", ", MilestoneCsv.getCsvHeaders());
        String content = String.join(", ", shipmentTrackingId, name, code, dateTime,
                fromCountry, fromState, fromCity, fromWard, fromDistrict, fromFacility, fromLatitude, fromLongitude,
                toCountry, toState, toCity, toWard, toDistrict, toFacility, toLatitude, toLongitude, latitude, longitude,
                hub, driverName, driverPhoneCode, driverPhoneNumber, driverEmail, vehicleType, vehicleName, vehicleNumber,
                senderName, senderCompany, senderDepartment, receiverName, receiverCompany, receiverDepartment, eta, notes);
        String csvContent = String.join("\n", header, content);
        String orgId = "TEST-ORG-1";
        String userId = "TEST-USER-1";

        InputStream is = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

        when(mockFile.getInputStream()).thenReturn(is);
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(orgId);
        when(userDetailsProvider.getCurrentUserId()).thenReturn(userId);

        List<MilestoneCsv> milestoneCsvList = milestoneAttachmentService.parseToDomain(mockFile);

        assertThat(milestoneCsvList).isNotEmpty();

        MilestoneCsv firstMilestoneCsv = milestoneCsvList.get(0);
        commonAssertLocation(firstMilestoneCsv.getFromFacility(), firstMilestoneCsv.getFromDistrict(),
                firstMilestoneCsv.getFromWard(), firstMilestoneCsv.getFromCity(), firstMilestoneCsv.getFromState(),
                firstMilestoneCsv.getFromCountry(), firstMilestoneCsv.getFromLatitude(), firstMilestoneCsv.getFromLongitude(),
                fromFacility, fromDistrict, fromWard, fromCity, fromState, fromCountry, fromLatitude, fromLongitude);
        commonAssertLocation(firstMilestoneCsv.getToFacility(), firstMilestoneCsv.getToDistrict(),
                firstMilestoneCsv.getToWard(), firstMilestoneCsv.getToCity(), firstMilestoneCsv.getToState(),
                firstMilestoneCsv.getToCountry(), firstMilestoneCsv.getToLatitude(), firstMilestoneCsv.getToLongitude(),
                toFacility, toDistrict, toWard, toCity, toState, toCountry, toLatitude, toLongitude);
        assertThat(firstMilestoneCsv.getLatitude()).isEqualTo(latitude);
        assertThat(firstMilestoneCsv.getLongitude()).isEqualTo(longitude);
        assertThat(firstMilestoneCsv.getHub()).isEqualTo(hub);
    }

    @Test
    void parseToDomain_validFile_shouldMapOtherMilestoneFields() throws Exception {
        String shipmentTrackingId = "QC2023022330607325-0102";
        String name = "Pick Up Scheduled";
        String code = "1400";
        String dateTime = "2023-05-08T16:00:00+08:00";
        String fromCountry = "Philippines";
        String fromState = "Metro Manila";
        String fromCity = "Makati";
        String fromWard = "Ward X";
        String fromDistrict = "District Y";
        String fromFacility = "Warehouse A";
        String fromLatitude = "10.001";
        String fromLongitude = "-10.001";
        String toCountry = "USA";
        String toState = "California";
        String toCity = "Los Angeles";
        String toWard = "Ward Q";
        String toDistrict = "District R";
        String toFacility = "Warehouse B";
        String toLatitude = "20.001";
        String toLongitude = "-20.001";
        String latitude = "34.0522";
        String longitude = "-118.2437";
        String hub = "LA Hub";
        String driverName = "John Doe";
        String driverPhoneCode = "+1";
        String driverPhoneNumber = "1234567890";
        String driverEmail = "johnDoe@example.com";
        String vehicleType = "Truck";
        String vehicleName = "Truck A";
        String vehicleNumber = "ABC123";
        String senderName = "Person A";
        String senderCompany = "Company A";
        String senderDepartment = "Department A";
        String receiverName = "Person B";
        String receiverCompany = "Company B";
        String receiverDepartment = "Department B";
        String eta = "2023-05-10T16:00:00+08:00";
        String notes = "Handle with care.";
        String header = String.join(", ", MilestoneCsv.getCsvHeaders());
        String content = String.join(", ", shipmentTrackingId, name, code, dateTime,
                fromCountry, fromState, fromCity, fromWard, fromDistrict, fromFacility, fromLatitude, fromLongitude,
                toCountry, toState, toCity, toWard, toDistrict, toFacility, toLatitude, toLongitude, latitude, longitude,
                hub, driverName, driverPhoneCode, driverPhoneNumber, driverEmail, vehicleType, vehicleName, vehicleNumber,
                senderName, senderCompany, senderDepartment, receiverName, receiverCompany, receiverDepartment, eta, notes);
        String csvContent = String.join("\n", header, content);
        String orgId = "TEST-ORG-1";
        String userId = "TEST-USER-1";

        InputStream is = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

        when(mockFile.getInputStream()).thenReturn(is);
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(orgId);
        when(userDetailsProvider.getCurrentUserId()).thenReturn(userId);

        List<MilestoneCsv> milestoneCsvList = milestoneAttachmentService.parseToDomain(mockFile);

        assertThat(milestoneCsvList).isNotEmpty();

        MilestoneCsv firstMilestoneCsv = milestoneCsvList.get(0);
        assertThat(firstMilestoneCsv.getDriverName()).isEqualTo(driverName);
        assertThat(firstMilestoneCsv.getDriverPhoneCode()).isEqualTo(driverPhoneCode);
        assertThat(firstMilestoneCsv.getDriverPhoneNumber()).isEqualTo(driverPhoneNumber);
        assertThat(firstMilestoneCsv.getDriverEmail()).isEqualTo(driverEmail);
        assertThat(firstMilestoneCsv.getVehicleType()).isEqualTo(vehicleType);
        assertThat(firstMilestoneCsv.getVehicleName()).isEqualTo(vehicleName);
        assertThat(firstMilestoneCsv.getVehicleNumber()).isEqualTo(vehicleNumber);
        assertThat(firstMilestoneCsv.getSenderName()).isEqualTo(senderName);
        assertThat(firstMilestoneCsv.getSenderCompany()).isEqualTo(senderCompany);
        assertThat(firstMilestoneCsv.getSenderDepartment()).isEqualTo(senderDepartment);
        assertThat(firstMilestoneCsv.getReceiverName()).isEqualTo(receiverName);
        assertThat(firstMilestoneCsv.getReceiverCompany()).isEqualTo(receiverCompany);
        assertThat(firstMilestoneCsv.getReceiverDepartment()).isEqualTo(receiverDepartment);
        assertThat(firstMilestoneCsv.getEta()).isEqualTo(eta);
        assertThat(firstMilestoneCsv.getNotes()).isEqualTo(notes);
    }

    private void commonAssertLocation(String actualFacility, String actualDistrict, String actualWard, String actualCity,
                                      String actualState, String actualCountry, String actualLatitude, String actualLongitude,
                                      String expectedFacility, String expectedDistrict, String expectedWard, String expectedCity,
                                      String expectedState, String expectedCountry, String expectedLatitude, String expectedLongitude) {
        assertThat(actualFacility).isEqualTo(expectedFacility);
        assertThat(actualDistrict).isEqualTo(expectedDistrict);
        assertThat(actualWard).isEqualTo(expectedWard);
        assertThat(actualCity).isEqualTo(expectedCity);
        assertThat(actualState).isEqualTo(expectedState);
        assertThat(actualCountry).isEqualTo(expectedCountry);
        assertThat(actualLatitude).isEqualTo(expectedLatitude);
        assertThat(actualLongitude).isEqualTo(expectedLongitude);
    }

    @Test
    void parseToDomain_exceptionOccurred_shouldThrowException() throws Exception {
        when(mockFile.getInputStream()).thenThrow(new IOException("Test exception"));

        assertThatThrownBy(() -> milestoneAttachmentService.parseToDomain(mockFile))
                .isInstanceOf(QuincusValidationException.class)
                .hasMessageContaining("Test exception");
    }

    @Test
    void getAttachmentType_shouldReturnMilestone() {
        assertThat(milestoneAttachmentService.getAttachmentType()).isEqualTo(AttachmentType.MILESTONE);
    }

    @Test
    void getCsvTemplate_shouldReturnMilestoneCsvContent() {
        assertThat(milestoneAttachmentService.getCsvTemplate()).isEqualTo(MilestoneAttachmentService.CSV_TEMPLATE);
    }
}
