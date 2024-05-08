package com.quincus.shipment.impl.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.ext.DateTimeUtil;
import com.quincus.shipment.api.constant.MilestoneCode;
import com.quincus.shipment.api.constant.MilestoneSource;
import com.quincus.shipment.api.domain.Coordinate;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.MilestoneAdditionalInfo;
import com.quincus.shipment.api.dto.MilestoneUpdateRequest;
import com.quincus.shipment.api.dto.csv.MilestoneCsv;
import com.quincus.shipment.impl.repository.entity.MilestoneEntity;
import com.quincus.shipment.impl.repository.entity.MilestoneEntity_;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.function.Supplier;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class MilestoneMapper {
    private ObjectMapper objectMapper;
    private EntityManager entityManager;

    @Mapping(target = "milestoneTime", qualifiedByName = "deserializeOffsetDateTime")
    @Mapping(target = "organizationId", source = "organizationId")
    @Mapping(target = "shipmentId", source = "shipment", qualifiedByName = "serializeToShipmentId")
    @Mapping(target = "segmentId", source = "segment", qualifiedByName = "serializeToSegmentId")
    @Mapping(target = "eta", qualifiedByName = "deserializeOffsetDateTime")
    @Mapping(target = "proofOfDeliveryTime", qualifiedByName = "deserializeOffsetDateTime")
    public abstract Milestone toDomain(MilestoneEntity milestoneEntity);

    @Mapping(target = "milestoneTime", qualifiedByName = "deserializeOffsetDateTime")
    @Mapping(target = "milestoneCode", qualifiedByName = "deserializeMilestoneCode")
    @Mapping(target = "milestoneCoordinates",
            expression = "java(extractMilestoneCoordinates(milestoneCsv::getLatitude, milestoneCsv::getLongitude))")
    @Mapping(target = "fromCoordinates",
            expression = "java(extractMilestoneCoordinates(milestoneCsv::getFromLatitude, milestoneCsv::getFromLongitude))")
    @Mapping(target = "toCoordinates",
            expression = "java(extractMilestoneCoordinates(milestoneCsv::getToLatitude, milestoneCsv::getToLongitude))")
    @Mapping(target = "eta", qualifiedByName = "deserializeOffsetDateTime")
    @Mapping(target = "additionalInfo", expression = "java(toRemarks(milestoneCsv::getNotes))")
    public abstract Milestone toDomain(MilestoneCsv milestoneCsv);

    @Mapping(target = "milestoneTime", qualifiedByName = "serializeOffsetDateTime")
    @Mapping(target = "eta", qualifiedByName = "serializeOffsetDateTime")
    @Mapping(target = "proofOfDeliveryTime", qualifiedByName = "serializeOffsetDateTime")
    @Mapping(target = "organizationId", source = "organizationId")
    @Mapping(target = "shipment", source = "shipmentId", qualifiedByName = "mapToShipmentEntity")
    @Mapping(target = "segment", source = "segmentId", qualifiedByName = "mapToSegmentEntity")
    public abstract MilestoneEntity toEntity(Milestone milestone);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "segment", ignore = true)
    @Mapping(target = "shipment", ignore = true)
    @Mapping(target = "milestoneCode", ignore = true)
    @Mapping(target = "milestoneTime", ignore = true)
    @Mapping(target = "eta", qualifiedByName = "serializeOffsetDateTime")
    @Mapping(target = "proofOfDeliveryTime", qualifiedByName = "serializeOffsetDateTime")
    public abstract MilestoneEntity toEntity(@MappingTarget MilestoneEntity entity, Milestone milestone);

    @Mapping(target = "milestoneCode", source = "milestone", qualifiedByName = "deserializeMilestoneCode")
    @Mapping(target = "externalSegmentId", source = "segmentId", qualifiedByName = "mapToSegmentId")
    @Mapping(target = "milestoneTime", qualifiedByName = "deserializeOffsetDateTime")
    @Mapping(target = "proofOfDeliveryTime", qualifiedByName = "deserializeOffsetDateTime")
    @Mapping(target = "receiverName", source = "recipientName")
    public abstract Milestone toMilestone(MilestoneUpdateRequest milestoneUpdateRequest);

    @Named("deserializeMilestoneCode")
    MilestoneCode toMilestoneCode(String code) {
        return MilestoneCode.fromValue(code);
    }

    @Named("deserializeSourceCode")
    MilestoneSource toSource(String source) {
        return MilestoneSource.fromValue(source);
    }

    Coordinate extractMilestoneCoordinates(Supplier<String> latitudeSupplier, Supplier<String> longitudeSupplier) {
        String latitude = latitudeSupplier.get();
        String longitude = longitudeSupplier.get();
        if (StringUtils.isBlank(latitude) && StringUtils.isBlank(longitude)) {
            return null;
        }

        Coordinate coordinate = new Coordinate();
        if (StringUtils.isNotBlank(latitude)) {
            coordinate.setLat(new BigDecimal(latitude));
        }
        if (StringUtils.isNotBlank(longitude)) {
            coordinate.setLon(new BigDecimal(longitude));
        }
        return coordinate;
    }

    MilestoneAdditionalInfo toRemarks(Supplier<String> notesSupplier) {
        String notes = notesSupplier.get();
        if (notes == null) {
            return null;
        }
        MilestoneAdditionalInfo additionalInfo = new MilestoneAdditionalInfo();
        additionalInfo.setRemarks(notes);
        return additionalInfo;
    }

    @Named("serializeToShipmentId")
    String toShipmentId(ShipmentEntity shipment) {
        if (shipment == null) {
            return null;
        }
        return shipment.getId();
    }

    @Named("mapToShipmentEntity")
    ShipmentEntity toShipmentEntity(String shipmentId) {
        if (shipmentId == null) {
            return null;
        }
        return entityManager.getReference(ShipmentEntity.class, shipmentId);
    }

    @Named("serializeToSegmentId")
    String toSegmentId(PackageJourneySegmentEntity segment) {
        if (segment == null) {
            return null;
        }
        return segment.getId();
    }

    @Named("mapToSegmentId")
    String mapToSegmentId(Object segmentId) {
        if (segmentId == null) {
            return null;
        }
        return segmentId.toString();
    }

    @Named("mapToSegmentEntity")
    PackageJourneySegmentEntity toSegmentEntity(String segmentId) {
        if (segmentId == null) {
            return null;
        }
        return entityManager.getReference(PackageJourneySegmentEntity.class, segmentId);
    }

    @Named("serializeOffsetDateTime")
    String offsetDateTimeToOffsetDateTimeString(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }
        return offsetDateTime.format(DateTimeUtil.ISO_FORMATTER);
    }

    @Named("deserializeOffsetDateTime")
    OffsetDateTime offsetDateTimeStringToOffsetDateTime(String offsetDateTimeText) {
        if (StringUtils.isBlank(offsetDateTimeText)) {
            return null;
        }
        return DateTimeUtil.toFormattedOffsetDateTime(offsetDateTimeText);
    }

    public Milestone convertMessageToDomain(String message) throws JsonProcessingException {
        Milestone milestone = objectMapper.readValue(message, Milestone.class);
        milestone.setData(objectMapper.readValue(message, JsonNode.class));
        return milestone;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public static MilestoneEntity mapTupleToEntity(Tuple milestoneTuple) {
        MilestoneEntity entity = new MilestoneEntity();
        entity.setMilestoneTime(milestoneTuple.get(MilestoneEntity_.MILESTONE_TIME, String.class));
        entity.setMilestoneCode(milestoneTuple.get(MilestoneEntity_.MILESTONE_CODE, MilestoneCode.class));
        return entity;
    }

}
