package com.quincus.web.common.multitenant;

import com.quincus.web.common.exception.model.OrganizationDetailsNotFoundException;
import com.quincus.web.common.exception.model.UserDetailsNotFoundException;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class UserDetailsContextHolder {
    private static final String ERR_UNABLE_TO_RESOLVE_ORGANIZATION_CONTEXT = "Unable to resolve Organization context.";
    private static final String ERR_UNABLE_TO_RESOLVE_USER_PROFILE_CONTEXT = "Unable to resolve UserProfile context.";
    
    public Optional<QuincusUserDetails> getQuincusUserDetails() {
        return SecurityContextUtil.getQuincusUserDetails();
    }

    public String getCurrentOrganizationId() {
        return getQuincusUserDetails()
                .map(QuincusUserDetails::getOrganizationId)
                .orElseThrow(() -> new OrganizationDetailsNotFoundException(ERR_UNABLE_TO_RESOLVE_ORGANIZATION_CONTEXT));
    }

    public String getCurrentOrganizationName() {
        return getQuincusUserDetails()
                .map(QuincusUserDetails::getOrganizationName)
                .orElseThrow(() -> new OrganizationDetailsNotFoundException(ERR_UNABLE_TO_RESOLVE_ORGANIZATION_CONTEXT));
    }

    public String getCurrentPartnerId() {
        return getQuincusUserDetails()
                .map(QuincusUserDetails::getPartnerId)
                .filter(StringUtils::isNotBlank)
                .orElse(null);
    }

    public String getCurrentPartnerName() {
        return getQuincusUserDetails()
                .map(QuincusUserDetails::getPartnerName)
                .orElse(null);
    }

    public String getCurrentUserFullName() {
        return getQuincusUserDetails()
                .map(QuincusUserDetails::getFullName)
                .orElseThrow(() -> new UserDetailsNotFoundException(ERR_UNABLE_TO_RESOLVE_USER_PROFILE_CONTEXT));
    }

    public String getCurrentUserId() {
        return getQuincusUserDetails()
                .map(QuincusUserDetails::getId)
                .orElseThrow(() -> new UserDetailsNotFoundException(ERR_UNABLE_TO_RESOLVE_USER_PROFILE_CONTEXT));
    }

    public List<QuincusLocationCoverage> getCurrentLocationCoverages() {
        return getQuincusUserDetails()
                .map(QuincusUserDetails::getLocationCoverages)
                .orElse(Collections.emptyList());
    }

    public List<QuincusUserPartner> getCurrentUserPartners() {
        return getQuincusUserDetails()
                .map(QuincusUserDetails::getUserPartners)
                .orElse(Collections.emptyList());
    }

    public String getCurrentRequestURI() {
        return SecurityContextUtil.getRequestURI();
    }

    public QuincusUserLocation getUserCurrentLocation() {
        return getQuincusUserDetails()
                .map(QuincusUserDetails::getUserLocation)
                .orElse(new QuincusUserLocation());
    }

    public String getCurrentSource() {
        return getQuincusUserDetails()
                .map(QuincusUserDetails::getSource)
                .orElse(Strings.EMPTY);
    }
}
