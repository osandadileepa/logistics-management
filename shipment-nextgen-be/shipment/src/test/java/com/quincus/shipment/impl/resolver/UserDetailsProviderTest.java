package com.quincus.shipment.impl.resolver;

import com.quincus.shipment.api.domain.Organization;
import com.quincus.web.common.multitenant.QuincusLocationCoverage;
import com.quincus.web.common.multitenant.UserDetailsContextHolder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsProviderTest {

    @Mock
    private UserDetailsContextHolder contextHolder;
    @InjectMocks
    private UserDetailsProvider userDetailsProvider;

    @Test
    @DisplayName("Get current organization")
    void getCurrentOrganization() {
        String organizationId = "org123";
        String organizationName = "Quincus";
        when(contextHolder.getCurrentOrganizationId()).thenReturn(organizationId);
        when(contextHolder.getCurrentOrganizationName()).thenReturn(organizationName);

        Organization currentOrganization = userDetailsProvider.getCurrentOrganization();

        assertThat(currentOrganization.getId()).isEqualTo(organizationId);
        assertThat(currentOrganization.getName()).isEqualTo(organizationName);
    }

    @Test
    @DisplayName("Get current organization ID")
    void getCurrentOrganizationId() {
        String organizationId = "org123";
        when(contextHolder.getCurrentOrganizationId()).thenReturn(organizationId);

        String currentOrganizationId = userDetailsProvider.getCurrentOrganizationId();

        assertThat(currentOrganizationId).isEqualTo(organizationId);
    }

    @Test
    @DisplayName("Get current user ID")
    void getCurrentUserId() {
        String userId = "user123";
        when(contextHolder.getCurrentUserId()).thenReturn(userId);

        String currentUserId = userDetailsProvider.getCurrentUserId();

        assertThat(currentUserId).isEqualTo(userId);
    }

    @Test
    @DisplayName("Get current user full name")
    void getCurrentUserFullName() {
        String fullName = "John Doe";
        when(contextHolder.getCurrentUserFullName()).thenReturn(fullName);

        String currentUserFullName = userDetailsProvider.getCurrentUserFullName();

        assertThat(currentUserFullName).isEqualTo(fullName);
    }

    @Test
    @DisplayName("Get current location coverages - empty list")
    void getCurrentLocationCoverages_EmptyList() {
        when(contextHolder.getCurrentLocationCoverages()).thenReturn(Collections.emptyList());

        List<QuincusLocationCoverage> currentLocationCoverages = userDetailsProvider.getCurrentLocationCoverages();

        assertThat(currentLocationCoverages).isEmpty();
    }

    @Test
    @DisplayName("Get current location coverages - non-empty list")
    void getCurrentLocationCoverages_NonEmptyList() {
        List<QuincusLocationCoverage> locationCoverages = new ArrayList<>();
        locationCoverages.add(new QuincusLocationCoverage());
        locationCoverages.add(new QuincusLocationCoverage());
        when(contextHolder.getCurrentLocationCoverages()).thenReturn(locationCoverages);

        List<QuincusLocationCoverage> currentLocationCoverages = userDetailsProvider.getCurrentLocationCoverages();

        assertThat(currentLocationCoverages).isEqualTo(locationCoverages);
    }

    @Test
    @DisplayName("Check if request is from allowed source - URI ends with '/orders'")
    void isFromAllowedSource_URIMatches() {
        String requestURI = "/orders";
        when(contextHolder.getCurrentRequestURI()).thenReturn(requestURI);

        boolean result = userDetailsProvider.isFromAllowedSource();

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Check if request is from allowed source - from Kafka")
    void isFromAllowedSource_FromKafka() {
        String requestURI = "/some/other/endpoint";
        String source = "kafka";
        when(contextHolder.getCurrentRequestURI()).thenReturn(requestURI);
        when(contextHolder.getCurrentSource()).thenReturn(source);

        boolean result = userDetailsProvider.isFromAllowedSource();

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Check if request is from allowed source - from 3rd party server")
    void isFromAllowedSource_From3rdPartyServer() {
        String requestURI = "/some/other/endpoint";
        String userId = "s2s";
        when(contextHolder.getCurrentRequestURI()).thenReturn(requestURI);
        when(contextHolder.getCurrentSource()).thenReturn(userId);

        boolean result = userDetailsProvider.isFromAllowedSource();

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Check if request is not from allowed source")
    void isFromAllowedSource_False() {
        String requestURI = "/some/other/endpoint";
        String userId = "user123";
        when(contextHolder.getCurrentRequestURI()).thenReturn(requestURI);
        when(contextHolder.getCurrentSource()).thenReturn(userId);

        boolean result = userDetailsProvider.isFromAllowedSource();

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Check if request is from pre-authenticated source - from Kafka")
    void isFromPreAuthenticatedSource_FromKafka() {
        String source = "kafka";
        when(contextHolder.getCurrentSource()).thenReturn(source);

        boolean result = userDetailsProvider.isFromPreAuthenticatedSource();

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Check if request is from pre-authenticated source - from other modules via S2S token")
    void isFromPreAuthenticatedSource_FromOtherModulesViaS2SToken() {
        String source = "S2S";
        when(contextHolder.getCurrentSource()).thenReturn(source);

        boolean result = userDetailsProvider.isFromPreAuthenticatedSource();

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Check if request is not from pre-authenticated source")
    void isFromPreAuthenticatedSource_False() {
        String source = "invalidSource";
        when(contextHolder.getCurrentSource()).thenReturn(source);

        boolean result = userDetailsProvider.isFromPreAuthenticatedSource();

        assertThat(result).isFalse();
    }

}

