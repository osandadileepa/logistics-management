package com.quincus.shipment.impl.service;

import com.quincus.shipment.impl.repository.ArchiveRepository;
import com.quincus.shipment.impl.repository.entity.ArchivedEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArchivingServiceTest {

    @Mock
    private ArchiveRepository archiveRepository;

    @InjectMocks
    private ArchivingService archivingService;

    @Test
    void testSaveAll() {
        ArchivedEntity archivedEntity = new ArchivedEntity();
        List<ArchivedEntity> archivedEntities = Collections.singletonList(archivedEntity);

        archivingService.saveAll(archivedEntities);

        verify(archiveRepository, times(1)).saveAll(archivedEntities);
    }

    @Test
    void testFindByReferenceId() {
        ArchivedEntity archivedEntity = new ArchivedEntity();
        when(archiveRepository.findByReferenceIdAndOrganizationId(any(), any())).thenReturn(Optional.of(archivedEntity));

        Optional<ArchivedEntity> result = archivingService.findByReferenceId("sampleReferenceId", "sampleOrgId");

        assertThat(result).isPresent().contains(archivedEntity);
        verify(archiveRepository, times(1)).findByReferenceIdAndOrganizationId("sampleReferenceId", "sampleOrgId");
    }

    @Test
    void testSave() {
        ArchivedEntity archivedEntity = new ArchivedEntity();

        archivingService.save(archivedEntity);

        verify(archiveRepository, times(1)).save(archivedEntity);
    }
}
