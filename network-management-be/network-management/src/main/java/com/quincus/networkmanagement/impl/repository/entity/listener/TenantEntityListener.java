package com.quincus.networkmanagement.impl.repository.entity.listener;

import com.quincus.networkmanagement.impl.repository.entity.component.TenantEntity;
import com.quincus.web.common.exception.model.QuincusException;
import com.quincus.web.common.multitenant.UserDetailsContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

@Slf4j
public class TenantEntityListener {

    private static final String ORGANIZATION_ID_MISMATCH = "The current organization (%s) does not match the organization of this entity (%s).";

    private final UserDetailsContextHolder userDetailsContextHolder = new UserDetailsContextHolder();

    @PrePersist
    @PreUpdate
    private void onPersist(TenantEntity tenantEntity) {
        tenantEntity.setOrganizationId(userDetailsContextHolder.getCurrentOrganizationId());
    }

    @PreRemove
    private void onModification(TenantEntity tenantEntity) {
        String currentOrganizationId = userDetailsContextHolder.getCurrentOrganizationId();
        if (!StringUtils.equals(currentOrganizationId, tenantEntity.getOrganizationId())) {
            String warningMessage = String.format(ORGANIZATION_ID_MISMATCH, currentOrganizationId, tenantEntity.getOrganizationId());
            log.warn(warningMessage);
            throw new QuincusException(warningMessage);
        }
    }

}