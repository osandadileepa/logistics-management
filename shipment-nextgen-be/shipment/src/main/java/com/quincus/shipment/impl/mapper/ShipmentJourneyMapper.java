package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.domain.Alert;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.impl.repository.entity.AlertEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@NoArgsConstructor(access = AccessLevel.NONE)
public class ShipmentJourneyMapper {

    private static final AlertMapper ALERT_MAPPER = Mappers.getMapper(AlertMapper.class);

    public static void setJourneyDomainAlerts(ShipmentJourney shipmentJourneyDomain, ShipmentJourneyEntity shipmentJourneyEntity) {
        List<Alert> alerts = new ArrayList<>();
        if (!CollectionUtils.isEmpty(shipmentJourneyEntity.getAlerts())) {
            alerts = shipmentJourneyEntity.getAlerts().stream()
                    .map(ALERT_MAPPER::toDomain)
                    .sorted()
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        shipmentJourneyDomain.setAlerts(alerts);
    }

    public static void setJourneyEntityAlerts(ShipmentJourneyEntity shipmentJourneyEntity, ShipmentJourney shipmentJourneyDomain) {
        if (shipmentJourneyDomain.getAlerts() != null) {
            List<AlertEntity> alertEntities = new ArrayList<>();
            shipmentJourneyDomain.getAlerts().stream()
                    .map(ALERT_MAPPER::toEntity)
                    .forEach(alertEntities::add);

            List<AlertEntity> uniqueAlerts = new ArrayList<>();
            uniqueAlerts.addAll(Optional.ofNullable(shipmentJourneyEntity.getAlerts()).orElse(Collections.emptyList()));
            uniqueAlerts.addAll(alertEntities.stream().distinct().toList());
            shipmentJourneyEntity.setAlerts(uniqueAlerts);
        }
    }

    public static ShipmentJourneyEntity mapDomainToEntity(ShipmentJourney shipmentJourneyDomain) {
        return mapDomainToEntity(shipmentJourneyDomain, true);
    }

    public static ShipmentJourneyEntity mapDomainToEntity(ShipmentJourney shipmentJourneyDomain, boolean includeSegments) {
        if (shipmentJourneyDomain == null) {
            return null;
        }
        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        shipmentJourneyEntity.setId(shipmentJourneyDomain.getJourneyId());
        shipmentJourneyEntity.setStatus(shipmentJourneyDomain.getStatus());
        setJourneyEntityAlerts(shipmentJourneyEntity, shipmentJourneyDomain);

        if (includeSegments) {
            shipmentJourneyEntity.addAllPackageJourneySegments(PackageJourneySegmentMapper.mapDomainListToEntityListPackageJourneySegment(shipmentJourneyDomain.getPackageJourneySegments()));
        }
        return shipmentJourneyEntity;
    }

    public static ShipmentJourney mapEntityToDomain(ShipmentJourneyEntity shipmentJourneyEntity) {
        ShipmentJourney shipmentJourneyDomain = mapCommonFieldsToDomain(shipmentJourneyEntity);
        if (shipmentJourneyDomain == null) {
            return null;
        }
        if (!CollectionUtils.isEmpty(shipmentJourneyEntity.getPackageJourneySegments())) {
            shipmentJourneyDomain.setPackageJourneySegments(
                    PackageJourneySegmentMapper.mapEntityListToDomainListPackageJourneySegment(shipmentJourneyEntity.getPackageJourneySegments()));
        }
        return shipmentJourneyDomain;
    }

    private static ShipmentJourney mapCommonFieldsToDomain(ShipmentJourneyEntity shipmentJourneyEntity) {
        if (shipmentJourneyEntity == null) {
            return null;
        }
        ShipmentJourney shipmentJourneyDomain = new ShipmentJourney();
        shipmentJourneyDomain.setJourneyId(shipmentJourneyEntity.getId());
        shipmentJourneyDomain.setStatus(shipmentJourneyEntity.getStatus());
        setJourneyDomainAlerts(shipmentJourneyDomain, shipmentJourneyEntity);
        return shipmentJourneyDomain;
    }

    public static ShipmentJourney mapEntityToDomainForListing(ShipmentJourneyEntity shipmentJourneyEntity) {
        if (isNull(shipmentJourneyEntity)) {
            return null;
        }
        ShipmentJourney shipmentJourneyDomain = mapCommonFieldsToDomain(shipmentJourneyEntity);
        if (!CollectionUtils.isEmpty(shipmentJourneyEntity.getPackageJourneySegments())) {
            shipmentJourneyDomain.setPackageJourneySegments(PackageJourneySegmentMapper.mapSegmentListing(shipmentJourneyEntity.getPackageJourneySegments()));
        }

        return shipmentJourneyDomain;
    }
}
