package com.quincus.db.impl;

import com.quincus.db.model.OrganizationEntity;
import com.quincus.db.repository.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class OrganizationDBImpl {
    private final OrganizationRepository organizationRepository;

    public OrganizationDBImpl(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public OrganizationEntity getOrCreateOrganizationEntity(String organizationId) {
        Assert.notNull(organizationId, "organizationId must not be empty null.");
        return organizationRepository.findById(organizationId).orElse(new OrganizationEntity(organizationId));
    }
}
