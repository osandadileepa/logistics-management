package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.impl.repository.entity.OrganizationEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.NONE)
public class OrganizationMapper {

    public static OrganizationEntity mapDomainToEntity(Organization organizationDomain) {
        if (organizationDomain == null) {
            return null;
        }

        OrganizationEntity organizationEntity = new OrganizationEntity();

        organizationEntity.setId(organizationDomain.getId());
        organizationEntity.setName(organizationDomain.getName());
        organizationEntity.setCode(organizationDomain.getCode());

        return organizationEntity;
    }

    public static Organization mapEntityToDomain(OrganizationEntity organizationEntity) {
        if (organizationEntity == null) {
            return null;
        }

        Organization organizationDomain = new Organization();

        organizationDomain.setId(organizationEntity.getId());
        organizationDomain.setName(organizationEntity.getName());
        organizationDomain.setCode(organizationEntity.getCode());

        return organizationDomain;
    }
}
