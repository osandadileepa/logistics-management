package com.quincus.shipment.impl.service.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.impl.mapper.PackageJourneySegmentMapper;
import com.quincus.shipment.impl.repository.entity.ArchivedEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.service.ArchivingService;
import com.quincus.shipment.impl.service.PackageJourneySegmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class PackageJourneySegmentDeletionScheduler {
    private final PackageJourneySegmentService packageJourneySegmentService;
    private final ArchivingService archivingService;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedRateString = "${shipment.async.packageJourneySegmentDeletionInterval}")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void archiveAndDeleteMarkedSegments() {
        List<PackageJourneySegmentEntity> packageJourneySegmentEntityList = packageJourneySegmentService.findAllByMarkedForDeletion();
        if (CollectionUtils.isNotEmpty(packageJourneySegmentEntityList)) {
            log.debug("Retrieved `{}` PackageJourneySegment entities marked for deletion", packageJourneySegmentEntityList.size());

            List<ArchivedEntity> archivedEntities = packageJourneySegmentEntityList.stream()
                    .map(PackageJourneySegmentMapper::mapEntityToDomain)
                    .map(this::toArchivedEntity)
                    .toList();

            log.debug("Archiving `{}` PackageJourneySegment entities marked for deletion", packageJourneySegmentEntityList.size());

            archivingService.saveAll(archivedEntities);

            packageJourneySegmentService.deleteAllMarkedForDeletion();

            log.debug("Successfully Archived and deleted `{}` PackageJourneySegment entities", packageJourneySegmentEntityList.size());
        }
    }

    private ArchivedEntity toArchivedEntity(PackageJourneySegment packageJourneySegment) {
        ArchivedEntity archivedEntity = new ArchivedEntity();
        try {
            archivedEntity.setReferenceId(packageJourneySegment.getJourneyId());
            archivedEntity.setOrganizationId(packageJourneySegment.getOrganizationId());
            archivedEntity.setClassName(packageJourneySegment.getClass().getName());
            archivedEntity.setData(objectMapper.writeValueAsString(packageJourneySegment));
        } catch (Exception e) {
            log.error("Error converting PackageJourneySegmentEntity with id `{}` and shipment journey id `{}` to JSON",
                    packageJourneySegment.getSegmentId(), packageJourneySegment.getJourneyId(), e);
        }
        return archivedEntity;
    }

}