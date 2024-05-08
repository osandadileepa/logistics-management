package com.quincus.web.security;

import com.quincus.qportal.api.QPortalApi;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationProviderTest {

    private static final String PERMISSION_NAME = "network_management";
    private static final String PERMISSION_VERSION = "v1";
    private static final String REQUEST_URI = "/";
    @Mock
    private PassThroughUserDetailsService userDetailsService;
    @Mock
    private QPortalApi qPortalApi;
    @InjectMocks
    private AuthenticationProvider authenticationProvider;

    @BeforeEach
    void setup() {
        Mockito.reset(userDetailsService, qPortalApi);
    }

    @Test
    void authenticateUser_ValidTokenAndUri_ReturnsUsernamePasswordAuthenticationToken() {
        // Arrange
        String token = "validToken";
        String uri = REQUEST_URI;
        QPortalUser userProfile = createMockUserProfile();
        QPortalPermission shipmentPermission = createMockShipmentPermission();
        List<SimpleGrantedAuthority> expectedAuthorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("SHIPMENT_READ"),
                new SimpleGrantedAuthority("SHIPMENT_WRITE")
        );
        when(qPortalApi.getCurrentUserProfile(anyString())).thenReturn(userProfile);
        when(userDetailsService.loadUserFromAuth(any(QPortalUser.class), anyList()))
                .thenReturn(createMockUserDetails(expectedAuthorities));

        // Act
        Authentication result = authenticationProvider.authenticateUser(token, uri);

        // Assert
        assertThat(result).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(result.getPrincipal()).isInstanceOf(QuincusUserDetails.class);
    }

    @Test
    void authenticateUser_InvalidTokenAndUri_ThrowsQuincusValidationException() {
        String token = "invalidToken";
        String uri = REQUEST_URI;
        when(qPortalApi.getCurrentUserProfile(token)).thenThrow(QuincusValidationException.class);

        assertThatExceptionOfType(QuincusValidationException.class)
                .isThrownBy(() -> authenticationProvider.authenticateUser(token, uri));
    }

    private QPortalUser createMockUserProfile() {
        QPortalUser userProfile = new QPortalUser();
        QPortalPermission otherPermission = new QPortalPermission();
        otherPermission.setName("other_permission");
        otherPermission.setVersion("v1");
        QPortalRole role = new QPortalRole();
        role.setName("USER");
        userProfile.setRoles(List.of(role));
        userProfile.setPermissions(Arrays.asList(createMockShipmentPermission(), otherPermission));
        return userProfile;
    }

    private QPortalPermission createMockShipmentPermission() {
        QPortalPermission shipmentPermission = new QPortalPermission();
        QPortalPermission.Page page = new QPortalPermission.Page();
        page.setName("shipment");
        QPortalPermission.Page.Action action = new QPortalPermission.Page.Action();
        action.setName("READ");
        page.setActions(List.of(action));
        shipmentPermission.setName(PERMISSION_NAME);
        shipmentPermission.setVersion(PERMISSION_VERSION);
        shipmentPermission.setPages(Collections.singletonList(page));
        return shipmentPermission;
    }

    private QuincusUserDetails createMockUserDetails(List<SimpleGrantedAuthority> authorities) {
        return new QuincusUser("userid", "username", "password", authorities);
    }
}
