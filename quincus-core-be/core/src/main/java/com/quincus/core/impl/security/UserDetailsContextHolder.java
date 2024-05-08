package com.quincus.core.impl.security;

import com.quincus.core.impl.exception.OrganizationDetailsNotFoundException;
import com.quincus.core.impl.exception.UserDetailsNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequestScope
@AllArgsConstructor
public class UserDetailsContextHolder {
    private static final String ERR_UNABLE_TO_RESOLVE_ORGANIZATION_CONTEXT = "Unable to resolve Organization context.";
    private static final String ERR_UNABLE_TO_RESOLVE_USER_PROFILE_CONTEXT = "Unable to resolve UserProfile context.";
    private static final String REQUEST_URI = "requestURI";

    public Optional<QuincusUserDetails> getQuincusUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof QuincusUserDetails quincusUserDetails) {
            return Optional.of(quincusUserDetails);
        }
        return Optional.empty();
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getDetails() instanceof Map)) return null;
        return ((Map<?, ?>) authentication.getDetails()).get(REQUEST_URI).toString();
    }

    public QuincusUserLocation getUserCurrentLocation() {
        return getQuincusUserDetails()
                .map(QuincusUserDetails::getUserLocation)
                .orElse(new QuincusUserLocation());
    }

}
