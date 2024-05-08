package com.quincus.shipment.impl.repository.listeners;

import com.quincus.shipment.impl.repository.entity.component.MultiTenantEntity;
import com.quincus.web.common.multitenant.QuincusUserDetails;
import com.quincus.web.common.multitenant.SecurityContextUtil;

import javax.persistence.PrePersist;

public class MultiTenantEntityListener {
    
    @PrePersist
    private void onPersist(MultiTenantEntity tenantEntity) {
        SecurityContextUtil.getQuincusUserDetails()
                .map(QuincusUserDetails::getOrganizationId)
                .ifPresent(tenantEntity::setOrganizationId);
    }

}
