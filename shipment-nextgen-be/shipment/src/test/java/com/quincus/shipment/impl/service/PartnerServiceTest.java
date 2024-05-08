package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Partner;
import com.quincus.shipment.impl.repository.PartnerRepository;
import com.quincus.shipment.impl.repository.entity.AddressEntity;
import com.quincus.shipment.impl.repository.entity.PartnerEntity;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartnerServiceTest {
    @InjectMocks
    private PartnerService partnerService;
    @Mock
    private PartnerRepository partnerRepository;
    @Mock
    private QPortalService qPortalService;
    @Mock
    private AddressService addressService;
    @Mock
    private UserDetailsProvider userDetailsProvider;
    @Captor
    private ArgumentCaptor<PartnerEntity> partnerEntityArgumentCaptor;

    @Test
    void givenPartnerExternalId_CreateAndSavePartnerFromQPortal_generatePartnerFromQportalAndSave() {
        String partnerExternalId = "p1";
        Address partnerAddress = new Address();

        Partner partnerFromQPortal = new Partner();
        partnerFromQPortal.setName("partnerName");
        partnerFromQPortal.setId(partnerExternalId);
        partnerFromQPortal.setAddress(partnerAddress);

        when(qPortalService.getPartnerById(partnerExternalId)).thenReturn(partnerFromQPortal);
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("orgId");
        when(addressService.createAddressEntityWithFacility(partnerAddress, "partnerName")).thenReturn(new AddressEntity());

        partnerService.createAndSavePartnerFromQPortal(partnerExternalId);

        verify(partnerRepository).save(partnerEntityArgumentCaptor.capture());
        assertThat(partnerEntityArgumentCaptor.getValue()).isNotNull();
        assertThat(partnerEntityArgumentCaptor.getValue().getAddress()).isNotNull();
    }
}