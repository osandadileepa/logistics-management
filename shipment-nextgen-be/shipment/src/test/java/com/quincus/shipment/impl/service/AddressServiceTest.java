package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.impl.repository.AddressRepository;
import com.quincus.shipment.impl.repository.entity.AddressEntity;
import com.quincus.shipment.impl.repository.entity.OrganizationEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @InjectMocks
    private AddressService addressService;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private LocationHierarchyService locationHierarchyService;

    @Test
    void createAddressEntityForOrganization_validArguments_shouldReturnAddress() {
        Address domainAddress = new Address();
        String country = "US";
        domainAddress.setCountry(country);
        String state = "MA";
        domainAddress.setState(state);
        String city = "Boston";
        domainAddress.setCity(city);

        String organizationId = "ORG1";
        OrganizationEntity organization = new OrganizationEntity();
        organization.setId(organizationId);

        when(addressRepository.save(any(AddressEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        AddressEntity resultAddress = addressService.createAddressEntityForOrganization(domainAddress);

        assertThat(resultAddress).isNotNull();

        verify(addressRepository, times(1)).save(any(AddressEntity.class));
    }

    @Test
    void createAddressEntityForOrganization_nullArguments_shouldReturnNull() {
        AddressEntity resultAddress = addressService.createAddressEntityForOrganization(null);

        assertThat(resultAddress).isNull();

        verify(addressRepository, never()).save(any(AddressEntity.class));
    }
}
