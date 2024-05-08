package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.impl.repository.entity.PartnerEntity;
import com.quincus.shipment.impl.repository.entity.component.MultiTenantEntity_;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.persistence.Tuple;

import static com.quincus.shipment.impl.repository.constant.NetworkLaneSegmentTupleAlias.PARTNER_CODE;
import static com.quincus.shipment.impl.repository.constant.NetworkLaneSegmentTupleAlias.PARTNER_EXTERNAL_ID;
import static com.quincus.shipment.impl.repository.constant.NetworkLaneSegmentTupleAlias.PARTNER_ID;
import static com.quincus.shipment.impl.repository.constant.NetworkLaneSegmentTupleAlias.PARTNER_NAME;
import static com.quincus.shipment.impl.repository.constant.NetworkLaneSegmentTupleAlias.PARTNER_TYPE;

@Component
public class NetworkLaneSegmentPartnerTupleMapper {
    public PartnerEntity toEntity(Tuple tuple) {
        String id = tuple.get(PARTNER_ID, String.class);
        if (StringUtils.isBlank(id)) {
            return null;
        }
        PartnerEntity partner = new PartnerEntity();
        partner.setId(id);
        partner.setCode(tuple.get(PARTNER_CODE, String.class));
        partner.setPartnerType(tuple.get(PARTNER_TYPE, String.class));
        partner.setName(tuple.get(PARTNER_NAME, String.class));
        partner.setOrganizationId(tuple.get(MultiTenantEntity_.ORGANIZATION_ID, String.class));
        partner.setExternalId(tuple.get(PARTNER_EXTERNAL_ID, String.class));
        return partner;
    }
}
