package com.quincus.shipment.impl.api;

import com.quincus.shipment.api.PartnerApi;
import com.quincus.shipment.api.domain.Partner;
import com.quincus.shipment.impl.mapper.PartnerMapper;
import com.quincus.shipment.impl.service.PartnerService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PartnerApiImpl implements PartnerApi {
    private PartnerService partnerService;

    @Override
    public Partner findByIdAndOrganizationId(String partnerId, String organizationId) {
        return PartnerMapper.INSTANCE.mapEntityToDomain(partnerService.findByIdAndOrganizationId(partnerId, organizationId).orElse(null));
    }
}
