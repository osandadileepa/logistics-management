package com.quincus.web.common.multitenant;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@SuppressWarnings("java:S107")
public class QuincusUser extends User implements QuincusUserDetails {
    private final String id;
    private final String fullName;
    private final String organizationName;
    private final String organizationId;
    private final String partnerName;
    private final String partnerId;
    private final List<QuincusLocationCoverage> locationCoverages;
    private final List<QuincusUserPartner> userPartners;
    private QuincusUserLocation userLocation;
    private final String source;

    public QuincusUser(
            String username,
            String id,
            String fullName,
            String organizationId,
            String organizationName,
            String partnerId,
            String partnerName,
            Collection<? extends GrantedAuthority> authorities,
            List<QuincusLocationCoverage> locationCoverages,
            QuincusUserLocation userLocation,
            List<QuincusUserPartner> userPartners,
            String source
    ) {
        super(username, UUID.randomUUID().toString(), authorities);
        this.id = id;
        this.fullName = fullName;
        this.organizationId = organizationId;
        this.organizationName = organizationName;
        this.partnerId = partnerId;
        this.partnerName = partnerName;
        this.locationCoverages = Optional.ofNullable(locationCoverages).orElse(Collections.emptyList());
        this.userLocation = userLocation;
        this.userPartners = Optional.ofNullable(userPartners).orElse(Collections.emptyList());
        this.source = source;
    }

    public QuincusUser(
            String id,
            String username,
            String source,
            String organizationId,
            Collection<? extends GrantedAuthority> authorities) {
        super(username, UUID.randomUUID().toString(), authorities);
        this.id = id;
        this.fullName = username;
        this.source = source;
        this.organizationId = organizationId;
        this.organizationName = StringUtils.EMPTY;
        this.partnerId = null;
        this.partnerName = null;
        this.locationCoverages = Collections.emptyList();
        this.userPartners = Collections.emptyList();
    }
}
