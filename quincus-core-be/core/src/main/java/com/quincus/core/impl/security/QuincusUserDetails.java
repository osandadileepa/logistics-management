package com.quincus.core.impl.security;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface QuincusUserDetails extends UserDetails {
    String getId();

    String getFullName();

    String getOrganizationName();

    String getOrganizationId();

    List<QuincusLocationCoverage> getLocationCoverages();

    QuincusUserLocation getUserLocation();

    List<QuincusUserPartner> getUserPartners();
}
