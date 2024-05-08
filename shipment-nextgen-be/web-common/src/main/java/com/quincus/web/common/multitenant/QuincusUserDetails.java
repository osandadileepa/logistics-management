package com.quincus.web.common.multitenant;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface QuincusUserDetails extends UserDetails {
    String getId();

    String getFullName();

    String getOrganizationName();

    String getOrganizationId();

    String getPartnerName();

    String getPartnerId();

    List<QuincusLocationCoverage> getLocationCoverages();

    QuincusUserLocation getUserLocation();

    List<QuincusUserPartner> getUserPartners();

    String getSource();
}
