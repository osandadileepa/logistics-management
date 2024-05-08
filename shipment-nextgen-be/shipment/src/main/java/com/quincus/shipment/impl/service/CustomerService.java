package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.filter.Filter;
import com.quincus.shipment.impl.repository.CustomerRepository;
import com.quincus.shipment.impl.repository.entity.CustomerEntity;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.Objects.isNull;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final UserDetailsProvider userDetailsProvider;

    public CustomerEntity find(final CustomerEntity customerEntity) {
        if (isNull(customerEntity)) return null;
        return customerRepository.findByCodeAndOrganizationId(customerEntity.getCode(), customerEntity.getOrganizationId()).orElse(null);
    }

    public List<CustomerEntity> findByOrganizationId() {
        return customerRepository.findByOrganizationId(userDetailsProvider.getCurrentOrganizationId());
    }

    public Page<CustomerEntity> findPageableCustomersByFilter(final Filter filter) {
        Pageable pageable = PageRequest.of((filter.getPage() - 1), filter.getPerPage());
        if (StringUtils.isNotEmpty(filter.getKey())) {
            return customerRepository.findByOrganizationIdAndNameContaining(userDetailsProvider.getCurrentOrganizationId(), filter.getKey(), pageable);
        } else {
            return customerRepository.findByOrganizationId(userDetailsProvider.getCurrentOrganizationId(), pageable);
        }
    }

    @Transactional
    public CustomerEntity findOrCreateCustomer(final CustomerEntity customerEntity) {
        if (isNull(customerEntity) || StringUtils.isBlank(customerEntity.getCode())) {
            return null;
        }
        String organizationId = userDetailsProvider.getCurrentOrganizationId();
        final CustomerEntity existingCustomerEntity = customerRepository.findByCodeAndOrganizationId(customerEntity.getCode(),
                organizationId).orElse(null);
        try {
            return isNull(existingCustomerEntity) ?
                    customerRepository.save(customerEntity) : existingCustomerEntity;
        } catch (ConstraintViolationException constraintViolationException) {
            return customerRepository.findByCodeAndOrganizationId(customerEntity.getCode(),
                    organizationId).orElse(null);
        }
    }
}
