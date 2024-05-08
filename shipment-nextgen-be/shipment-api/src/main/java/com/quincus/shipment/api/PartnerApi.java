package com.quincus.shipment.api;

import com.quincus.shipment.api.domain.Partner;

public interface PartnerApi {
    Partner findByIdAndOrganizationId(String partnerId, String organizationId);
}
