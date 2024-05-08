package com.quincus.shipment.impl.resolver;

import com.quincus.shipment.api.constant.Source;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.web.common.multitenant.QuincusLocationCoverage;
import com.quincus.web.common.multitenant.QuincusUserDetails;
import com.quincus.web.common.multitenant.QuincusUserLocation;
import com.quincus.web.common.multitenant.QuincusUserPartner;
import com.quincus.web.common.multitenant.UserDetailsContextHolder;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public final class UserDetailsProvider {
    private static final String ORDERS = "/orders";
    private final UserDetailsContextHolder contextHolder;

    public Organization getCurrentOrganization() {
        Organization organization = new Organization(contextHolder.getCurrentOrganizationId());
        organization.setName(contextHolder.getCurrentOrganizationName());
        return organization;
    }

    public QuincusUserDetails getQuincusUserDetails() {
        return contextHolder.getQuincusUserDetails().orElse(null);
    }

    public String getCurrentOrganizationId() {
        return contextHolder.getCurrentOrganizationId();
    }

    public String getCurrentPartnerId() {
        return contextHolder.getCurrentPartnerId();
    }

    public String getCurrentUserId() {
        return contextHolder.getCurrentUserId();
    }

    public String getSource() {
        return contextHolder.getCurrentSource();
    }

    public String getCurrentUserFullName() {
        return contextHolder.getCurrentUserFullName();
    }

    public List<QuincusLocationCoverage> getCurrentLocationCoverages() {
        return contextHolder.getCurrentLocationCoverages();
    }

    public List<String> getCurrentLocationCoverageIds() {
        return contextHolder.getCurrentLocationCoverages().stream().map(QuincusLocationCoverage::getId).toList();
    }

    public List<QuincusUserPartner> getCurrentUserPartners() {
        return contextHolder.getCurrentUserPartners();
    }

    private boolean isCurrentUriFromOrder() {
        return StringUtils.endsWithIgnoreCase(contextHolder.getCurrentRequestURI(), ORDERS);
    }

    public boolean isFromKafka() {
        return StringUtils.equalsIgnoreCase(Source.KAFKA.name(), getSource());
    }

    private boolean isFrom3rdPartyServer() {
        return StringUtils.equalsIgnoreCase(Source.S2S.name(), getSource());
    }

    public boolean isFromAllowedSource() {
        return isCurrentUriFromOrder() || isFromKafka() || isFrom3rdPartyServer();
    }

    public boolean isFromPreAuthenticatedSource() {
        return isFromKafka() || isFrom3rdPartyServer();
    }

    public Map<String, Set<QuincusLocationCoverage>> getCurrentLocationCoverageMap() {
        return Optional.ofNullable(getCurrentLocationCoverages())
                .orElse(Collections.emptyList()).stream()
                .collect(Collectors.groupingBy(
                        locationCoverage -> locationCoverage.getLocationType().getName(),
                        Collectors.toSet()
                ));
    }

    public Set<QuincusLocationCoverage> getFacilitySpecificCoverage() {
        return getCurrentLocationCoverageMap().getOrDefault(QuincusLocationCoverage.LOCATION_TYPE_FACILITY,
                Collections.emptySet());
    }

    public Set<QuincusLocationCoverage> getCityWideCoverage() {
        return getCurrentLocationCoverageMap().getOrDefault(QuincusLocationCoverage.LOCATION_TYPE_CITY,
                Collections.emptySet());
    }

    public Set<QuincusLocationCoverage> getStateWideCoverage() {
        return getCurrentLocationCoverageMap().getOrDefault(QuincusLocationCoverage.LOCATION_TYPE_STATE,
                Collections.emptySet());
    }

    public Set<QuincusLocationCoverage> getCountryWideCoverage() {
        return getCurrentLocationCoverageMap().getOrDefault(QuincusLocationCoverage.LOCATION_TYPE_COUNTRY,
                Collections.emptySet());
    }

    public Set<String> getFacilityIdCoverages() {
        return getLocationIdCoverages(getFacilitySpecificCoverage());
    }

    public Set<String> getCityIdCoverages() {
        return getLocationIdCoverages(getCityWideCoverage());
    }

    public Set<String> getStateIdCoverages() {
        return getLocationIdCoverages(getStateWideCoverage());
    }

    public Set<String> getCountryIdCoverages() {
        return getLocationIdCoverages(getCountryWideCoverage());
    }

    public QuincusUserLocation getUserCurrentLocation() {
        return contextHolder.getUserCurrentLocation();
    }

    private Set<String> getLocationIdCoverages(Set<QuincusLocationCoverage> locationCoverages) {
        return locationCoverages.stream()
                .map(QuincusLocationCoverage::getId)
                .collect(Collectors.toSet());
    }
}