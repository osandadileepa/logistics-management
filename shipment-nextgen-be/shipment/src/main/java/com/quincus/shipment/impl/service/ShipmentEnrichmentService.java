package com.quincus.shipment.impl.service;

import com.quincus.shipment.impl.repository.entity.AlertEntity;
import com.quincus.shipment.impl.repository.entity.InstructionEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

@Service
@Slf4j
@AllArgsConstructor
@Transactional(readOnly = true)
public class ShipmentEnrichmentService {

    private final AlertService alertService;
    private final InstructionFetchService instructionFetchService;

    public void enrichShipmentPackageJourneySegmentsWithInstructions(List<PackageJourneySegmentEntity> packageJourneySegmentEntities) {
        List<String> segmentIds = packageJourneySegmentEntities
                .stream().map(PackageJourneySegmentEntity::getId).toList();
        Map<String, List<InstructionEntity>> instructionsBySegmentId = groupInstructionsBySegmentId(instructionFetchService.findBySegmentIds(segmentIds));
        packageJourneySegmentEntities.forEach(
                segment -> segment.setInstructions(instructionsBySegmentId.get(segment.getId()))
        );
    }

    public void enrichShipmentJourneyAndSegmentWithAlert(ShipmentJourneyEntity shipmentJourneyEntity) {
        String shipmentJourneyId = shipmentJourneyEntity.getId();
        List<String> segmentIds = shipmentJourneyEntity.getPackageJourneySegments()
                .stream().map(PackageJourneySegmentEntity::getId).toList();
        Map<String, List<AlertEntity>> alertEntityById = groupAlertsByJourneyAndSegmentId(alertService
                .findByJourneyIdsAndSegmentIds(List.of(shipmentJourneyId), segmentIds));
        shipmentJourneyEntity.setAlerts(alertEntityById.get(shipmentJourneyEntity.getId()));
        shipmentJourneyEntity.getPackageJourneySegments()
                .forEach(segment -> segment.setAlerts(alertEntityById.get(segment.getId())));
    }

    private Map<String, List<AlertEntity>> groupAlertsByJourneyAndSegmentId(List<AlertEntity> alertEntities) {
        Map<String, List<AlertEntity>> alertEntityById = new HashMap<>();
        alertEntities.forEach(alertEntity -> {
            if (StringUtils.isNotBlank(alertEntity.getPackageJourneySegmentId())) {
                alertEntityById.computeIfAbsent(alertEntity.getPackageJourneySegmentId(), k -> new ArrayList<>()).add(alertEntity);
            } else if (StringUtils.isNotBlank(alertEntity.getShipmentJourneyId())) {
                alertEntityById.computeIfAbsent(alertEntity.getShipmentJourneyId(), k -> new ArrayList<>()).add(alertEntity);
            }
        });
        return alertEntityById;
    }

    private Map<String, List<InstructionEntity>> groupInstructionsBySegmentId(List<InstructionEntity> instructionEntities) {
        return instructionEntities.stream()
                .collect(groupingBy(InstructionEntity::getPackageJourneySegmentId));
    }
}
