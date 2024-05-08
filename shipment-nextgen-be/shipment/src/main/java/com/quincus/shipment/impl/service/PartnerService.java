package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.domain.Partner;
import com.quincus.shipment.impl.mapper.PartnerMapper;
import com.quincus.shipment.impl.repository.PartnerRepository;
import com.quincus.shipment.impl.repository.entity.PartnerEntity;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.web.common.exception.model.QuincusValidationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
@Transactional(readOnly = true)
public class PartnerService {
    private static final String ERROR_UNABLE_TO_RETRIEVE_PARTNER_INFORMATION = "Unable to retrieve information for Partner ID {}";
    private final PartnerRepository partnerRepository;
    private final QPortalService qPortalService;
    private final AddressService addressService;
    private final UserDetailsProvider userDetailsProvider;


    public PartnerEntity findOrCreatePartner(String partnerExternalId) {

        Optional<PartnerEntity> existingPartnerEntity = findByExternalId(partnerExternalId);
        if (existingPartnerEntity.isPresent()) return existingPartnerEntity.get();

        PartnerEntity partnerEntity = new PartnerEntity();
        Partner partner = getPartnerDetailsFromQPortal(partnerExternalId);
        if (partner == null) {
            partnerEntity.setOrganizationId(userDetailsProvider.getCurrentOrganizationId());
            partnerEntity.setExternalId(partnerExternalId);
            return partnerRepository.save(partnerEntity);
        }
        partnerEntity = PartnerMapper.INSTANCE.mapDomainToEntity(partner);
        partnerEntity.setAddress(addressService.createAddressEntityWithFacility(partner.getAddress(), partner.getName()));
        return partnerRepository.save(partnerEntity);
    }

    public Optional<PartnerEntity> findByExternalId(String partnerId) {
        String organizationId = userDetailsProvider.getCurrentOrganizationId();
        return partnerRepository.findByExternalIdAndOrganizationId(partnerId, organizationId);
    }

    public List<PartnerEntity> findAllByExternalIds(List<String> partnerExternalIds) {
        String organizationId = userDetailsProvider.getCurrentOrganizationId();
        return partnerRepository.findByExternalIdInAndOrganizationId(partnerExternalIds, organizationId);
    }

    public PartnerEntity createAndSavePartnerFromQPortal(String externalId) {

        Partner partner = getPartnerDetailsFromQPortal(externalId);
        if (partner == null) {
            throw new QuincusValidationException(String.format("Invalid Partner Id: %s", externalId));
        }
        PartnerEntity partnerEntity = PartnerMapper.INSTANCE.mapDomainToEntity(partner);
        partnerEntity.setOrganizationId(userDetailsProvider.getCurrentOrganizationId());
        partnerEntity.setAddress(addressService.createAddressEntityWithFacility(partner.getAddress(), partner.getName()));
        return partnerRepository.save(partnerEntity);
    }

    public Optional<PartnerEntity> findByIdAndOrganizationId(String partnerId, String organizationId) {
        return partnerRepository.findByExternalIdAndOrganizationId(partnerId, organizationId);
    }

    private Partner getPartnerDetailsFromQPortal(String partnerId) {
        if (StringUtils.isBlank(partnerId)) return null;
        try {
            Partner partner = qPortalService.getPartnerById(partnerId);
            partner.setOrganizationId(userDetailsProvider.getCurrentOrganizationId());
            return partner;
        } catch (Exception e) {
            log.error(ERROR_UNABLE_TO_RETRIEVE_PARTNER_INFORMATION, partnerId);
            return null;
        }
    }
}