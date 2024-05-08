package com.quincus.web.security;

import com.quincus.core.impl.security.QuincusUser;
import com.quincus.core.impl.security.QuincusUserDetails;
import com.quincus.qportal.api.QPortalApi;
import com.quincus.qportal.model.QPortalPermission;
import com.quincus.qportal.model.QPortalRole;
import com.quincus.qportal.model.QPortalUser;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Component
public class AuthenticationProvider {

    private static final String SHPV2_PERMISSION_NAME = "shipment";
    private static final String SHPV2_PERMISSION_VERSION = "v2";
    private static final String REQUEST_URI = "requestURI";
    private final PassThroughUserDetailsService userDetailsService;
    private final QPortalApi qPortalApi;

    public Authentication authenticateUser(String token, String uri) {
        final QPortalUser profile = qPortalApi.getCurrentUserProfile(token);
        final QuincusUserDetails userDetails = userDetailsService.loadUserFromAuth(
                profile,
                createAuthorities(profile.getRoles(), profile.getPermissions())
        );
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        authentication.setDetails(Map.of(REQUEST_URI, uri));
        return authentication;
    }

    public Authentication authenticatePreAuthenticatedUser(String organizationId, String preAuthenticatedUser) {
        final QuincusUserDetails quincusUserDetails = new QuincusUser(
                preAuthenticatedUser,
                organizationId,
                List.of(new SimpleGrantedAuthority(preAuthenticatedUser)));
        return new PreAuthenticatedAuthenticationToken(quincusUserDetails, preAuthenticatedUser, quincusUserDetails.getAuthorities());
    }

    private List<SimpleGrantedAuthority> createAuthorities(List<QPortalRole> roles, List<QPortalPermission> permissions) {
        List<SimpleGrantedAuthority> authorities = createAuthoritiesFromRoles(roles);
        authorities.addAll(createAuthoritiesFromPermissions(permissions));
        return authorities;
    }

    private List<SimpleGrantedAuthority> createAuthoritiesFromRoles(List<QPortalRole> roles) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        roles.forEach(r -> authorities.add(new SimpleGrantedAuthority(r.getName())));
        return authorities;
    }

    private List<SimpleGrantedAuthority> createAuthoritiesFromPermissions(List<QPortalPermission> permissions) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        permissions.stream()
                .filter(p -> SHPV2_PERMISSION_NAME.equals(p.getName()) && SHPV2_PERMISSION_VERSION.equals(p.getVersion()))
                .findFirst()
                .ifPresent(shpV2Permission -> shpV2Permission.getPages().forEach(page ->
                        page.getActions().forEach(action ->
                                authorities.add(new SimpleGrantedAuthority((page.getName() + "_" + action.getName()).toUpperCase()))
                        )
                ));
        return authorities;
    }

}
