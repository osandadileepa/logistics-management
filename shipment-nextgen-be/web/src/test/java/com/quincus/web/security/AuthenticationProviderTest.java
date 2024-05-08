package com.quincus.web.security;

import com.quincus.qportal.api.QPortalApi;
import com.quincus.qportal.model.QPortalModuleItem;
import com.quincus.qportal.model.QPortalOrganization;
import com.quincus.qportal.model.QPortalOrganizationModuleItem;
import com.quincus.qportal.model.QPortalPermission;
import com.quincus.qportal.model.QPortalRole;
import com.quincus.qportal.model.QPortalUser;
import com.quincus.web.common.exception.model.QuincusValidationException;
import com.quincus.web.common.multitenant.QuincusUser;
import com.quincus.web.common.multitenant.QuincusUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationProviderTest {

    @Mock
    private PassThroughUserDetailsService userDetailsService;

    @Mock
    private QPortalApi qPortalApi;

    @InjectMocks
    private AuthenticationProvider authenticationProvider;

    private static final String SHPV2_PERMISSION_NAME = "shipment";
    private static final String SHPV2_PERMISSION_VERSION = "v2";
    private static final String REQUEST_URI = "requestURI";
    private static final String MISSING_SHPV2_PERMISSION = "User does not have 'shipment v2' permission.";

    @BeforeEach
    void setup() {
        Mockito.reset(userDetailsService, qPortalApi);
    }

    @Test
    void authenticateUser_ValidTokenAndUri_ReturnsUsernamePasswordAuthenticationToken() {
        // Arrange
        String token = "validToken";
        String uri = "/api/shipment";
        QPortalUser userProfile = createMockUserProfile();
        QPortalPermission shipmentPermission = createMockShipmentPermission();
        List<SimpleGrantedAuthority> expectedAuthorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("SHIPMENT_READ"),
                new SimpleGrantedAuthority("SHIPMENT_WRITE")
        );
        when(qPortalApi.getCurrentUserProfile(anyString())).thenReturn(userProfile);
        when(userDetailsService.loadUserFromAuth(any(QPortalUser.class), anyList(), eq("API")))
                .thenReturn(createMockUserDetails(expectedAuthorities));

        // Act
        Authentication result = authenticationProvider.authenticateUser(token, uri);

        // Assert
        assertThat(result).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(result.getPrincipal()).isInstanceOf(QuincusUserDetails.class);
    }

    @Test
    void authenticateUser_InvalidTokenAndUri_ThrowsQuincusValidationException() {
        // Arrange
        String token = "invalidToken";
        String uri = "/api/shipment";
        when(qPortalApi.getCurrentUserProfile(token)).thenThrow(QuincusValidationException.class);

        // Act & Assert
        assertThatExceptionOfType(QuincusValidationException.class)
                .isThrownBy(() -> authenticationProvider.authenticateUser(token, uri));
    }

    @Test
    void authenticatePreAuthenticatedUser_ValidOrganizationIdAndUser_ReturnsPreAuthenticatedAuthenticationToken() {
        // Arrange
        String organizationId = "orgId";
        String preAuthenticatedUser = "preAuthUser";
        List<SimpleGrantedAuthority> expectedAuthorities = Collections.singletonList(new SimpleGrantedAuthority(preAuthenticatedUser));
        QPortalOrganization organization = new QPortalOrganization();
        organization.setId(organizationId);
        organization.setOrganizationModuleItems(createQPortalModuleItems());
        when(qPortalApi.getOrganizationById(organizationId)).thenReturn(organization);

        // Act
        Authentication result = authenticationProvider.authenticatePreAuthenticatedUser(organizationId, preAuthenticatedUser, preAuthenticatedUser);

        // Assert
        assertThat(result).isInstanceOf(PreAuthenticatedAuthenticationToken.class);
        assertThat(result.getPrincipal()).isInstanceOf(QuincusUserDetails.class);
    }

    private List<QPortalOrganizationModuleItem> createQPortalModuleItems() {
        List<QPortalOrganizationModuleItem> organizationModuleItems = new ArrayList<>();
        QPortalOrganizationModuleItem organizationModuleItem = new QPortalOrganizationModuleItem();
        QPortalModuleItem moduleItem = new QPortalModuleItem();
        moduleItem.setId("moduleItemId");
        moduleItem.setName(SHPV2_PERMISSION_NAME);
        moduleItem.setVersion(SHPV2_PERMISSION_VERSION);
        organizationModuleItem.setModuleItem(moduleItem);
        organizationModuleItems.add(organizationModuleItem);
        return organizationModuleItems;
    }

    @Test
    void authenticatePreAuthenticatedUser_ValidOrganization_ShouldReturnAuthentication() {
        // Mock the response from QPortalApi
        String organizationId = "organizationId";
        QPortalOrganization organization = new QPortalOrganization();
        organization.setId(organizationId);
        organization.setOrganizationModuleItems(createQPortalModuleItems());
        when(qPortalApi.getOrganizationById(organizationId)).thenReturn(organization);

        // Call the method under test
        Authentication result = authenticationProvider.authenticatePreAuthenticatedUser(organizationId, "preAuthenticatedUser", "userName");

        // Assert the result
        assertThat(result).isInstanceOf(PreAuthenticatedAuthenticationToken.class);
        assertThat(result.getPrincipal()).isInstanceOf(QuincusUserDetails.class);
    }

    @Test
    void authenticatePreAuthenticatedUser_NullQPortalModuleItems_ShouldReturnAuthentication() {
        String organizationId = "organizationId";
        QPortalOrganization organization = new QPortalOrganization();
        organization.setId(organizationId);
        organization.setOrganizationModuleItems(null);
        when(qPortalApi.getOrganizationById(organizationId)).thenReturn(organization);

        assertThatThrownBy(() -> authenticationProvider.authenticatePreAuthenticatedUser(organizationId, "preAuthenticatedUser", "userName"))
                .isInstanceOf(QuincusValidationException.class)
                .hasMessageContaining("Organization 'organizationId' does not have access to 'shipment v2' module.");
    }

    @Test
    void authenticatePreAuthenticatedUser_EmptyQPortalModuleItems_ShouldReturnAuthentication() {
        String organizationId = "organizationId";
        QPortalOrganization organization = new QPortalOrganization();
        organization.setId(organizationId);
        organization.setOrganizationModuleItems(Collections.emptyList());
        when(qPortalApi.getOrganizationById(organizationId)).thenReturn(organization);

        assertThatThrownBy(() -> authenticationProvider.authenticatePreAuthenticatedUser(organizationId, "preAuthenticatedUser", "userName"))
                .isInstanceOf(QuincusValidationException.class)
                .hasMessageContaining("Organization 'organizationId' does not have access to 'shipment v2' module.");
    }

    @Test
    void authenticatePreAuthenticatedUser_InvalidOrganization_ShouldThrowException() {
        String organizationId = "organizationId";
        when(qPortalApi.getOrganizationById(organizationId)).thenReturn(null);

        assertThatThrownBy(() -> authenticationProvider.authenticatePreAuthenticatedUser(organizationId, "preAuthenticatedUser", "userName"))
                .isInstanceOf(QuincusValidationException.class)
                .hasMessageContaining("Organization 'null' does not have access to 'shipment v2' module.");
    }

    private QPortalUser createMockUserProfile() {
        QPortalUser userProfile = new QPortalUser();
        QPortalPermission otherPermission = new QPortalPermission();
        otherPermission.setName("other_permission");
        otherPermission.setVersion("v1");
        QPortalRole role = new QPortalRole();
        role.setName("USER");
        userProfile.setRoles(Arrays.asList(role));
        userProfile.setPermissions(Arrays.asList(createMockShipmentPermission(), otherPermission));
        return userProfile;
    }

    private QPortalPermission createMockShipmentPermission() {
        QPortalPermission shipmentPermission = new QPortalPermission();
        QPortalPermission.Page page = new QPortalPermission.Page();
        page.setName("shipment");
        QPortalPermission.Page.Action action = new QPortalPermission.Page.Action();
        action.setName("READ");
        page.setActions(Arrays.asList(action));
        shipmentPermission.setName(SHPV2_PERMISSION_NAME);
        shipmentPermission.setVersion(SHPV2_PERMISSION_VERSION);
        shipmentPermission.setPages(Collections.singletonList(page));
        return shipmentPermission;
    }

    private QuincusUserDetails createMockUserDetails(List<SimpleGrantedAuthority> authorities) {
        return new QuincusUser("userid", "username", "userSource", "orgId", authorities);
    }
}
