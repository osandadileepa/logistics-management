package com.quincus.web.security;

import com.quincus.qportal.model.QPortalFacility;
import com.quincus.qportal.model.QPortalLocationCoverage;
import com.quincus.qportal.model.QPortalUser;
import com.quincus.qportal.model.QPortalUserPartner;
import com.quincus.web.common.multitenant.QuincusLocationCoverage;
import com.quincus.web.common.multitenant.QuincusUser;
import com.quincus.web.common.multitenant.QuincusUserDetails;
import com.quincus.web.common.multitenant.QuincusUserLocation;
import com.quincus.web.common.multitenant.QuincusUserPartner;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PassThroughUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final GrantedAuthority[] authorities = new GrantedAuthority[]{
                () -> "ADMIN"
        };
        return User
                .withUsername(username)
                .password(UUID.randomUUID().toString())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    public QuincusUserDetails loadUserFromAuth(QPortalUser profile, List<SimpleGrantedAuthority> authorities, String source) {
        return new QuincusUser(
                profile.getUsername(),
                profile.getId(),
                profile.getFullName(),
                profile.getOrganizationId(),
                profile.getOrganizationName(),
                profile.getPartnerId(),
                profile.getPartner(),
                authorities,
                Optional.ofNullable(profile.getLocations())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(this::toLocationCoverage)
                        .toList(),
                toUserLocation(profile),
                profile.getAllUserPartners()
                        .stream()
                        .map(this::toUserPartner)
                        .toList(),
                source
        );
    }

    private QuincusUserLocation toUserLocation(QPortalUser qPortalUser) {
        // get user populates facility while get profile populates userLocation.
        if (qPortalUser.getUserFacility() != null) {
            QPortalFacility qPortalFacility = qPortalUser.getUserFacility();
            return new QuincusUserLocation()
                    .locationId(qPortalFacility.getId())
                    .locationFacilityName(qPortalFacility.getName())
                    .locationCode(qPortalFacility.getLocationCode())
                    .countryId(qPortalFacility.getCountryId())
                    .stateProvinceId(qPortalFacility.getStateProvinceId())
                    .cityId(qPortalFacility.getCityId());
        } else if (qPortalUser.getUserLocation() != null) {
            return new QuincusUserLocation()
                    .locationId(qPortalUser.getUserLocation().getLocationId())
                    .locationFacilityName(qPortalUser.getUserLocation().getLocationFacilityName())
                    .locationCode(qPortalUser.getUserLocation().getLocationCode());
        }
        return null;
    }

    private QuincusLocationCoverage toLocationCoverage(QPortalLocationCoverage coverage) {
        QuincusLocationCoverage locationCoverage = new QuincusLocationCoverage();

        locationCoverage.setId(coverage.getId());
        locationCoverage.setName(coverage.getName());
        locationCoverage.setCountryId(coverage.getCountryId());
        locationCoverage.setStateProvinceId(coverage.getStateProvinceId());
        locationCoverage.setCityId(coverage.getCityId());

        QuincusLocationCoverage.LocationType locationType = new QuincusLocationCoverage.LocationType();
        if (coverage.getLocationType() != null) {
            locationType.setId(coverage.getLocationType().getId());
            locationType.setName(coverage.getLocationType().getName());
            locationCoverage.setLocationType(locationType);
        }

        QuincusLocationCoverage.LocationTag locationTag = new QuincusLocationCoverage.LocationTag();
        if (coverage.getLocationTag() != null) {
            locationTag.setId(coverage.getLocationTag().getId());
            locationTag.setName(coverage.getLocationTag().getName());
            locationCoverage.setLocationTag(locationTag);
        }
        locationCoverage.setAncestors(coverage.getAncestors());
        locationCoverage.setLocationCode(coverage.getLocationCode());
        return locationCoverage;
    }

    private QuincusUserPartner toUserPartner(QPortalUserPartner qPortalUserPartner) {
        QuincusUserPartner userPartner = new QuincusUserPartner();
        userPartner.setPartnerId(qPortalUserPartner.getPartnerId());
        userPartner.setPartnerName(qPortalUserPartner.getPartnerName());
        return userPartner;
    }
}