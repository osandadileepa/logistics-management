package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.impl.mapper.OrganizationMapper;
import com.quincus.shipment.impl.repository.OrganizationRepository;
import com.quincus.shipment.impl.repository.entity.OrganizationEntity;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
public class OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final UserDetailsProvider userDetailsProvider;

    @Transactional
    public OrganizationEntity findOrCreateOrganization() {
        Organization organization = userDetailsProvider.getCurrentOrganization();
        Optional<OrganizationEntity> optionalOrganizationEntity = organizationRepository.findById(organization.getId());
        return optionalOrganizationEntity.orElseGet(() -> organizationRepository.save(OrganizationMapper.mapDomainToEntity(organization)));
    }
}
