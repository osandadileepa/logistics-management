package com.quincus.web.security;

import com.quincus.qportal.api.QPortalApi;
import com.quincus.qportal.model.QPortalPermission;
import com.quincus.qportal.model.QPortalRole;
import com.quincus.qportal.model.QPortalUser;
import com.quincus.web.common.multitenant.QuincusUser;
import com.quincus.web.common.multitenant.QuincusUserDetails;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Component
public class AuthenticationProvider {

    private static final String PERMISSION_NAME = "network_management";
    private static final String PERMISSION_VERSION = "v1";
    private static final String REQUEST_URI = "requestURI";
    private final PassThroughUserDetailsService userDetailsService;
    private final QPortalApi qPortalApi;

    public Authentication authenticateUser(String token, String uri) {
        final QPortalUser profile = qPortalApi.getCurrentUserProfile(token);
        QPortalPermission permission = getModulePermissionOrThrowException(profile.getPermissions());
        final QuincusUserDetails userDetails = userDetailsService.loadUserFromAuth(
                profile,
                createAuthorities(profile.getRoles(), permission)
        );
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        authentication.setDetails(Map.of(REQUEST_URI, uri));
        return authentication;
    }

    public Authentication authenticatePreAuthenticatedUser(String organizationId, String preAuthenticatedUser, String userName) {
        final QuincusUserDetails quincusUserDetails = new QuincusUser(
                preAuthenticatedUser,
                userName,
                organizationId,
                List.of(new SimpleGrantedAuthority(preAuthenticatedUser)));
        return new PreAuthenticatedAuthenticationToken(quincusUserDetails, preAuthenticatedUser, quincusUserDetails.getAuthorities());
    }

    private QPortalPermission getModulePermissionOrThrowException(List<QPortalPermission> permissions) {
        return permissions.stream()
                .filter(p -> PERMISSION_NAME.equalsIgnoreCase(p.getName()) && PERMISSION_VERSION.equalsIgnoreCase(p.getVersion()))
                .findFirst()
                // todo revert to throw error once permissions are configured
                .orElse(null);
    }

    private List<SimpleGrantedAuthority> createAuthorities(List<QPortalRole> roles, QPortalPermission permission) {
        List<SimpleGrantedAuthority> authorities = createAuthoritiesFromRoles(roles);
        authorities.addAll(createAuthoritiesFromPermissions(permission));
        return authorities;
    }

    private List<SimpleGrantedAuthority> createAuthoritiesFromRoles(List<QPortalRole> roles) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        roles.forEach(r -> authorities.add(new SimpleGrantedAuthority(r.getName())));
        return authorities;
    }

    private List<SimpleGrantedAuthority> createAuthoritiesFromPermissions(QPortalPermission permission) {
        if (permission == null) {
            return Collections.emptyList();
        }
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        permission.getPages().forEach(page ->
                page.getActions().forEach(action ->
                        authorities.add(new SimpleGrantedAuthority((page.getName() + "_" + action.getName()).toUpperCase()))
                ));
        return authorities;
    }
}
