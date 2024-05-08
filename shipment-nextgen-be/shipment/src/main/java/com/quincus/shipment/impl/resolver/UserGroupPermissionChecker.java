package com.quincus.shipment.impl.resolver;

import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.exception.UserGroupNotAllowedException;
import com.quincus.shipment.impl.repository.entity.CostEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.web.common.multitenant.QuincusUserPartner;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class UserGroupPermissionChecker {

    private static final String ERR_NOT_ALLOWED = "Current user does not meet the required user group to access or modify this record.";

    private final UserDetailsProvider userDetailsProvider;

    public void checkUserGroupPermissions(String shpPartnerId) {
        if (userDetailsProvider.isFromPreAuthenticatedSource()) return;

        String shipmentPartnerId = StringUtils.isEmpty(shpPartnerId) ? null : shpPartnerId;
        String partnerId = userDetailsProvider.getCurrentPartnerId();
        List<QuincusUserPartner> userPartners = userDetailsProvider.getCurrentUserPartners();

        List<String> allowedPartnerIds = userPartners.stream().map(QuincusUserPartner::getPartnerId)
                .collect(Collectors.toCollection(ArrayList::new));
        //null partner ID is considered valid and means the user is a company user
        allowedPartnerIds.add(partnerId);

        if (shipmentPartnerId != null) {
            if (allowedPartnerIds.stream().noneMatch(shipmentPartnerId::equals)) {
                throw new UserGroupNotAllowedException(ERR_NOT_ALLOWED);
            }
        } else {
            if (partnerId != null) {
                throw new UserGroupNotAllowedException(ERR_NOT_ALLOWED);
            }
        }
    }

    public void checkUserGroupPermissions(CostEntity costEntity) {
        checkUserGroupPermissions(costEntity.getPartnerId());
    }

    public void checkUserGroupPermissions(ShipmentEntity shipmentEntity) {
        checkUserGroupPermissions(shipmentEntity.getPartnerId());
    }

    public void checkUserGroupPermissions(Shipment shipment) {
        checkUserGroupPermissions(shipment.getPartnerId());
    }
}
