package com.quincus.shipment.impl.api;

import com.quincus.shipment.api.ArchivedApi;
import com.quincus.shipment.api.domain.Archived;
import com.quincus.shipment.impl.mapper.ArchivedMapper;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.service.ArchivingService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Service
public class ArchivedApiImpl implements ArchivedApi {

    private final ArchivingService archivingService;
    private final ArchivedMapper archivedMapper;
    private final UserDetailsProvider userDetailsProvider;

    @Override
    public void save(Archived archived) {
        archivingService.save(archivedMapper.toEntity(archived));
    }

    @Override
    public Optional<Archived> findLatestByReferenceId(String referenceId) {
        return archivingService.findLatestByReferenceId(referenceId, userDetailsProvider.getCurrentOrganizationId())
                .map(archivedMapper::toDomain);
    }
}
