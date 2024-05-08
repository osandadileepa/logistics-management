package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.filter.Filter;
import com.quincus.shipment.impl.repository.CustomerRepository;
import com.quincus.shipment.impl.repository.entity.CustomerEntity;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
    @InjectMocks
    private CustomerService customerService;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private UserDetailsProvider userDetailsProvider;

    @Test
    void find_customerFound_shouldReturnCustomerEntity() {
        CustomerEntity sourceCustomerEntity = new CustomerEntity();
        sourceCustomerEntity.setName("Name");
        sourceCustomerEntity.setCode("CODE");
        sourceCustomerEntity.setOrganizationId("ORG1");

        CustomerEntity savedCustomerEntity = new CustomerEntity();
        savedCustomerEntity.setName("Name");
        savedCustomerEntity.setCode("CODE");
        savedCustomerEntity.setOrganizationId("ORG1");

        when(customerRepository.findByCodeAndOrganizationId(anyString(), anyString())).thenReturn(Optional.of(savedCustomerEntity));

        CustomerEntity resultCustomerEntity = customerService.find(sourceCustomerEntity);

        assertThat(resultCustomerEntity).isNotNull();
        assertThat(savedCustomerEntity).usingRecursiveComparison().isEqualTo(sourceCustomerEntity);

        verify(customerRepository, times(1)).findByCodeAndOrganizationId(anyString(), anyString());
    }

    @Test
    void find_customerNotFound_shouldReturnNull() {
        CustomerEntity sourceCustomerEntity = new CustomerEntity();
        sourceCustomerEntity.setName("Name");
        sourceCustomerEntity.setCode("CODE");
        sourceCustomerEntity.setOrganizationId("ORG1");

        when(customerRepository.findByCodeAndOrganizationId(anyString(), anyString())).thenReturn(Optional.empty());

        CustomerEntity resultCustomerEntity = customerService.find(sourceCustomerEntity);

        assertThat(resultCustomerEntity).isNull();

        verify(customerRepository, times(1)).findByCodeAndOrganizationId(anyString(), anyString());
    }

    @Test
    void find_nullArgument_shouldReturnNull() {
        CustomerEntity resultCustomerEntity = customerService.find(null);

        assertThat(resultCustomerEntity).isNull();

        verify(customerRepository, never()).findByCodeAndOrganizationId(anyString(), anyString());
    }

    @Test
    void findByOrgId_customerFound_shouldReturnCustomerList() {
        String organizationId = "ORG1";

        CustomerEntity savedCustomerEntity = new CustomerEntity();
        savedCustomerEntity.setName("Name");
        savedCustomerEntity.setCode("CODE");
        savedCustomerEntity.setOrganizationId(organizationId);

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(organizationId);
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(organizationId);
        when(customerRepository.findByOrganizationId(anyString())).thenReturn(List.of(savedCustomerEntity));

        List<CustomerEntity> customerEntityList = customerService.findByOrganizationId();

        assertThat(customerEntityList).isNotNull();
        assertThat(customerEntityList.get(0).getOrganizationId()).isEqualTo(organizationId);

        verify(customerRepository, times(1)).findByOrganizationId(anyString());
    }

    @Test
    void findByOrgId_customerNotFound_shouldReturnEmptyList() {
        String organizationId = "ORG1";

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(organizationId);
        when(customerRepository.findByOrganizationId(anyString())).thenReturn(Collections.emptyList());

        List<CustomerEntity> customerEntityList = customerService.findByOrganizationId();

        assertThat(customerEntityList).isEmpty();

        verify(customerRepository, times(1)).findByOrganizationId(anyString());
    }

    @Test
    void findByOrgId_nullArgument_shouldReturnEmptyList() {
        List<CustomerEntity> customerEntityList = customerService.findByOrganizationId();

        assertThat(customerEntityList).isEmpty();

        verify(customerRepository, never()).findByOrganizationId(anyString());
    }

    @Test
    void findOrCreateCustomer_customerFound_shouldReturnExistingCustomerEntity() {
        CustomerEntity customer = new CustomerEntity();
        customer.setName("Name");
        String customerCode = "CODE";
        customer.setCode(customerCode);
        String organizationId = "ORG1";
        customer.setOrganizationId(organizationId);

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(organizationId);
        when(customerRepository.findByCodeAndOrganizationId(customerCode, organizationId)).thenReturn(Optional.of(customer));

        CustomerEntity resultCustomer = customerService.findOrCreateCustomer(customer);

        assertThat(resultCustomer).isNotNull().isEqualTo(customer);

        verify(customerRepository, times(1)).findByCodeAndOrganizationId(customerCode, organizationId);
        verify(customerRepository, never()).save(customer);
    }

    @Test
    void findOrCreateCustomer_customerNotFound_shouldReturnNewCustomerEntity() {
        CustomerEntity customer = new CustomerEntity();
        customer.setName("Name");
        String customerCode = "CODE";
        customer.setCode(customerCode);
        String organizationId = "ORG1";
        customer.setOrganizationId(organizationId);

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(organizationId);
        when(customerRepository.findByCodeAndOrganizationId(customerCode, organizationId)).thenReturn(Optional.empty());
        when(customerRepository.save(any(CustomerEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        CustomerEntity resultCustomer = customerService.findOrCreateCustomer(customer);

        assertThat(resultCustomer).isNotNull().isEqualTo(customer);

        verify(customerRepository, times(1)).findByCodeAndOrganizationId(customerCode, organizationId);
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    void findOrCreateCustomer_nullArguments_shouldReturnNull() {
        CustomerEntity resultCustomer = customerService.findOrCreateCustomer(null);

        assertThat(resultCustomer).isNull();

        verify(customerRepository, never()).findByCodeAndOrganizationId(anyString(), anyString());
        verify(customerRepository, never()).save(any());
    }

    @Test
    void findOrCreateCustomer_argumentNullCode_shouldReturnNull() {
        CustomerEntity customer = new CustomerEntity();
        customer.setName("Name");
        String organizationId = "ORG1";
        customer.setOrganizationId(organizationId);

        CustomerEntity resultCustomer = customerService.findOrCreateCustomer(customer);

        assertThat(resultCustomer).isNull();

        verify(customerRepository, never()).findByCodeAndOrganizationId(anyString(), anyString());
        verify(customerRepository, never()).save(any());
    }

    @Test
    void findOrCreateCustomer_argumentNullOrgId_shouldReturnNull() {
        CustomerEntity customer = new CustomerEntity();
        customer.setName("Name");
        String customerCode = " ";
        customer.setCode(customerCode);

        CustomerEntity resultCustomer = customerService.findOrCreateCustomer(customer);

        assertThat(resultCustomer).isNull();

        verify(customerRepository, never()).findByCodeAndOrganizationId(anyString(), anyString());
        verify(customerRepository, never()).save(any());
    }

    @Test
    void findOrCreateCustomer_saveThrowsException_shouldReturnExistingCustomerEntity() {
        CustomerEntity customer = new CustomerEntity();
        customer.setName("Name");
        String customerCode = "CODE";
        customer.setCode(customerCode);
        String organizationId = "ORG1";
        customer.setOrganizationId(organizationId);

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(organizationId);
        when(customerRepository.findByCodeAndOrganizationId(customerCode, organizationId)).thenReturn(Optional.empty())
                .thenReturn(Optional.of(customer));
        doThrow(new ConstraintViolationException("Not an exception. Test Only.", new SQLException(), "none"))
                .when(customerRepository).save(any(CustomerEntity.class));

        var resultCustomer = customerService.findOrCreateCustomer(customer);

        assertThat(resultCustomer).isNotNull().isEqualTo(customer);

        verify(customerRepository, times(2)).findByCodeAndOrganizationId(customerCode, organizationId);
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    void findPageableCustomersByFilter_filterArgumentWithKey_shouldCallFindByOrgIdAndNameContaining() {
        Filter filter = new Filter();
        String organizationId = "ORG1";
        String key = "KEY";
        filter.setKey(key);
        filter.setPage(1);
        filter.setPerPage(10);

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(organizationId);
        customerService.findPageableCustomersByFilter(filter);

        verify(customerRepository, times(1)).findByOrganizationIdAndNameContaining(eq(organizationId), eq(key), any(Pageable.class));
    }

    @Test
    void findPageableCustomersByFilter_filterArgumentWithoutKey_shouldCallFindByOrgId() {
        Filter filter = new Filter();
        String organizationId = "ORG1";
        filter.setPage(1);
        filter.setPerPage(10);

        customerService.findPageableCustomersByFilter(filter);

        verify(customerRepository, times(1)).findByOrganizationId(any(), any());
    }
}
