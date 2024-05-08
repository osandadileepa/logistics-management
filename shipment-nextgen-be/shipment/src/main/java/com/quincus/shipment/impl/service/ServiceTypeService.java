package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.filter.Filter;
import com.quincus.shipment.impl.repository.ServiceTypeRepository;
import com.quincus.shipment.impl.repository.entity.ServiceTypeEntity;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.Objects.isNull;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ServiceTypeService {
    private final ServiceTypeRepository serviceTypeRepository;
    private final UserDetailsProvider userDetailsProvider;

    public ServiceTypeEntity find(final String serviceTypeCode) {
        if (StringUtils.isBlank(serviceTypeCode)) return null;
        return serviceTypeRepository.findByCodeAndOrganizationId(serviceTypeCode, userDetailsProvider.getCurrentOrganizationId()).orElse(null);
    }

    public Page<ServiceTypeEntity> findPageableServiceTypesByFilter(final Filter filter) {
        Pageable pageable = PageRequest.of((filter.getPage() - 1), filter.getPerPage());
        if (StringUtils.isNotBlank(filter.getKey())) {
            return serviceTypeRepository.findByOrganizationIdAndNameContaining(userDetailsProvider.getCurrentOrganizationId(), filter.getKey(), pageable);
        } else {
            return serviceTypeRepository.findByOrganizationId(userDetailsProvider.getCurrentOrganizationId(), pageable);
        }
    }

    @Transactional
    public ServiceTypeEntity findOrCreateServiceType(final ServiceTypeEntity serviceTypeEntity) {
        if ((isNull(serviceTypeEntity)) || (StringUtils.isBlank(serviceTypeEntity.getCode()))) {
            return null;
        }
        final ServiceTypeEntity existingServiceTypeEntity = find(serviceTypeEntity.getCode());
        try {
            return isNull(existingServiceTypeEntity) ? serviceTypeRepository.save(serviceTypeEntity) : existingServiceTypeEntity;
        } catch (ConstraintViolationException constraintViolationException) {
            return find(serviceTypeEntity.getCode());
        }
    }

    public List<ServiceTypeEntity> findAllByIds(final Set<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return serviceTypeRepository.findAllById(ids);
    }

    public Page<ServiceTypeEntity> findPageableServiceTypesByFilterForNetworkLane(final Filter filter) {
        Pageable pageable = PageRequest.of((filter.getPage() - 1), filter.getPerPage());
        if (StringUtils.isBlank(filter.getKey())) {
            return serviceTypeRepository.findByOrganizationIdForNetworkLane(userDetailsProvider.getCurrentOrganizationId(), pageable);
        }
        return serviceTypeRepository.findByOrganizationIdAndNameContainingForNetworkLane(userDetailsProvider.getCurrentOrganizationId(), filter.getKey(), pageable);
    }
}