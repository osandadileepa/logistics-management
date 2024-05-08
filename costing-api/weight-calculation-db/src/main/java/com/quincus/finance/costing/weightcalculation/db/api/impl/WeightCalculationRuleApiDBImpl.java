package com.quincus.finance.costing.weightcalculation.db.api.impl;

import com.quincus.db.impl.OrganizationDBImpl;
import com.quincus.db.impl.PartnerDBImpl;
import com.quincus.finance.costing.weightcalculation.api.WeightCalculationRuleApi;
import com.quincus.finance.costing.weightcalculation.api.filter.WeightCalculationRuleFilter;
import com.quincus.finance.costing.weightcalculation.api.model.WeightCalculationRule;
import com.quincus.finance.costing.weightcalculation.db.mapper.WeightCalculationRuleMapper;
import com.quincus.finance.costing.weightcalculation.db.model.WeightCalculationRuleEntity;
import com.quincus.finance.costing.weightcalculation.db.repository.WeightCalculationRuleRepository;
import com.quincus.finance.costing.weightcalculation.db.repository.specification.WeightCalculationRuleSpecification;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@AllArgsConstructor
@Service
public class WeightCalculationRuleApiDBImpl implements WeightCalculationRuleApi {

    private final WeightCalculationRuleRepository weightCalculationRuleRepository;

    private final WeightCalculationRuleMapper mapper;

    private final OrganizationDBImpl organizationDBImpl;

    private final PartnerDBImpl partnerDBImpl;


    public WeightCalculationRule create(WeightCalculationRule weightCalculationRule) {
        WeightCalculationRuleEntity weightCalculationRuleEntity = mapper.mapDomainToEntity(weightCalculationRule);
        weightCalculationRuleEntity.setOrganization(organizationDBImpl.getOrCreateOrganizationEntity(weightCalculationRule.getOrganizationId()));

        if (!CollectionUtils.isEmpty(weightCalculationRule.getPartners())) {
            weightCalculationRuleEntity.setPartners(partnerDBImpl.getOrCreatePartnerEntities(weightCalculationRuleEntity.getPartners()));
        }
        return mapper.mapEntityToDomain(weightCalculationRuleRepository.save(weightCalculationRuleEntity));

    }

    @Override
    public Optional<WeightCalculationRule> find(String id) {
        return findById(id).map(mapper::mapEntityToDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WeightCalculationRule> findActiveByOrganizationIdAndPartnerIds(String organizationId, Set<String> partnerIds) {
        final List<WeightCalculationRule> rules = new ArrayList<>();
        List<WeightCalculationRuleEntity> result = weightCalculationRuleRepository.findActiveByOrganizationIdAndPartnerIds(organizationId, partnerIds, Boolean.TRUE, Boolean.FALSE);
        if (!CollectionUtils.isEmpty(result)) {
            result.forEach(e -> rules.add(mapper.mapEntityToDomain(e)));
        }
        return rules;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WeightCalculationRule> findActiveByOrganizationId(String organizationId) {
        final List<WeightCalculationRule> rules = new ArrayList<>();
        List<WeightCalculationRuleEntity> result = weightCalculationRuleRepository.findActiveByOrganizationId(organizationId, Boolean.TRUE, Boolean.FALSE);
        if (!CollectionUtils.isEmpty(result)) {
            result.forEach(e -> rules.add(mapper.mapEntityToDomain(e)));
        }
        return rules;
    }

    @Override
    public Optional<WeightCalculationRule> update(WeightCalculationRule weightCalculationRule) {
        Optional<WeightCalculationRuleEntity> retrievedEntity = findById(weightCalculationRule.getId());

        if (retrievedEntity.isEmpty()) {
            return Optional.empty();
        }

        WeightCalculationRuleEntity weightCalculationRuleEntity = mapper.update(weightCalculationRule, retrievedEntity.get());
        weightCalculationRuleEntity.setOrganization(organizationDBImpl.getOrCreateOrganizationEntity(weightCalculationRule.getOrganizationId()));
        if (!CollectionUtils.isEmpty(weightCalculationRule.getPartners())) {
            weightCalculationRuleEntity.setPartners(partnerDBImpl.getOrCreatePartnerEntities(weightCalculationRuleEntity.getPartners()));
        }
        return Optional.of(mapper.mapEntityToDomain(weightCalculationRuleRepository.save(weightCalculationRuleEntity)));
    }

    @Override
    public void delete(String id) {
        Optional<WeightCalculationRuleEntity> entity = findById(id);
        if (entity.isPresent()) {
            entity.get().setActive(false);
            entity.get().setDeleted(true);
            weightCalculationRuleRepository.save(entity.get());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WeightCalculationRule> search(WeightCalculationRuleFilter filter, Pageable pageable) {
        WeightCalculationRuleSpecification specification = new WeightCalculationRuleSpecification(filter);
        return weightCalculationRuleRepository.findAll(specification, pageable).map(mapper::mapEntityToDomain);
    }

    private Optional<WeightCalculationRuleEntity> findById(String id) {
        return weightCalculationRuleRepository.findById(id);
    }

}
