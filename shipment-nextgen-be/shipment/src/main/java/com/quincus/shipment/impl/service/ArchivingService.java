package com.quincus.shipment.impl.service;

import com.quincus.shipment.impl.repository.ArchiveRepository;
import com.quincus.shipment.impl.repository.entity.ArchivedEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArchivingService {
    private final ArchiveRepository archiveRepository;

    @Transactional
    public void saveAll(List<ArchivedEntity> archivedEntities) {
        archiveRepository.saveAll(archivedEntities);
    }

    public Optional<ArchivedEntity> findByReferenceId(String referenceId, String organizationId) {
        return archiveRepository.findByReferenceIdAndOrganizationId(referenceId, organizationId);
    }

    public Optional<ArchivedEntity> findLatestByReferenceId(String referenceId, String organizationId) {
        return archiveRepository.findFirstByReferenceIdAndOrganizationIdOrderByCreateTimeDesc(referenceId, organizationId);
    }

    @Transactional
    public void save(ArchivedEntity archivedEntity) {
        archiveRepository.save(archivedEntity);
    }
}
