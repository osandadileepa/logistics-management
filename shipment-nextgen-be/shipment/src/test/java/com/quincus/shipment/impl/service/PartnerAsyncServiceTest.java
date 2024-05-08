package com.quincus.shipment.impl.service;

import com.quincus.qportal.api.QPortalApi;
import com.quincus.qportal.model.QPortalPartner;
import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Partner;
import com.quincus.shipment.impl.mapper.qportal.QPortalPartnerMapper;
import com.quincus.shipment.impl.repository.PartnerRepository;
import com.quincus.shipment.impl.repository.entity.AddressEntity;
import com.quincus.shipment.impl.repository.entity.PartnerEntity;
import com.quincus.web.common.exception.model.QuincusException;
import com.quincus.web.common.exception.model.QuincusValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartnerAsyncServiceTest {

    @Mock
    private PartnerRepository partnerRepository;
    @Mock
    private QPortalApi qPortalApi;
    @Mock
    private AddressAsyncService addressAsyncService;
    @Mock
    private QPortalPartnerMapper qPortalPartnerMapper;
    @InjectMocks
    private PartnerAsyncService partnerAsyncService;

    @Test
    void givenPartnerDataExistingWhenFindOrCreatePartnerByNameThenNoNeedToCreate() {
        // GIVEN:
        String partnerName = "Existing Partner";
        String organizationId = "123456";
        PartnerEntity existingPartnerEntity = new PartnerEntity();
        when(partnerRepository.findByNameIgnoreCaseAndOrganizationId(partnerName, organizationId))
                .thenReturn(Optional.of(existingPartnerEntity));

        // WHEN:
        PartnerEntity result = partnerAsyncService.findOrCreatePartnerByName(partnerName, organizationId);

        // THEN:
        assertThat(result).isNotNull().isEqualTo(existingPartnerEntity);
        ;
        verify(partnerRepository, times(1)).findByNameIgnoreCaseAndOrganizationId(partnerName, organizationId);
        verifyNoMoreInteractions(partnerRepository);
        verifyNoInteractions(qPortalApi, addressAsyncService, qPortalPartnerMapper);
    }

    @Test
    void givenNonExstingPartnerDataWhenFindOrCreatePartnerByNameAndParnterNotExistInQPortalThenThrowQuincusException() {
        // GIVEN:
        String partnerName = "Non-Existing Partner";
        String organizationId = "123456";
        when(partnerRepository.findByNameIgnoreCaseAndOrganizationId(partnerName, organizationId))
                .thenReturn(Optional.empty());
        when(qPortalApi.getPartnerByName(organizationId, partnerName)).thenReturn(null);

        // WHEN: THEN:
        assertThatThrownBy(() -> partnerAsyncService.findOrCreatePartnerByName(partnerName, organizationId)).isInstanceOf(QuincusException.class);

        verify(partnerRepository, times(1)).findByNameIgnoreCaseAndOrganizationId(partnerName, organizationId);
        verifyNoMoreInteractions(partnerRepository);
        verifyNoInteractions(addressAsyncService);
    }

    @Test
    void givenNonExstingPartnerDataWhenFindOrCreatePartnerByNameThenCreateDataWithDetailsFromQportal() {
        // GIVEN:
        String partnerName = "Qportal-Existing Partner";
        String organizationId = "123456";

        Address address = new Address();

        Partner partner = new Partner();
        partner.setName(partnerName);
        partner.setOrganizationId(organizationId);
        partner.setAddress(address);

        AddressEntity addressEntity = new AddressEntity();

        QPortalPartner mockQportalPartner = mock(QPortalPartner.class);
        when(partnerRepository.findByNameIgnoreCaseAndOrganizationId(partnerName, organizationId))
                .thenReturn(Optional.empty());
        when(qPortalApi.getPartnerByName(organizationId, partnerName)).thenReturn(mockQportalPartner);
        when(qPortalPartnerMapper.toPartner(mockQportalPartner)).thenReturn(partner);
        when(addressAsyncService.createAddressEntityWithFacility(address, partnerName, organizationId)).thenReturn(addressEntity);

        // WHEN:
        partnerAsyncService.findOrCreatePartnerByName(partnerName, organizationId);

        THEN:
        verify(partnerRepository, times(1)).findByNameIgnoreCaseAndOrganizationId(partnerName, organizationId);
        verify(partnerRepository, times(1)).save(any());
        verify(addressAsyncService, times(1)).createAddressEntityWithFacility(address, partnerName, organizationId);
    }

    @Test
    void givenNoPartnerFromQPortalThenThrowValidationException() {
        // GIVEN:
        String partnerName = "Qportal-Not-Existing Partner";
        String organizationId = "123456";

        Address address = new Address();

        Partner partner = new Partner();
        partner.setName(partnerName);
        partner.setOrganizationId(organizationId);
        partner.setAddress(address);

        AddressEntity addressEntity = new AddressEntity();


        when(partnerRepository.findByNameIgnoreCaseAndOrganizationId(partnerName, organizationId))
                .thenReturn(Optional.empty());
        when(qPortalApi.getPartnerByName(organizationId, partnerName)).thenReturn(null);
        when(qPortalPartnerMapper.toPartner(null)).thenReturn(null);

        // WHEN:
        assertThatThrownBy(() -> partnerAsyncService.findOrCreatePartnerByName(partnerName, organizationId))
                .hasMessage("Partner `" + partnerName + "` is not valid").isInstanceOf(QuincusValidationException.class);

    }
}
