package com.quincus.shipment.impl.resolver;

import com.quincus.shipment.api.exception.SegmentLocationNotAllowedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationPermissionCheckerTest {

    @Test
    void testCheckLocationPermissions_WithValidLocationIds() {
        // Mock UserDetailsProvider
        UserDetailsProvider userDetailsProvider = mock(UserDetailsProvider.class);
        when(userDetailsProvider.getCurrentLocationCoverageIds())
                .thenReturn(List.of("ExternalID1", "ExternalID2", "ExternalID3"));

        // Create LocationPermissionChecker
        LocationPermissionChecker locationPermissionChecker = new LocationPermissionChecker(userDetailsProvider);

        // Set of external IDs
        Set<String> externalIds = Set.of("ExternalID1", "ExternalID3");

        // Perform the check
        locationPermissionChecker.checkLocationPermissions(externalIds);

        // No exception should be thrown
    }

    @Test
    void testCheckLocationPermissions_WithInvalidLocationIds() {
        // Mock UserDetailsProvider
        UserDetailsProvider userDetailsProvider = mock(UserDetailsProvider.class);
        when(userDetailsProvider.getCurrentLocationCoverageIds())
                .thenReturn(List.of("ExternalID1", "ExternalID2", "ExternalID3"));

        // Create LocationPermissionChecker
        LocationPermissionChecker locationPermissionChecker = new LocationPermissionChecker(userDetailsProvider);

        // Set of external IDs
        Set<String> externalIds = Set.of("ExternalID4", "ExternalID5");

        // Perform the check and verify the exception
        assertThatExceptionOfType(SegmentLocationNotAllowedException.class)
                .isThrownBy(() -> locationPermissionChecker.checkLocationPermissions(externalIds))
                .withMessage("No access location coverages for the segment.");
    }

    @Test
    void testCheckLocationPermissions_WithEmptyLocationIds() {
        // Mock UserDetailsProvider
        UserDetailsProvider userDetailsProvider = mock(UserDetailsProvider.class);
        when(userDetailsProvider.getCurrentLocationCoverageIds())
                .thenReturn(List.of("ExternalID1", "ExternalID2", "ExternalID3"));

        // Create LocationPermissionChecker
        LocationPermissionChecker locationPermissionChecker = new LocationPermissionChecker(userDetailsProvider);

        // Empty set of external IDs
        Set<String> externalIds = new HashSet<>();

        // Perform the check and verify the exception
        assertThatExceptionOfType(SegmentLocationNotAllowedException.class)
                .isThrownBy(() -> locationPermissionChecker.checkLocationPermissions(externalIds))
                .withMessage("No access location coverages for the segment.");
    }
    
    @Test
    void testCheckLocationPermissions_WithEmptyCurrentLocationCoverageIds() {
        // Mock UserDetailsProvider with empty current location coverage IDs
        UserDetailsProvider userDetailsProvider = mock(UserDetailsProvider.class);
        when(userDetailsProvider.getCurrentLocationCoverageIds()).thenReturn(List.of());

        // Create LocationPermissionChecker
        LocationPermissionChecker locationPermissionChecker = new LocationPermissionChecker(userDetailsProvider);

        // Set of external IDs
        Set<String> externalIds = Set.of("ExternalID1", "ExternalID2");

        // Perform the check and verify the exception
        assertThatExceptionOfType(SegmentLocationNotAllowedException.class)
                .isThrownBy(() -> locationPermissionChecker.checkLocationPermissions(externalIds))
                .withMessage("No access location coverages for the segment.");
    }
}
