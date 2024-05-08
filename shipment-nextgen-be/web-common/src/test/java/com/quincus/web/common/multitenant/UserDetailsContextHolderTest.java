package com.quincus.web.common.multitenant;

import com.quincus.web.common.exception.model.OrganizationDetailsNotFoundException;
import com.quincus.web.common.exception.model.UserDetailsNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsContextHolderTest {

    private UserDetailsContextHolder provider;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @BeforeEach
    public void setUp() {
        provider = new UserDetailsContextHolder();
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testGetCurrentOrganizationId() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        QuincusUserDetails userDetails = createQuincusUser();
        when(authentication.getPrincipal()).thenReturn(userDetails);
        assertThat(provider.getCurrentOrganizationId()).isEqualTo("org123");
    }

    @Test
    void testGetCurrentOrganizationIdThrowsException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(null);
        assertThatExceptionOfType(OrganizationDetailsNotFoundException.class).isThrownBy(() -> provider.getCurrentOrganizationId())
                .withMessage("Unable to resolve Organization context.");
    }

    @Test
    void testGetCurrentOrganizationName() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        QuincusUserDetails userDetails = createQuincusUser();
        when(authentication.getPrincipal()).thenReturn(userDetails);
        assertThat(provider.getCurrentOrganizationName()).isEqualTo("Test Organization");
    }

    @Test
    void testGetCurrentOrganizationNameThrowsException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(null);
        assertThatExceptionOfType(OrganizationDetailsNotFoundException.class).isThrownBy(() -> provider.getCurrentOrganizationName())
                .withMessage("Unable to resolve Organization context.");
    }

    @Test
    void testGetCurrentUserFullName() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        QuincusUserDetails userDetails = createQuincusUser();
        when(authentication.getPrincipal()).thenReturn(userDetails);
        assertThat(provider.getCurrentUserFullName()).isEqualTo("Test User");
    }

    @Test
    void testGetCurrentUserFullNameThrowsException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(null);
        assertThatExceptionOfType(UserDetailsNotFoundException.class).isThrownBy(() -> provider.getCurrentUserFullName())
                .withMessage("Unable to resolve UserProfile context.");
    }

    @Test
    void testGetCurrentUserId() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        QuincusUserDetails userDetails = createQuincusUser();
        when(authentication.getPrincipal()).thenReturn(userDetails);
        assertThat(provider.getCurrentUserId()).isEqualTo("123");
    }

    @Test
    void testGetCurrentUserIdThrowsException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(null);
        assertThatExceptionOfType(UserDetailsNotFoundException.class).isThrownBy(() -> provider.getCurrentUserId())
                .withMessage("Unable to resolve UserProfile context.");
    }

    @Test
    void testGetCurrentLocationCoverages() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        QuincusUserDetails userDetails = createQuincusUser();
        when(authentication.getPrincipal()).thenReturn(userDetails);
        assertThat(provider.getCurrentLocationCoverages().get(0).getLocationCode()).isEqualTo("PH");
    }

    @Test
    void testGetPartnerWhenBlankShouldReturnNull() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        QuincusUserDetails userDetails = createQuincusUserWithBlankStringPartnerId();

        when(authentication.getPrincipal()).thenReturn(userDetails);
        assertThat(provider.getCurrentPartnerId()).isNull();
    }

    @Test
    void testGetCurrentUserCurrentLocation() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        QuincusUserDetails userDetails = createQuincusUserWithLimitedDetails();
        when(authentication.getPrincipal()).thenReturn(userDetails);
        assertThat(provider.getUserCurrentLocation()).isNotNull();
        assertThat(provider.getUserCurrentLocation().locationFacilityName()).isNull();
        assertThat(provider.getUserCurrentLocation().locationCode()).isNull();
        assertThat(provider.getUserCurrentLocation().locationId()).isNull();
    }

    private QuincusUser createQuincusUser() {
        QuincusLocationCoverage locationCoverage = new QuincusLocationCoverage();
        locationCoverage.setId(UUID.randomUUID().toString());
        locationCoverage.setLocationCode("PH");

        QuincusUserPartner userPartner = new QuincusUserPartner();
        userPartner.setPartnerId(UUID.randomUUID().toString());
        userPartner.setPartnerName("Partner A");

        return new QuincusUser(
                "testuser",
                "123",
                "Test User",
                "org123",
                "Test Organization",
                "partner123",
                "Test Partner",
                List.of(),
                List.of(locationCoverage),
                new QuincusUserLocation(),
                List.of(userPartner),
                "API"
        );
    }

    private QuincusUser createQuincusUserWithBlankStringPartnerId() {
        QuincusLocationCoverage locationCoverage = new QuincusLocationCoverage();
        locationCoverage.setId(UUID.randomUUID().toString());
        locationCoverage.setLocationCode("PH");

        QuincusUserPartner userPartner = new QuincusUserPartner();
        userPartner.setPartnerId("");
        userPartner.setPartnerName("Partner A");

        return new QuincusUser(
                "testuser",
                "123",
                "Test User",
                "org123",
                "Test Organization",
                "",
                "Test Partner",
                List.of(),
                List.of(locationCoverage),
                new QuincusUserLocation(),
                List.of(userPartner),
                "API"
        );
    }

    private QuincusUser createQuincusUserWithLimitedDetails() {
        return new QuincusUser(
                "testId",
                "KAFKA",
                "KAFKA",
                "123",
                List.of()
        );
    }

}