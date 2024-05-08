package com.quincus.networkmanagement.impl.attachment.node;

import com.quincus.networkmanagement.api.domain.Node;
import com.quincus.networkmanagement.api.domain.Partner;
import com.quincus.networkmanagement.impl.attachment.NetworkManagementRecordMapper;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface NodeRecordMapper extends NetworkManagementRecordMapper {
    String DEFAULT_MAX_VALUE = "999999";
    String DEFAULT_MIN_VALUE = "0";
    String DEFAULT_PROCESSING_TIME = "0";

    @Mapping(target = "active", defaultValue = "true")
    @Mapping(target = "facility.name", source = "facilityName")
    @Mapping(target = "vendor", source = "vendorName", qualifiedByName = "toPartner")
    @Mapping(target = "operatingHours.monStartTime", source = "monStartTime")
    @Mapping(target = "operatingHours.monEndTime", source = "monEndTime")
    @Mapping(target = "operatingHours.monProcessingTime", source = "monProcessingTime", defaultValue = DEFAULT_PROCESSING_TIME)
    @Mapping(target = "operatingHours.tueStartTime", source = "tueStartTime")
    @Mapping(target = "operatingHours.tueEndTime", source = "tueEndTime")
    @Mapping(target = "operatingHours.tueProcessingTime", source = "tueProcessingTime", defaultValue = DEFAULT_PROCESSING_TIME)
    @Mapping(target = "operatingHours.wedStartTime", source = "wedStartTime")
    @Mapping(target = "operatingHours.wedEndTime", source = "wedEndTime")
    @Mapping(target = "operatingHours.wedProcessingTime", source = "wedProcessingTime", defaultValue = DEFAULT_PROCESSING_TIME)
    @Mapping(target = "operatingHours.thuStartTime", source = "thuStartTime")
    @Mapping(target = "operatingHours.thuEndTime", source = "thuEndTime")
    @Mapping(target = "operatingHours.thuProcessingTime", source = "thuProcessingTime", defaultValue = DEFAULT_PROCESSING_TIME)
    @Mapping(target = "operatingHours.friStartTime", source = "friStartTime")
    @Mapping(target = "operatingHours.friEndTime", source = "friEndTime")
    @Mapping(target = "operatingHours.friProcessingTime", source = "friProcessingTime", defaultValue = DEFAULT_PROCESSING_TIME)
    @Mapping(target = "operatingHours.satStartTime", source = "satStartTime")
    @Mapping(target = "operatingHours.satEndTime", source = "satEndTime")
    @Mapping(target = "operatingHours.satProcessingTime", source = "satProcessingTime", defaultValue = DEFAULT_PROCESSING_TIME)
    @Mapping(target = "operatingHours.sunStartTime", source = "sunStartTime")
    @Mapping(target = "operatingHours.sunEndTime", source = "sunEndTime")
    @Mapping(target = "operatingHours.sunProcessingTime", source = "sunProcessingTime", defaultValue = DEFAULT_PROCESSING_TIME)
    @Mapping(target = "operatingHours.processingTimeUnit", source = "processingTimeUnit", defaultValue = "MINUTES")
    @Mapping(target = "shipmentProfile.maxLength", source = "maxLength", defaultValue = DEFAULT_MAX_VALUE)
    @Mapping(target = "shipmentProfile.minLength", source = "minLength", defaultValue = DEFAULT_MIN_VALUE)
    @Mapping(target = "shipmentProfile.maxWidth", source = "maxWidth", defaultValue = DEFAULT_MAX_VALUE)
    @Mapping(target = "shipmentProfile.minWidth", source = "minWidth", defaultValue = DEFAULT_MIN_VALUE)
    @Mapping(target = "shipmentProfile.maxHeight", source = "maxHeight", defaultValue = DEFAULT_MAX_VALUE)
    @Mapping(target = "shipmentProfile.minHeight", source = "minHeight", defaultValue = DEFAULT_MIN_VALUE)
    @Mapping(target = "shipmentProfile.maxWeight", source = "maxWeight", defaultValue = DEFAULT_MAX_VALUE)
    @Mapping(target = "shipmentProfile.minWeight", source = "minWeight", defaultValue = DEFAULT_MIN_VALUE)
    @Mapping(target = "measurementUnits.dimensionUnit", source = "dimensionUnit", defaultValue = "METERS")
    @Mapping(target = "measurementUnits.weightUnit", source = "weightUnit", defaultValue = "KILOGRAMS")
    @Mapping(target = "measurementUnits.volumeUnit", source = "volumeUnit", defaultValue = "CUBIC_METERS")
    @Mapping(target = "capacityProfile.maxShipmentCount", source = "maxCapacityCount", defaultValue = DEFAULT_MAX_VALUE)
    @Mapping(target = "capacityProfile.maxVolume", source = "maxCapacityVolume", defaultValue = DEFAULT_MAX_VALUE)
    @Mapping(target = "capacityProfile.maxWeight", source = "maxCapacityWeight", defaultValue = DEFAULT_MAX_VALUE)
    Node toDomain(NodeRecord nodeRecord);

    @Mapping(target = "facilityName", source = "facility.name")
    @Mapping(target = "vendorName", source = "vendor.name")
    @Mapping(target = "monStartTime", source = "operatingHours.monStartTime")
    @Mapping(target = "monEndTime", source = "operatingHours.monEndTime")
    @Mapping(target = "monProcessingTime", source = "operatingHours.monProcessingTime")
    @Mapping(target = "tueStartTime", source = "operatingHours.tueStartTime")
    @Mapping(target = "tueEndTime", source = "operatingHours.tueEndTime")
    @Mapping(target = "tueProcessingTime", source = "operatingHours.tueProcessingTime")
    @Mapping(target = "wedStartTime", source = "operatingHours.wedStartTime")
    @Mapping(target = "wedEndTime", source = "operatingHours.wedEndTime")
    @Mapping(target = "wedProcessingTime", source = "operatingHours.wedProcessingTime")
    @Mapping(target = "thuStartTime", source = "operatingHours.thuStartTime")
    @Mapping(target = "thuEndTime", source = "operatingHours.thuEndTime")
    @Mapping(target = "thuProcessingTime", source = "operatingHours.thuProcessingTime")
    @Mapping(target = "friStartTime", source = "operatingHours.friStartTime")
    @Mapping(target = "friEndTime", source = "operatingHours.friEndTime")
    @Mapping(target = "friProcessingTime", source = "operatingHours.friProcessingTime")
    @Mapping(target = "satStartTime", source = "operatingHours.satStartTime")
    @Mapping(target = "satEndTime", source = "operatingHours.satEndTime")
    @Mapping(target = "satProcessingTime", source = "operatingHours.satProcessingTime")
    @Mapping(target = "sunStartTime", source = "operatingHours.sunStartTime")
    @Mapping(target = "sunEndTime", source = "operatingHours.sunEndTime")
    @Mapping(target = "sunProcessingTime", source = "operatingHours.sunProcessingTime")
    @Mapping(target = "processingTimeUnit", source = "operatingHours.processingTimeUnit")
    @Mapping(target = "maxLength", source = "shipmentProfile.maxLength")
    @Mapping(target = "minLength", source = "shipmentProfile.minLength")
    @Mapping(target = "maxWidth", source = "shipmentProfile.maxWidth")
    @Mapping(target = "minWidth", source = "shipmentProfile.minWidth")
    @Mapping(target = "maxHeight", source = "shipmentProfile.maxHeight")
    @Mapping(target = "minHeight", source = "shipmentProfile.minHeight")
    @Mapping(target = "maxWeight", source = "shipmentProfile.maxWeight")
    @Mapping(target = "minWeight", source = "shipmentProfile.minWeight")
    @Mapping(target = "dimensionUnit", source = "measurementUnits.dimensionUnit")
    @Mapping(target = "weightUnit", source = "measurementUnits.weightUnit")
    @Mapping(target = "volumeUnit", source = "measurementUnits.volumeUnit")
    @Mapping(target = "maxCapacityCount", source = "capacityProfile.maxShipmentCount")
    @Mapping(target = "maxCapacityVolume", source = "capacityProfile.maxVolume")
    @Mapping(target = "maxCapacityWeight", source = "capacityProfile.maxWeight")
    NodeRecord toRecord(Node node);

    @Named("toPartner")
    default Partner toPartner(String partnerName) {
        if (StringUtils.isBlank(partnerName))
            return null;
        Partner partner = new Partner();
        partner.setName(partnerName);
        return partner;
    }
}
