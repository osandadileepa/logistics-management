package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.filter.Filter;
import com.quincus.shipment.impl.repository.ServiceTypeRepository;
import com.quincus.shipment.impl.repository.entity.OrganizationEntity;
import com.quincus.shipment.impl.repository.entity.ServiceTypeEntity;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceTypeServiceTest {
    @InjectMocks
    private ServiceTypeService serviceTypeService;
    @Mock
    private ServiceTypeRepository serviceTypeRepository;
    @Mock
    private UserDetailsProvider userDetailsProvider;

    @Test
    void find_serviceTypeFound_shouldReturnServiceTypeEntity() {
        String serviceTypeCode = "CODE";
        String organizationId = "ORG1";
        ServiceTypeEntity serviceType = new ServiceTypeEntity();
        serviceType.setCode(serviceTypeCode);
        serviceType.setOrganizationId(organizationId);

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(organizationId);
        when(serviceTypeRepository.findByCodeAndOrganizationId(serviceTypeCode, organizationId))
                .thenReturn(Optional.of(new ServiceTypeEntity()));

        ServiceTypeEntity serviceTypeEntity = serviceTypeService.find(serviceType.getCode());

        assertThat(serviceTypeEntity).isNotNull();

        verify(serviceTypeRepository, times(1)).findByCodeAndOrganizationId(anyString(), anyString());
    }

    @Test
    void find_serviceTypeNotFound_shouldReturnNull() {
        String serviceTypeCode = "CODE";
        String organizationId = "ORG1";
        ServiceTypeEntity serviceType = new ServiceTypeEntity();
        serviceType.setCode(serviceTypeCode);
        serviceType.setOrganizationId(organizationId);

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(organizationId);
        when(serviceTypeRepository.findByCodeAndOrganizationId(serviceTypeCode, organizationId))
                .thenReturn(Optional.empty());

        ServiceTypeEntity serviceTypeEntity = serviceTypeService.find(serviceType.getCode());

        assertThat(serviceTypeEntity).isNull();

        verify(serviceTypeRepository, times(1)).findByCodeAndOrganizationId(anyString(), anyString());
    }

    @Test
    void find_nullArguments_shouldReturnNull() {
        ServiceTypeEntity serviceTypeEntity = serviceTypeService.find(null);

        assertThat(serviceTypeEntity).isNull();

        verify(serviceTypeRepository, never()).findByCodeAndOrganizationId(anyString(), anyString());
    }

    @Test
    void find_argumentNullCode_shouldReturnNull() {
        String organizationId = "ORG1";
        ServiceTypeEntity serviceType = new ServiceTypeEntity();
        serviceType.setOrganizationId(organizationId);

        ServiceTypeEntity serviceTypeEntity = serviceTypeService.find(serviceType.getCode());

        assertThat(serviceTypeEntity).isNull();

        verify(serviceTypeRepository, never()).findByCodeAndOrganizationId(anyString(), anyString());
    }

    @Test
    void find_argumentNullOrganization_shouldReturnNull() {
        String serviceTypeCode = "CODE";
        ServiceTypeEntity serviceType = new ServiceTypeEntity();
        serviceType.setCode(serviceTypeCode);

        ServiceTypeEntity serviceTypeEntity = serviceTypeService.find(serviceType.getCode());

        assertThat(serviceTypeEntity).isNull();

        verify(serviceTypeRepository, never()).findByCodeAndOrganizationId(anyString(), anyString());
    }

    @Test
    void findOrCreateServiceType_serviceTypeFound_shouldReturnExistingServiceType() {
        ServiceTypeEntity serviceType = new ServiceTypeEntity();
        serviceType.setId("SVC-ID1");
        String svcCode = "SVC-CODE1";
        serviceType.setCode(svcCode);
        serviceType.setName("Service Type");
        serviceType.setDescription("Service Type Description");

        String organizationId = "ORG1";
        serviceType.setOrganizationId(organizationId);

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(organizationId);
        when(serviceTypeRepository.findByCodeAndOrganizationId(svcCode, organizationId)).thenReturn(Optional.of(serviceType));

        ServiceTypeEntity serviceTypeEntity = serviceTypeService.findOrCreateServiceType(serviceType);

        assertThat(serviceTypeEntity).isEqualTo(serviceType);

        verify(serviceTypeRepository, times(1)).findByCodeAndOrganizationId(svcCode, organizationId);
        verify(serviceTypeRepository, never()).save(serviceType);
    }

    @Test
    void findOrCreateServiceType_serviceTypeNotFound_shouldReturnNewServiceType() {
        ServiceTypeEntity serviceType = new ServiceTypeEntity();
        serviceType.setId("SVC-ID1");
        String svcCode = "SVC-CODE1";
        serviceType.setCode(svcCode);
        serviceType.setName("Service Type");
        serviceType.setDescription("Service Type Description");

        String organizationId = "ORG1";
        serviceType.setOrganizationId(organizationId);

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(organizationId);
        when(serviceTypeRepository.findByCodeAndOrganizationId(svcCode, organizationId)).thenReturn(Optional.empty());
        when(serviceTypeRepository.save(any(ServiceTypeEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        ServiceTypeEntity serviceTypeEntity = serviceTypeService.findOrCreateServiceType(serviceType);

        assertThat(serviceTypeEntity).isEqualTo(serviceType);

        verify(serviceTypeRepository, times(1)).findByCodeAndOrganizationId(svcCode, organizationId);
        verify(serviceTypeRepository, times(1)).save(serviceType);
    }

    @Test
    void findOrCreateServiceType_nullArguments_shouldReturnNull() {
        ServiceTypeEntity serviceTypeEntity = serviceTypeService.findOrCreateServiceType(null);

        assertThat(serviceTypeEntity).isNull();

        verify(serviceTypeRepository, never()).findByCodeAndOrganizationId(anyString(), anyString());
        verify(serviceTypeRepository, never()).save(any());
    }

    @Test
    void findOrCreateServiceType_argumentNullCode_shouldReturnNull() {
        ServiceTypeEntity serviceType = new ServiceTypeEntity();
        serviceType.setId("SVC-ID1");
        serviceType.setName("Service Type");
        serviceType.setDescription("Service Type Description");

        String organizationId = "ORG1";
        serviceType.setOrganizationId(organizationId);
        ServiceTypeEntity serviceTypeEntity = serviceTypeService.findOrCreateServiceType(serviceType);

        assertThat(serviceTypeEntity).isNull();

        verify(serviceTypeRepository, never()).findByCodeAndOrganizationId(anyString(), anyString());
        verify(serviceTypeRepository, never()).save(any());
    }

    @Test
    void findOrCreateServiceType_argumentNullOrganization_shouldReturnNull() {
        ServiceTypeEntity serviceType = new ServiceTypeEntity();
        serviceType.setId("SVC-ID1");
        String svcCode = " ";
        serviceType.setCode(svcCode);
        serviceType.setName("Service Type");
        serviceType.setDescription("Service Type Description");

        ServiceTypeEntity serviceTypeEntity = serviceTypeService.findOrCreateServiceType(serviceType);

        assertThat(serviceTypeEntity).isNull();

        verify(serviceTypeRepository, never()).findByCodeAndOrganizationId(anyString(), anyString());
        verify(serviceTypeRepository, never()).save(any());
    }

    @Test
    void findOrCreateServiceType_saveThrowsException_shouldReturnExistingServiceType() {
        ServiceTypeEntity serviceType = new ServiceTypeEntity();
        serviceType.setId("SVC-ID1");
        String svcCode = "SVC-CODE1";
        serviceType.setCode(svcCode);
        serviceType.setName("Service Type");
        serviceType.setDescription("Service Type Description");

        String organizationId = "ORG1";
        serviceType.setOrganizationId(organizationId);

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(organizationId);
        when(serviceTypeRepository.findByCodeAndOrganizationId(svcCode, organizationId)).thenReturn(Optional.empty())
                .thenReturn(Optional.of(serviceType));
        doThrow(new ConstraintViolationException("Not an exception. Test Only.", new SQLException(), "none"))
                .when(serviceTypeRepository).save(any(ServiceTypeEntity.class));

        ServiceTypeEntity serviceTypeEntity = serviceTypeService.findOrCreateServiceType(serviceType);

        assertThat(serviceTypeEntity).isEqualTo(serviceType);

        verify(serviceTypeRepository, times(2)).findByCodeAndOrganizationId(svcCode, organizationId);
        verify(serviceTypeRepository, times(1)).save(serviceType);
    }

    @Test
    void findPageableServiceTypesByFilter_FilterHasKey_ShouldCallfindByOrgIdAndNameContaining() {
        Filter filter = new Filter();
        OrganizationEntity org = new OrganizationEntity();
        org.setId("ORG1");
        String key = "KEY";
        filter.setKey(key);
        filter.setPage(1);
        filter.setPerPage(10);

        serviceTypeService.findPageableServiceTypesByFilter(filter);

        verify(serviceTypeRepository, times(1)).findByOrganizationIdAndNameContaining(any(), any(), any());
    }

    @Test
    void findPageableServiceTypesByFilter_FilterHasNoKey_ShouldCallfindByOrgId() {
        Filter filter = new Filter();
        OrganizationEntity org = new OrganizationEntity();
        org.setId("ORG1");
        filter.setKey(null);
        filter.setPage(1);
        filter.setPerPage(10);

        serviceTypeService.findPageableServiceTypesByFilter(filter);

        verify(serviceTypeRepository, times(1)).findByOrganizationId(any(), any());
    }

    @Test
    void givenIdsProvided_findAllById_InvokeRepositoryFind() {
        Set<String> ids = Set.of("1", "2");
        serviceTypeService.findAllByIds(ids);

        verify(serviceTypeRepository, times(1)).findAllById(ids);
    }

    @Test
    void givenNullParams_findAllById_ReturnEmptyCollection() {
        assertThat(serviceTypeService.findAllByIds(null)).isEmpty();

        verifyNoInteractions(serviceTypeRepository);
    }

    @Test
    void findPageableServiceTypesByFilterForNetworkLane_FilterHasKey_ShouldCallFindByOrganizationIdAndNameContainingForNetworkLane() {
        Filter filter = new Filter();
        OrganizationEntity org = new OrganizationEntity();
        org.setId("ORG1");
        String key = "KEY";
        filter.setKey(key);
        filter.setPage(1);
        filter.setPerPage(10);

        serviceTypeService.findPageableServiceTypesByFilterForNetworkLane(filter);

        verify(serviceTypeRepository, times(1)).findByOrganizationIdAndNameContainingForNetworkLane(any(), any(), any());
    }

    @Test
    void findPageableServiceTypesByFilterForNetworkLane_FilterHasNoKey_ShouldCallFindByOrganizationIdForNetworkLane() {
        Filter filter = new Filter();
        OrganizationEntity org = new OrganizationEntity();
        org.setId("ORG1");
        filter.setKey(null);
        filter.setPage(1);
        filter.setPerPage(10);

        serviceTypeService.findPageableServiceTypesByFilterForNetworkLane(filter);

        verify(serviceTypeRepository, times(1)).findByOrganizationIdForNetworkLane(any(), any());
    }
}
