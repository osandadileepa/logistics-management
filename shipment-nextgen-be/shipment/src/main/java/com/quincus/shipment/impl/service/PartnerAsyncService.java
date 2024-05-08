package com.quincus.shipment.impl.service;

import com.quincus.qportal.api.QPortalApi;
import com.quincus.shipment.api.domain.Partner;
import com.quincus.shipment.impl.mapper.PartnerMapper;
import com.quincus.shipment.impl.mapper.qportal.QPortalPartnerMapper;
import com.quincus.shipment.impl.repository.PartnerRepository;
import com.quincus.shipment.impl.repository.entity.PartnerEntity;
import com.quincus.web.common.exception.model.QuincusException;
import com.quincus.web.common.exception.model.QuincusValidationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
@Transactional(readOnly = true)
public class PartnerAsyncService {
    private static final String ERROR_UNABLE_TO_RETRIEVE_PARTNER_INFORMATION = "Unable to retrieve information for Partner Name %s";
    private static final String ERROR_PARTNER_NOT_FOUND = "Partner `%s` is not valid";
    private final PartnerRepository partnerRepository;
    private final QPortalApi qPortalApi;
    private final AddressAsyncService addressAsyncService;
    private final QPortalPartnerMapper qPortalPartnerMapper;

    @Transactional
    public PartnerEntity findOrCreatePartnerByName(String partnerName, String organizationId) {
        Optional<PartnerEntity> existingPartnerEntity = findByNameIgnoreCaseAndOrganizationId(partnerName, organizationId);
        if (existingPartnerEntity.isPresent()) return existingPartnerEntity.get();

        Partner partner = getPartnerDetailsFromQPortal(partnerName, organizationId);
        if (partner == null) {
            throw new QuincusValidationException(String.format(ERROR_PARTNER_NOT_FOUND, partnerName));
        }

        PartnerEntity partnerEntity = PartnerMapper.INSTANCE.mapDomainToEntity(partner);
        partnerEntity.setAddress(addressAsyncService.createAddressEntityWithFacility(partner.getAddress(), partner.getName(), organizationId));
        return partnerRepository.save(partnerEntity);
    }

    private Optional<PartnerEntity> findByNameIgnoreCaseAndOrganizationId(String partnerName, String organizationId) {
        return partnerRepository.findByNameIgnoreCaseAndOrganizationId(partnerName, organizationId);
    }

    private Partner getPartnerDetailsFromQPortal(String partnerName, String organizationId) {
        if (StringUtils.isBlank(partnerName)) return null;
        try {
            Partner partner = qPortalPartnerMapper.toPartner(qPortalApi.getPartnerByName(organizationId, partnerName));
            if (partner == null) {
                return null;
            }
            partner.setOrganizationId(organizationId);
            return partner;
        } catch (Exception e) {
            throw new QuincusException(String.format(ERROR_UNABLE_TO_RETRIEVE_PARTNER_INFORMATION, partnerName));
        }
    }
}