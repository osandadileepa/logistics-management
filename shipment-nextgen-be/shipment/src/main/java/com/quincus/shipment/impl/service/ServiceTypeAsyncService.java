package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.domain.ServiceType;
import com.quincus.shipment.impl.mapper.ServiceTypeMapper;
import com.quincus.shipment.impl.repository.ServiceTypeRepository;
import com.quincus.shipment.impl.repository.entity.ServiceTypeEntity;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.isNull;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ServiceTypeAsyncService {
    private final ServiceTypeRepository serviceTypeRepository;

    public ServiceTypeEntity find(final String serviceTypeCode, String orgId) {
        if (StringUtils.isBlank(serviceTypeCode)) return null;
        return serviceTypeRepository.findByCodeAndOrganizationId(serviceTypeCode, orgId).orElse(null);
    }

    @Transactional
    public ServiceTypeEntity findOrCreateServiceType(final ServiceType serviceType, final String organizationId) {
        if ((isNull(serviceType))
                || StringUtils.isBlank(serviceType.getCode())
                || StringUtils.isBlank(organizationId)) {
            return null;
        }

        Optional<ServiceTypeEntity> existingServiceTypeEntity = Optional.ofNullable(find(serviceType.getCode(), serviceType.getOrganizationId()));
        return existingServiceTypeEntity.orElseGet(() -> serviceTypeRepository.save(Objects.requireNonNull(ServiceTypeMapper.mapDomainToEntity(serviceType, organizationId))));
    }
}