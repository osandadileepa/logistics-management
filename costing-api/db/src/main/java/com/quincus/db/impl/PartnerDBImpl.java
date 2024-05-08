package com.quincus.db.impl;

import com.quincus.db.model.PartnerEntity;
import com.quincus.db.repository.PartnerRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PartnerDBImpl {
    private final PartnerRepository partnerRepository;

    public PartnerDBImpl(PartnerRepository partnerRepository) {
        this.partnerRepository = partnerRepository;
    }

    public Set<PartnerEntity> getOrCreatePartnerEntities(Set<PartnerEntity> partners) {
        Set<String> partnerIds = partners.stream().map(PartnerEntity::getId).collect(Collectors.toSet());
        List<PartnerEntity> existingPartners = partnerRepository.findAllById(partnerIds);

        if (CollectionUtils.isEmpty(existingPartners)) {
            return partners;
        }

        Set<PartnerEntity> result = new HashSet<>(existingPartners);
        if (result.size() != partnerIds.size()) {
            result.addAll(getNonExistingPartners(partners, result));
        }
        return result;
    }

    private Set<PartnerEntity> getNonExistingPartners(Set<PartnerEntity> partners, Set<PartnerEntity> existingPartners) {
        Set<String> existingPartnerIds = existingPartners.stream().map(PartnerEntity::getId).collect(Collectors.toSet());
        return partners.stream().filter(partner -> !existingPartnerIds.contains(partner.getId())).collect(Collectors.toSet());
    }
}


