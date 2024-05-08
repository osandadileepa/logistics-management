package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.domain.Customer;
import com.quincus.shipment.impl.repository.entity.CustomerEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.NONE)
public class CustomerMapper {

    public static CustomerEntity mapDomainToEntity(Customer customerDomain) {
        if (customerDomain == null) {
            return null;
        }

        CustomerEntity customerEntity = new CustomerEntity();

        customerEntity.setId(customerDomain.getId());
        customerEntity.setCode(customerDomain.getCode());
        customerEntity.setName(customerDomain.getName());
        customerEntity.setOrganizationId(customerDomain.getOrganizationId());

        return customerEntity;
    }

    public static CustomerEntity mapDomainToEntity(Customer customerDomain, String organizationId) {
        if (customerDomain == null) {
            return null;
        }

        CustomerEntity customerEntity = new CustomerEntity();

        customerEntity.setId(customerDomain.getId());
        customerEntity.setCode(customerDomain.getCode());
        customerEntity.setName(customerDomain.getName());
        customerEntity.setOrganizationId(organizationId);

        return customerEntity;
    }

    public static Customer mapEntityToDomain(CustomerEntity customerEntity) {
        if (customerEntity == null) {
            return null;
        }

        Customer customerDomain = new Customer();

        customerDomain.setId(customerEntity.getId());
        customerDomain.setCode(customerEntity.getCode());
        customerDomain.setName(customerEntity.getName());
        customerDomain.setOrganizationId(customerEntity.getOrganizationId());

        return customerDomain;
    }
}
