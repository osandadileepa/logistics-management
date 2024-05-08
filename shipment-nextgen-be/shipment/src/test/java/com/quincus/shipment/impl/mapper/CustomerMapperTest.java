package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.domain.Customer;
import com.quincus.shipment.impl.repository.entity.CustomerEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CustomerMapperTest {

    @Test
    void mapDomainToEntity_customerDomain_shouldReturnCustomerEntity() {
        Customer domain = new Customer();
        domain.setId("ID-1");
        domain.setCode("CODE1");
        domain.setName("Name");
        domain.setOrganizationId("ORG1");

        final CustomerEntity entity = CustomerMapper.mapDomainToEntity(domain);

        assertThat(entity)
                .usingRecursiveComparison()
                .ignoringFields("modifyTime", "createTime", "version")
                .isEqualTo(domain);
    }

    @Test
    void mapDomainToEntity_customerDomainNull_shouldReturnNull() {
        assertThat(CustomerMapper.mapDomainToEntity(null)).isNull();
    }

    @Test
    void mapEntityToDomain_customerEntity_shouldReturnCustomerDomain() {
        CustomerEntity entity = new CustomerEntity();
        entity.setId("ID1");
        entity.setCode("code1");
        entity.setName("name");
        entity.setOrganizationId("org1");

        final Customer domain = CustomerMapper.mapEntityToDomain(entity);

        assertThat(domain)
                .usingRecursiveComparison()
                .ignoringFields("modifyTime", "createTime", "version")
                .isEqualTo(entity);
    }

    @Test
    void mapEntityToDomain_customerEntityNull_shouldReturnNull() {
        assertThat(CustomerMapper.mapEntityToDomain(null)).isNull();
    }
}
