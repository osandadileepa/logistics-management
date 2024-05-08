package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.domain.ServiceType;
import com.quincus.shipment.impl.repository.entity.OrganizationEntity;
import com.quincus.shipment.impl.repository.entity.ServiceTypeEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ServiceTypeMapperTest {

    @Test
    void mapDomainToEntity_serviceTypeDomain_shouldReturnServiceTypeEntity() {
        ServiceType domain = new ServiceType();
        domain.setId("SERVICE1");
        domain.setCode("SVC1");
        domain.setName("-NAME-");
        domain.setOrganizationId("ORG1");
        OrganizationEntity organization = new OrganizationEntity();
        organization.setId("ORG1");
        final ServiceTypeEntity entity = ServiceTypeMapper.mapDomainToEntity(domain, organization.getId());

        assertThat(entity)
                .usingRecursiveComparison()
                .ignoringFields("modifyTime", "createTime", "version", "description")
                .isEqualTo(domain);
    }

    @Test
    void mapDomainToEntity_serviceTypeDomainNull_shouldReturnNull() {
        assertThat(ServiceTypeMapper.mapDomainToEntity(null, null)).isNull();
    }

    @Test
    void mapEntityToDomain_serviceTypeEntity_shouldReturnServiceTypeDomain() {
        ServiceTypeEntity entity = new ServiceTypeEntity();
        entity.setId("service1");
        entity.setCode("svc-code1");
        entity.setName("serviceType");
        entity.setDescription("service desc");
        OrganizationEntity organization = new OrganizationEntity();
        organization.setId("org1");
        entity.setOrganizationId(organization.getId());

        final ServiceType domain = ServiceTypeMapper.mapEntityToDomain(entity);

        assertThat(domain)
                .usingRecursiveComparison()
                .ignoringFields("remark", "group", "status")
                .isEqualTo(entity);
    }

    @Test
    void mapEntityToDomain_serviceTypeEntityNull_shouldReturnNull() {
        assertThat(ServiceTypeMapper.mapEntityToDomain(null)).isNull();
    }
}
