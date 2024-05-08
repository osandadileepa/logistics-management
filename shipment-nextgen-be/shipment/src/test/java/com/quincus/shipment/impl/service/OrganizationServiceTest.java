package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.impl.repository.OrganizationRepository;
import com.quincus.shipment.impl.repository.entity.OrganizationEntity;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {
    @InjectMocks
    private OrganizationService organizationService;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private UserDetailsProvider userDetailsProvider;

    @Test
    void findOrCreateOrganization_organizationFound_shouldReturnExistingOrganization() {
        String organizationId = "ORG1";
        Organization organization = new Organization();
        organization.setId(organizationId);
        OrganizationEntity organizationEntity = new OrganizationEntity();
        organizationEntity.setId(organizationId);
        organizationEntity.setCode("CODE1");
        organizationEntity.setName("Name");

        when(userDetailsProvider.getCurrentOrganization()).thenReturn(organization);
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(organizationEntity));

        OrganizationEntity resultOrganization = organizationService.findOrCreateOrganization();

        assertThat(resultOrganization).isNotNull().isEqualTo(organizationEntity);

        verify(organizationRepository, times(1)).findById(organizationId);
        verify(organizationRepository, never()).save(any());
    }

    @Test
    void findOrCreateOrganization_organizationNotFound_shouldReturnNewOrganization() {
        String organizationId = "ORG1";
        String name = "Name";
        Organization organization = new Organization();
        organization.setId(organizationId);
        organization.setName(name);
        OrganizationEntity organizationEntity = new OrganizationEntity();
        organizationEntity.setId(organizationId);
        organizationEntity.setCode("CODE1");
        organizationEntity.setName(name);

        when(userDetailsProvider.getCurrentOrganization()).thenReturn(organization);
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.empty());
        when(organizationRepository.save(any())).thenReturn(organizationEntity);

        OrganizationEntity resultOrganization = organizationService.findOrCreateOrganization();

        assertThat(resultOrganization).isNotNull().isEqualTo(organizationEntity);

        verify(organizationRepository, times(1)).findById(organizationId);
        verify(organizationRepository, times(1)).save(any());
    }

}
