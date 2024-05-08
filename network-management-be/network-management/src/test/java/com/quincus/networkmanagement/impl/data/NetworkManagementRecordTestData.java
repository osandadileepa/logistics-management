package com.quincus.networkmanagement.impl.data;

import com.quincus.networkmanagement.impl.attachment.connection.ConnectionRecord;
import com.quincus.networkmanagement.impl.attachment.node.NodeRecord;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class NetworkManagementRecordTestData {

    public static ConnectionRecord dummyGroundConnectionRecord() {
        ConnectionRecord record = new ConnectionRecord();
        record.setConnectionCode("CONNECTION-A");
        record.setVendorName("VENDOR-NAME");
        record.setTags("fragile, handle with care");
        record.setActive("TRUE");
        record.setTransportType("GROUND");
        record.setVehicleType("truck");
        record.setDepartureNodeCode("HUB-1");
        record.setArrivalNodeCode("HUB-2");
        record.setCost("2000");
        record.setCurrencyCode("SGD");
        record.setMaxLength("9999");
        record.setMinLength("99");
        record.setMaxWidth("8888");
        record.setMinWidth("88");
        record.setMaxHeight("7777");
        record.setMinHeight("77");
        record.setMaxWeight("6666");
        record.setMinWeight("66");
        record.setDimensionUnit("METERS");
        record.setWeightUnit("KILOGRAMS");
        record.setMaxSingleSide("5555");
        record.setMinSingleSide("55");
        record.setMaxLinearDim("4444");
        record.setMinLinearDim("44");
        record.setMaxVolume("3333");
        record.setMinVolume("33");
        record.setVolumeUnit("CUBIC_METERS");
        record.setSchedules("RRULE:FREQ=WEEKLY;INTERVAL=1;WKST=MO;BYDAY=MO,WE,FR;BYHOUR=10;BYMINUTE=30");
        record.setDuration("60");
        record.setMaxCapacityCount("3333");
        record.setMaxCapacityWeight("3333");
        record.setMaxCapacityVolume("3333");
        return record;
    }

    public static ConnectionRecord dummyAirConnectionRecord() {
        ConnectionRecord record = new ConnectionRecord();
        record.setConnectionCode("CONNECTION-B");
        record.setVendorName("VENDOR-NAME");
        record.setTags("fragile, handle with care");
        record.setActive("TRUE");
        record.setTransportType("AIR");
        record.setDepartureNodeCode("AIRPORT-1");
        record.setArrivalNodeCode("AIRPORT-2");
        record.setCost("2000");
        record.setCurrencyCode("SGD");
        record.setMaxLength("9999");
        record.setMinLength("99");
        record.setMaxWidth("8888");
        record.setMinWidth("88");
        record.setMaxHeight("7777");
        record.setMinHeight("77");
        record.setMaxWeight("6666");
        record.setMinWeight("66");
        record.setDimensionUnit("METERS");
        record.setWeightUnit("KILOGRAMS");
        record.setMaxSingleSide("5555");
        record.setMinSingleSide("55");
        record.setMaxLinearDim("4444");
        record.setMinLinearDim("44");
        record.setMaxVolume("3333");
        record.setMinVolume("33");
        record.setVolumeUnit("CUBIC_METERS");
        record.setAirLockoutDuration("10");
        record.setAirRecoveryDuration("15");
        record.setSchedules("RRULE:FREQ=WEEKLY;INTERVAL=1;WKST=MO;BYDAY=MO,TU,WE,TH,FR;BYHOUR=7;BYMINUTE=30 | RRULE:FREQ=WEEKLY;INTERVAL=1;WKST=MO;BYDAY=SA,SU;BYHOUR=10;BYMINUTE=15");
        record.setDuration("240");
        record.setMaxCapacityCount("3333");
        record.setMaxCapacityWeight("3333");
        record.setMaxCapacityVolume("3333");
        return record;
    }

    public static NodeRecord dummyNodeRecord() {
        NodeRecord record = new NodeRecord();
        record.setNodeCode("NODE-A");
        record.setNodeType("CARGO");
        record.setDescription("Sample description");
        record.setActive("TRUE");
        record.setTags("fragile, refrigerated");
        record.setAddressLine1("KIV");
        record.setAddressLine2("KIV");
        record.setAddressLine3("KIV");
        record.setFacilityName("FACILITY-NAME");
        record.setVendorName("VENDOR-NAME");
        record.setMonStartTime("10:00:00 AM");
        record.setMonEndTime("5:00:00 PM");
        record.setMonProcessingTime("30");
        record.setProcessingTimeUnit("MINUTES");
        record.setMaxLength("9999");
        record.setMinLength("99");
        record.setMaxWidth("8888");
        record.setMinWidth("88");
        record.setMaxHeight("7777");
        record.setMinHeight("77");
        record.setMaxWeight("6666");
        record.setMinWeight("66");
        record.setDimensionUnit("METERS");
        record.setWeightUnit("KILOGRAMS");
        record.setVolumeUnit("CUBIC_METERS");
        record.setMaxCapacityCount("999999");
        record.setMaxCapacityWeight("999999");
        record.setMaxCapacityVolume("999999");
        return record;
    }

    public static MultipartFile dummyConnectionsExcelTemplate() {
        return dummyTemplate(
                "valid-connections-template.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );
    }

    public static MultipartFile dummyConnectionsCsvTemplate() {
        return dummyTemplate(
                "valid-connections-template.csv",
                "text/csv"
        );
    }

    public static MultipartFile dummyNodesExcelTemplate() {
        return dummyTemplate(
                "valid-nodes-template.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );
    }

    public static MultipartFile dummyNodesCsvTemplate() {
        return dummyTemplate(
                "valid-nodes-template.csv",
                "text/csv"
        );
    }

    public static MultipartFile dummyTemplate(String fileName, String contentType) {
        try {
            ClassPathResource path = new ClassPathResource("template/" + fileName);
            FileInputStream input = new FileInputStream(path.getFile());
            return new MockMultipartFile(
                    fileName,
                    fileName,
                    contentType,
                    IOUtils.toByteArray(input)
            );
        } catch (Exception e) {
            return null;
        }
    }

}
