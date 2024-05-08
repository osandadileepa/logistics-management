package com.quincus.web.common.multitenant;

import com.quincus.web.common.exception.model.OrganizationDetailsNotFoundException;
import com.quincus.web.common.exception.model.UserDetailsNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class UserDetailsContextHolder {
    private static final String ERR_UNABLE_TO_RESOLVE_ORGANIZATION_CONTEXT = "Unable to resolve Organization context.";
    private static final String ERR_UNABLE_TO_RESOLVE_USER_PROFILE_CONTEXT = "Unable to resolve UserProfile context.";
    private static final String REQUEST_URI = "requestURI";

    @Value("${spring.profiles.active}")
    private String profile;

    public boolean isQuincusUserDetailsPresent() {
        return getQuincusUserDetails().isPresent();
    }

    public Optional<QuincusUserDetails> getQuincusUserDetails() {
        Authentication authentication = getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof QuincusUserDetails quincusUserDetails) {
            return Optional.of(quincusUserDetails);
        }
        return Optional.empty();
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
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
        Authentication authentication = getAuthentication();
        if (authentication == null || !(authentication.getDetails() instanceof Map)) return null;
        return ((Map<?, ?>) authentication.getDetails()).get(REQUEST_URI).toString();
    }

    public QuincusUserLocation getUserCurrentLocation() {
        return getQuincusUserDetails()
                .map(QuincusUserDetails::getUserLocation)
                .orElse(new QuincusUserLocation());
    }

    public String getCurrentProfile() {
        return profile;
    }
}
