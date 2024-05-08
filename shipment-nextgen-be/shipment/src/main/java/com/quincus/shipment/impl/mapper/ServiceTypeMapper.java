package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.domain.ServiceType;
import com.quincus.shipment.impl.repository.entity.ServiceTypeEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import static java.util.Objects.isNull;

@NoArgsConstructor(access = AccessLevel.NONE)
public class ServiceTypeMapper {

    public static ServiceTypeEntity mapDomainToEntity(final ServiceType serviceTypeDomain, final String organizationId) {
        if (isNull(serviceTypeDomain)) {
            return null;
        }
        ServiceTypeEntity serviceTypeEntity = new ServiceTypeEntity();
        serviceTypeEntity.setId(serviceTypeDomain.getId());
        serviceTypeEntity.setCode(serviceTypeDomain.getCode());
        serviceTypeEntity.setName(serviceTypeDomain.getName());
        if (StringUtils.isNotEmpty(organizationId)) {
            serviceTypeEntity.setOrganizationId(organizationId);
        }
        return serviceTypeEntity;
    }

    public static ServiceType mapEntityToDomain(final ServiceTypeEntity serviceTypeEntity) {
        if (isNull(serviceTypeEntity)) {
            return null;
        }
        ServiceType serviceTypeDomain = new ServiceType();
        serviceTypeDomain.setId(serviceTypeEntity.getId());
        serviceTypeDomain.setCode(serviceTypeEntity.getCode());
        serviceTypeDomain.setName(serviceTypeEntity.getName());
        serviceTypeDomain.setOrganizationId(serviceTypeEntity.getOrganizationId());
        return serviceTypeDomain;
    }
}
