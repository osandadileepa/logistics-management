package com.quincus.shipment.impl.service.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.impl.repository.entity.ArchivedEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.service.ArchivingService;
import com.quincus.shipment.impl.service.PackageJourneySegmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackageJourneySegmentDeletionSchedulerTest {

    @Mock
    private PackageJourneySegmentService packageJourneySegmentService;

    @Mock
    private ArchivingService archivingService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PackageJourneySegmentDeletionScheduler scheduler;

    @Test
    void testArchiveAndDeleteMarkedSegments() throws Exception {
        PackageJourneySegmentEntity entity = mock(PackageJourneySegmentEntity.class);
        when(packageJourneySegmentService.findAllByMarkedForDeletion()).thenReturn(List.of(entity));

        ArchivedEntity archivedEntity = new ArchivedEntity();
        archivedEntity.setReferenceId("journeyId");
        archivedEntity.setData("data");
        when(objectMapper.writeValueAsString(any())).thenReturn("data");

        scheduler.archiveAndDeleteMarkedSegments();

        verify(packageJourneySegmentService, times(1)).findAllByMarkedForDeletion();
        verify(archivingService, times(1)).saveAll(Collections.singletonList(archivedEntity));
        verify(packageJourneySegmentService, times(1)).deleteAllMarkedForDeletion();
    }
}
