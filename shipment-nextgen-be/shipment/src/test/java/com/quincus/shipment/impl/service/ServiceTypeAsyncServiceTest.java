package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.domain.ServiceType;
import com.quincus.shipment.impl.repository.ServiceTypeRepository;
import com.quincus.shipment.impl.repository.entity.ServiceTypeEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceTypeAsyncServiceTest {
    @InjectMocks
    private ServiceTypeAsyncService serviceTypeAsyncService;
    @Mock
    private ServiceTypeRepository serviceTypeRepository;

    @Test
    void givenValidServiceTypeCodeAndOrgWhenFindThenReturnServiceTypeEntity() {
        // Arrange
        String serviceTypeCode = "TestCode";
        String orgId = "TestOrgId";
        ServiceTypeEntity serviceTypeEntity = new ServiceTypeEntity();
        when(serviceTypeRepository.findByCodeAndOrganizationId(serviceTypeCode, orgId))
                .thenReturn(Optional.of(serviceTypeEntity));

        // Act
        ServiceTypeEntity result = serviceTypeAsyncService.find(serviceTypeCode, orgId);

        // Assert
        assertThat(result).isNotNull().isEqualTo(serviceTypeEntity);
        verify(serviceTypeRepository, times(1)).findByCodeAndOrganizationId(serviceTypeCode, orgId);
    }

    @Test
    void givenNullServiceTypeCodeWhenFindThenReturnNullAndNoException() {
        // Arrange
        String serviceTypeCode = null;
        String orgId = "TestOrgId";

        // Act
        ServiceTypeEntity result = serviceTypeAsyncService.find(serviceTypeCode, orgId);

        // Assert
        assertThat(result).isNull();
        verify(serviceTypeRepository, never()).findByCodeAndOrganizationId(any(), any());
    }

    @Test
    void givenServiceTypeCodeNotExistingWhenFindOrCreateServiceTypeSaveNewServiceType() {
        // Arrange
        ServiceType serviceType = new ServiceType();
        serviceType.setCode("TestCode");
        serviceType.setOrganizationId("TestOrgId");
        String organizationId = "TestOrgId";
        when(serviceTypeRepository.findByCodeAndOrganizationId(any(), any())).thenReturn(Optional.empty());
        when(serviceTypeRepository.save(any(ServiceTypeEntity.class))).thenReturn(new ServiceTypeEntity());

        // Act
        ServiceTypeEntity result = serviceTypeAsyncService.findOrCreateServiceType(serviceType, organizationId);

        // Assert
        assertThat(result).isNotNull();
        verify(serviceTypeRepository, times(1)).save(any(ServiceTypeEntity.class));
    }

    @Test
    void givenExistingServiceTypeCodeWhenFindOrCreateServiceTypeReturnExistingAndDontCreateNewData() {
        // Arrange
        ServiceType serviceType = new ServiceType();
        serviceType.setCode("TestCode");
        serviceType.setOrganizationId("TestOrgId");
        String organizationId = "TestOrgId";
        ServiceTypeEntity existingServiceTypeEntity = new ServiceTypeEntity();
        when(serviceTypeRepository.findByCodeAndOrganizationId(any(), any()))
                .thenReturn(Optional.of(existingServiceTypeEntity));

        // WHEN:
        ServiceTypeEntity result = serviceTypeAsyncService.findOrCreateServiceType(serviceType, organizationId);

        // THEN
        assertThat(result).isNotNull().isEqualTo(existingServiceTypeEntity);
        verify(serviceTypeRepository, never()).save(any(ServiceTypeEntity.class));
    }
}
