package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.impl.repository.entity.OrganizationEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OrganizationMapperTest {

    @Test
    void mapDomainToEntity_organizationDomain_shouldReturnOrganizationEntity() {
        Organization domain = new Organization();
        domain.setId("ORG1");
        domain.setCode("CODE1");
        domain.setName("Organization");

        final OrganizationEntity entity = OrganizationMapper.mapDomainToEntity(domain);

        assertThat(entity)
                .usingRecursiveComparison()
                .ignoringFields("version", "modifyTime", "createTime")
                .isEqualTo(domain);
    }

    @Test
    void mapDomainToEntity_organizationDomainNull_shouldReturnNull() {
        assertThat(OrganizationMapper.mapDomainToEntity(null)).isNull();
    }

    @Test
    void mapEntityToDomain_organizationEntity_shouldReturnOrganizationDomain() {
        OrganizationEntity entity = new OrganizationEntity();
        entity.setId("organization-1");
        entity.setCode("org-code");
        entity.setName("Organization Name");

        final Organization domain = OrganizationMapper.mapEntityToDomain(entity);

        assertThat(domain)
                .usingRecursiveComparison()
                .ignoringFields("version", "modifyTime", "createTime")
                .isEqualTo(entity);
    }

    @Test
    void mapEntityToDomain_organizationEntityNull_shouldReturnNull() {
        assertThat(OrganizationMapper.mapEntityToDomain(null)).isNull();
    }

}
