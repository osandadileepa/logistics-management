package com.quincus.web.security;

import com.quincus.qportal.api.QPortalApi;
import com.quincus.qportal.model.QPortalOrganization;
import com.quincus.qportal.model.QPortalPermission;
import com.quincus.qportal.model.QPortalRole;
import com.quincus.qportal.model.QPortalUser;
import com.quincus.web.common.exception.model.QuincusValidationException;
import com.quincus.web.common.multitenant.QuincusUser;
import com.quincus.web.common.multitenant.QuincusUserDetails;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Component
public class AuthenticationProvider {

    private static final String SHPV2_PERMISSION_NAME = "shipment";
    private static final String SHPV2_PERMISSION_VERSION = "v2";
    private static final String REQUEST_URI = "requestURI";
    private static final String MISSING_SHPV2_PERMISSION = "User does not have 'shipment v2' permission.";
    private static final String NON_SHPV2_ORGANIZATION = "Organization '%s' does not have access to 'shipment v2' module.";
    private static final String SHPV2_USER_NOT_FOUND_ERROR = "User with id '%s' and organizationId %s not found.";
    private final PassThroughUserDetailsService userDetailsService;
    private final QPortalApi qPortalApi;
    private static final String API_SOURCE = "API";

    public Authentication authenticateUser(String token, String uri) {
        final QPortalUser profile = qPortalApi.getCurrentUserProfile(token);
        QPortalPermission permission = getShipmentPermissionOrThrowException(profile.getPermissions());
        final QuincusUserDetails userDetails = userDetailsService.loadUserFromAuth(
                profile,
                createAuthorities(profile.getRoles(), permission),
                API_SOURCE
        );
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        authentication.setDetails(Map.of(REQUEST_URI, uri));
        return authentication;
    }

    public Authentication authenticatePreAuthenticatedUserWithUserId(String organizationId, String userId, String source) {
        final QPortalUser qPortalUser = qPortalApi.getUser(organizationId, userId);
        if (qPortalUser == null) {
            throw new QuincusValidationException(String.format(SHPV2_USER_NOT_FOUND_ERROR, userId, organizationId));
        }
        final QuincusUserDetails quincusUserDetails = userDetailsService.loadUserFromAuth(
                qPortalUser,
                List.of(new SimpleGrantedAuthority(source)),
                source
        );
        return new PreAuthenticatedAuthenticationToken(quincusUserDetails, source, quincusUserDetails.getAuthorities());
    }

    public Authentication authenticatePreAuthenticatedUser(String organizationId, String source, String sourceUser) {
        final QPortalOrganization organization = qPortalApi.getOrganizationById(organizationId);
        validateShipmentVersion(organization);
        final QuincusUserDetails quincusUserDetails = new QuincusUser(
                source,
                sourceUser,
                source,
                organizationId,
                List.of(new SimpleGrantedAuthority(source)));
        return new PreAuthenticatedAuthenticationToken(quincusUserDetails, source, quincusUserDetails.getAuthorities());
    }

    private void validateShipmentVersion(QPortalOrganization organization) {
        if (organization == null || CollectionUtils.isEmpty(organization.getOrganizationModuleItems()) ||
                !hasValidShipmentVersion(organization)) {
            throw new QuincusValidationException(String.format(NON_SHPV2_ORGANIZATION, organization != null ? organization.getId() : null));
        }
    }

    private boolean hasValidShipmentVersion(QPortalOrganization organization) {
        return organization.getOrganizationModuleItems().stream()
                .anyMatch(m -> m.getModuleItem() != null && SHPV2_PERMISSION_NAME.equalsIgnoreCase(m.getModuleItem().getName())
                        && SHPV2_PERMISSION_VERSION.equalsIgnoreCase(m.getModuleItem().getVersion()));
    }

    private QPortalPermission getShipmentPermissionOrThrowException(List<QPortalPermission> permissions) {
        return permissions.stream()
                .filter(p -> SHPV2_PERMISSION_NAME.equalsIgnoreCase(p.getName()) && SHPV2_PERMISSION_VERSION.equalsIgnoreCase(p.getVersion()))
                .findFirst()
                .orElseThrow(() -> new QuincusValidationException(MISSING_SHPV2_PERMISSION));
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
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        permission.getPages().forEach(page ->
                page.getActions().forEach(action ->
                        authorities.add(new SimpleGrantedAuthority((page.getName() + "_" + action.getName()).toUpperCase()))
                ));
        return authorities;
    }
}
