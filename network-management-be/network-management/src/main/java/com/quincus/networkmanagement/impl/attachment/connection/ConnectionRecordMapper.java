package com.quincus.networkmanagement.impl.attachment.connection;

import com.quincus.networkmanagement.api.domain.Connection;
import com.quincus.networkmanagement.api.domain.VehicleType;
import com.quincus.networkmanagement.impl.attachment.NetworkManagementRecordMapper;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface ConnectionRecordMapper extends NetworkManagementRecordMapper {

    String DEFAULT_MAX_VALUE = "999999";
    String DEFAULT_MIN_VALUE = "0";

    @Mapping(target = "active", defaultValue = "true")
    @Mapping(target = "departureNode.nodeCode", source = "departureNodeCode")
    @Mapping(target = "schedules", source = "schedules", qualifiedByName = "toListOfSchedules")
    @Mapping(target = "duration")
    @Mapping(target = "arrivalNode.nodeCode", source = "arrivalNodeCode")
    @Mapping(target = "vendor.name", source = "vendorName")
    @Mapping(target = "cost", defaultValue = "0")
    @Mapping(target = "currency.code", source = "currencyCode", defaultValue = "USD")
    @Mapping(target = "shipmentProfile.maxLength", source = "maxLength", defaultValue = DEFAULT_MAX_VALUE)
    @Mapping(target = "shipmentProfile.minLength", source = "minLength", defaultValue = DEFAULT_MIN_VALUE)
    @Mapping(target = "shipmentProfile.maxWidth", source = "maxWidth", defaultValue = DEFAULT_MAX_VALUE)
    @Mapping(target = "shipmentProfile.minWidth", source = "minWidth", defaultValue = DEFAULT_MIN_VALUE)
    @Mapping(target = "shipmentProfile.maxHeight", source = "maxHeight", defaultValue = DEFAULT_MAX_VALUE)
    @Mapping(target = "shipmentProfile.minHeight", source = "minHeight", defaultValue = DEFAULT_MIN_VALUE)
    @Mapping(target = "shipmentProfile.maxWeight", source = "maxWeight", defaultValue = DEFAULT_MAX_VALUE)
    @Mapping(target = "shipmentProfile.minWeight", source = "minWeight", defaultValue = DEFAULT_MIN_VALUE)
    @Mapping(target = "shipmentProfile.maxSingleSide", source = "maxSingleSide", defaultValue = DEFAULT_MAX_VALUE)
    @Mapping(target = "shipmentProfile.minSingleSide", source = "minSingleSide", defaultValue = DEFAULT_MIN_VALUE)
    @Mapping(target = "shipmentProfile.maxLinearDim", source = "maxLinearDim", defaultValue = DEFAULT_MAX_VALUE)
    @Mapping(target = "shipmentProfile.minLinearDim", source = "minLinearDim", defaultValue = DEFAULT_MIN_VALUE)
    @Mapping(target = "shipmentProfile.maxVolume", source = "maxVolume", defaultValue = DEFAULT_MAX_VALUE)
    @Mapping(target = "shipmentProfile.minVolume", source = "minVolume", defaultValue = DEFAULT_MIN_VALUE)
    @Mapping(target = "measurementUnits.volumeUnit", source = "volumeUnit", defaultValue = "CUBIC_METERS")
    @Mapping(target = "measurementUnits.dimensionUnit", source = "dimensionUnit", defaultValue = "METERS")
    @Mapping(target = "measurementUnits.weightUnit", source = "weightUnit", defaultValue = "KILOGRAMS")
    @Mapping(target = "airLockoutDuration", defaultValue = "0")
    @Mapping(target = "airRecoveryDuration", defaultValue = "0")
    @Mapping(target = "capacityProfile.maxShipmentCount", source = "maxCapacityCount", defaultValue = DEFAULT_MAX_VALUE)
    @Mapping(target = "capacityProfile.maxVolume", source = "maxCapacityVolume", defaultValue = DEFAULT_MAX_VALUE)
    @Mapping(target = "capacityProfile.maxWeight", source = "maxCapacityWeight", defaultValue = DEFAULT_MAX_VALUE)
    Connection toDomain(ConnectionRecord connectionRecord);

    @Mapping(target = "vehicleType", source = "vehicleType.name")
    @Mapping(target = "departureNodeCode", source = "departureNode.nodeCode")
    @Mapping(target = "arrivalNodeCode", source = "arrivalNode.nodeCode")
    @Mapping(target = "schedules", source = "schedules", qualifiedByName = "toPipeSeparatedString")
    @Mapping(target = "vendorName", source = "vendor.name")
    @Mapping(target = "currencyCode", source = "currency.code")
    @Mapping(target = "maxLength", source = "shipmentProfile.maxLength")
    @Mapping(target = "minLength", source = "shipmentProfile.minLength")
    @Mapping(target = "maxWidth", source = "shipmentProfile.maxWidth")
    @Mapping(target = "minWidth", source = "shipmentProfile.minWidth")
    @Mapping(target = "maxHeight", source = "shipmentProfile.maxHeight")
    @Mapping(target = "minHeight", source = "shipmentProfile.minHeight")
    @Mapping(target = "maxWeight", source = "shipmentProfile.maxWeight")
    @Mapping(target = "minWeight", source = "shipmentProfile.minWeight")
    @Mapping(target = "maxSingleSide", source = "shipmentProfile.maxSingleSide")
    @Mapping(target = "minSingleSide", source = "shipmentProfile.minSingleSide")
    @Mapping(target = "maxLinearDim", source = "shipmentProfile.maxLinearDim")
    @Mapping(target = "minLinearDim", source = "shipmentProfile.minLinearDim")
    @Mapping(target = "maxVolume", source = "shipmentProfile.maxVolume")
    @Mapping(target = "minVolume", source = "shipmentProfile.minVolume")
    @Mapping(target = "volumeUnit", source = "measurementUnits.volumeUnit")
    @Mapping(target = "dimensionUnit", source = "measurementUnits.dimensionUnit")
    @Mapping(target = "weightUnit", source = "measurementUnits.weightUnit")
    @Mapping(target = "maxCapacityCount", source = "capacityProfile.maxShipmentCount")
    @Mapping(target = "maxCapacityVolume", source = "capacityProfile.maxVolume")
    @Mapping(target = "maxCapacityWeight", source = "capacityProfile.maxWeight")
    ConnectionRecord toRecord(Connection connection);

    default VehicleType toVehicleType(String vehicleTypeName) {
        if (StringUtils.isBlank(vehicleTypeName))
            return null;
        VehicleType vehicleType = new VehicleType();
        vehicleType.setName(vehicleTypeName);
        return vehicleType;
    }

    @Named("toListOfSchedules")
    default List<String> toListOfSchedules(String value) {
        if (StringUtils.isBlank(value)) {
            return Collections.emptyList();
        }
        String[] values = value.split("\\s*\\|\\s*");
        return Arrays.asList(values);
    }

    @Named("toPipeSeparatedString")
    default String toPipeSeparatedString(List<String> stringList) {
        return String.join(" | ", stringList);
    }

}
