package com.quincus.shipment.impl.resolver;

import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.exception.UserGroupNotAllowedException;
import com.quincus.shipment.impl.repository.entity.CostEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.web.common.multitenant.QuincusUserPartner;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserGroupPermissionCheckerTest {

    private static Stream<Arguments> provideMatchingShipmentAndUserDetails() {
        String partnerId1 = "partner-uuid-1";
        String partnerId2 = "partner-uuid-2";

        Shipment shipmentNoPartner = new Shipment();
        Shipment shipmentWithPartner = new Shipment();
        shipmentWithPartner.setPartnerId(partnerId1);

        UserDetailsProvider userNoPartner = mock(UserDetailsProvider.class);
        when(userNoPartner.getCurrentPartnerId()).thenReturn(null);
        when(userNoPartner.getCurrentUserPartners()).thenReturn(Collections.emptyList());

        QuincusUserPartner user1 = new QuincusUserPartner();
        user1.setPartnerId(partnerId1);

        UserDetailsProvider companyUserWithPartner = mock(UserDetailsProvider.class);
        when(companyUserWithPartner.getCurrentPartnerId()).thenReturn(null);
        when(companyUserWithPartner.getCurrentUserPartners()).thenReturn(List.of(user1));

        UserDetailsProvider partnerUser = mock(UserDetailsProvider.class);
        when(partnerUser.getCurrentPartnerId()).thenReturn(partnerId1);
        when(partnerUser.getCurrentUserPartners()).thenReturn(Collections.emptyList());

        UserDetailsProvider partnerUserWithPartner = mock(UserDetailsProvider.class);
        when(partnerUserWithPartner.getCurrentPartnerId()).thenReturn(partnerId2);
        when(partnerUserWithPartner.getCurrentUserPartners()).thenReturn(List.of(user1));


        return Stream.of(
                Arguments.of(Named.of("Shipment no Partner", shipmentNoPartner),
                        Named.of("Company User", userNoPartner)),
                Arguments.of(Named.of("Shipment no Partner", shipmentNoPartner),
                        Named.of("Company User w/ Partner", companyUserWithPartner)),
                Arguments.of(Named.of("Shipment with Partner", shipmentWithPartner),
                        Named.of("Partner User", partnerUser)),
                Arguments.of(Named.of("Shipment with Partner", shipmentWithPartner),
                        Named.of("Company User w/ Partner", companyUserWithPartner)),
                Arguments.of(Named.of("Shipment with Partner", shipmentWithPartner),
                        Named.of("Partner User w/ extra Partner", partnerUserWithPartner))
        );
    }

    private static Stream<Arguments> provideMismatchShipmentAndUserDetails() {
        String partnerId1 = "partner-uuid-1";
        String partnerId2 = "partner-uuid-2";

        Shipment shipmentNoPartner = new Shipment();
        Shipment shipmentWithPartner = new Shipment();
        shipmentWithPartner.setPartnerId(partnerId1);

        UserDetailsProvider userNoPartner = mock(UserDetailsProvider.class);
        when(userNoPartner.getCurrentPartnerId()).thenReturn(null);
        when(userNoPartner.getCurrentUserPartners()).thenReturn(Collections.emptyList());

        QuincusUserPartner user1 = new QuincusUserPartner();
        user1.setPartnerId(partnerId1);

        UserDetailsProvider companyUserWithPartner = mock(UserDetailsProvider.class);
        when(companyUserWithPartner.getCurrentPartnerId()).thenReturn(null);
        when(companyUserWithPartner.getCurrentUserPartners()).thenReturn(List.of(user1));

        UserDetailsProvider partnerUser = mock(UserDetailsProvider.class);
        when(partnerUser.getCurrentPartnerId()).thenReturn(partnerId1);
        when(partnerUser.getCurrentUserPartners()).thenReturn(Collections.emptyList());

        UserDetailsProvider partnerUser2 = mock(UserDetailsProvider.class);
        when(partnerUser2.getCurrentPartnerId()).thenReturn(partnerId2);
        when(partnerUser2.getCurrentUserPartners()).thenReturn(Collections.emptyList());

        UserDetailsProvider partnerUserWithPartner = mock(UserDetailsProvider.class);
        when(partnerUserWithPartner.getCurrentPartnerId()).thenReturn(partnerId2);
        when(partnerUserWithPartner.getCurrentUserPartners()).thenReturn(List.of(user1));


        return Stream.of(
                Arguments.of(Named.of("Shipment from Partner1", shipmentWithPartner),
                        Named.of("Partner2", partnerUser2)),
                Arguments.of(Named.of("Shipment from Partner1", shipmentWithPartner),
                        Named.of("Company User", userNoPartner)),
                Arguments.of(Named.of("Shipment no Partner", shipmentNoPartner),
                        Named.of("Partner1", partnerUser)),
                Arguments.of(Named.of("Shipment no Partner", shipmentNoPartner),
                        Named.of("Partner User w/ extra Partner", partnerUserWithPartner))
        );
    }

    private static Stream<Arguments> provideMatchingShipmentEntityAndUserDetails() {
        String partnerId1 = "partner-uuid-1";
        String partnerId2 = "partner-uuid-2";

        ShipmentEntity shipmentNoPartner = new ShipmentEntity();
        ShipmentEntity shipmentWithPartner = new ShipmentEntity();
        shipmentWithPartner.setPartnerId(partnerId1);

        UserDetailsProvider userNoPartner = mock(UserDetailsProvider.class);
        when(userNoPartner.getCurrentPartnerId()).thenReturn(null);
        when(userNoPartner.getCurrentUserPartners()).thenReturn(Collections.emptyList());

        QuincusUserPartner user1 = new QuincusUserPartner();
        user1.setPartnerId(partnerId1);

        UserDetailsProvider companyUserWithPartner = mock(UserDetailsProvider.class);
        when(companyUserWithPartner.getCurrentPartnerId()).thenReturn(null);
        when(companyUserWithPartner.getCurrentUserPartners()).thenReturn(List.of(user1));

        UserDetailsProvider partnerUser = mock(UserDetailsProvider.class);
        when(partnerUser.getCurrentPartnerId()).thenReturn(partnerId1);
        when(partnerUser.getCurrentUserPartners()).thenReturn(Collections.emptyList());

        UserDetailsProvider partnerUserWithPartner = mock(UserDetailsProvider.class);
        when(partnerUserWithPartner.getCurrentPartnerId()).thenReturn(partnerId2);
        when(partnerUserWithPartner.getCurrentUserPartners()).thenReturn(List.of(user1));


        return Stream.of(
                Arguments.of(Named.of("Shipment no Partner", shipmentNoPartner),
                        Named.of("Company User", userNoPartner)),
                Arguments.of(Named.of("Shipment no Partner", shipmentNoPartner),
                        Named.of("Company User w/ Partner", companyUserWithPartner)),
                Arguments.of(Named.of("Shipment with Partner", shipmentWithPartner),
                        Named.of("Partner User", partnerUser)),
                Arguments.of(Named.of("Shipment with Partner", shipmentWithPartner),
                        Named.of("Company User w/ Partner", companyUserWithPartner)),
                Arguments.of(Named.of("Shipment with Partner", shipmentWithPartner),
                        Named.of("Partner User w/ extra Partner", partnerUserWithPartner))
        );
    }

    private static Stream<Arguments> provideMismatchShipmentEntityAndUserDetails() {
        String partnerId1 = "partner-uuid-1";
        String partnerId2 = "partner-uuid-2";

        ShipmentEntity shipmentNoPartner = new ShipmentEntity();
        ShipmentEntity shipmentWithPartner = new ShipmentEntity();
        shipmentWithPartner.setPartnerId(partnerId1);

        UserDetailsProvider userNoPartner = mock(UserDetailsProvider.class);
        when(userNoPartner.getCurrentPartnerId()).thenReturn(null);
        when(userNoPartner.getCurrentUserPartners()).thenReturn(Collections.emptyList());

        QuincusUserPartner user1 = new QuincusUserPartner();
        user1.setPartnerId(partnerId1);

        UserDetailsProvider companyUserWithPartner = mock(UserDetailsProvider.class);
        when(companyUserWithPartner.getCurrentPartnerId()).thenReturn(null);
        when(companyUserWithPartner.getCurrentUserPartners()).thenReturn(List.of(user1));

        UserDetailsProvider partnerUser = mock(UserDetailsProvider.class);
        when(partnerUser.getCurrentPartnerId()).thenReturn(partnerId1);
        when(partnerUser.getCurrentUserPartners()).thenReturn(Collections.emptyList());

        UserDetailsProvider partnerUser2 = mock(UserDetailsProvider.class);
        when(partnerUser2.getCurrentPartnerId()).thenReturn(partnerId2);
        when(partnerUser2.getCurrentUserPartners()).thenReturn(Collections.emptyList());

        UserDetailsProvider partnerUserWithPartner = mock(UserDetailsProvider.class);
        when(partnerUserWithPartner.getCurrentPartnerId()).thenReturn(partnerId2);
        when(partnerUserWithPartner.getCurrentUserPartners()).thenReturn(List.of(user1));


        return Stream.of(
                Arguments.of(Named.of("Shipment from Partner1", shipmentWithPartner),
                        Named.of("Partner2", partnerUser2)),
                Arguments.of(Named.of("Shipment from Partner1", shipmentWithPartner),
                        Named.of("Company User", userNoPartner)),
                Arguments.of(Named.of("Shipment no Partner", shipmentNoPartner),
                        Named.of("Partner1", partnerUser)),
                Arguments.of(Named.of("Shipment no Partner", shipmentNoPartner),
                        Named.of("Partner User w/ extra Partner", partnerUserWithPartner))
        );
    }

    private static Stream<Arguments> provideMatchingCostEntityAndUserDetails() {
        String partnerId1 = "partner-uuid-1";
        String partnerId2 = "partner-uuid-2";

        CostEntity costNoPartner = new CostEntity();
        CostEntity costWithPartner = new CostEntity();
        costWithPartner.setPartnerId(partnerId1);
        CostEntity costBlankPartner = new CostEntity();
        costBlankPartner.setPartnerId("");

        UserDetailsProvider userNoPartner = mock(UserDetailsProvider.class);
        when(userNoPartner.getCurrentPartnerId()).thenReturn(null);
        when(userNoPartner.getCurrentUserPartners()).thenReturn(Collections.emptyList());

        QuincusUserPartner user1 = new QuincusUserPartner();
        user1.setPartnerId(partnerId1);

        UserDetailsProvider companyUserWithPartner = mock(UserDetailsProvider.class);
        when(companyUserWithPartner.getCurrentPartnerId()).thenReturn(null);
        when(companyUserWithPartner.getCurrentUserPartners()).thenReturn(List.of(user1));

        UserDetailsProvider partnerUser = mock(UserDetailsProvider.class);
        when(partnerUser.getCurrentPartnerId()).thenReturn(partnerId1);
        when(partnerUser.getCurrentUserPartners()).thenReturn(Collections.emptyList());

        UserDetailsProvider partnerUserWithPartner = mock(UserDetailsProvider.class);
        when(partnerUserWithPartner.getCurrentPartnerId()).thenReturn(partnerId2);
        when(partnerUserWithPartner.getCurrentUserPartners()).thenReturn(List.of(user1));


        return Stream.of(
                Arguments.of(Named.of("Cost no Partner", costNoPartner),
                        Named.of("Company User", userNoPartner)),
                Arguments.of(Named.of("Cost blank Partner", costBlankPartner),
                        Named.of("Company User", userNoPartner)),
                Arguments.of(Named.of("Cost no Partner", costNoPartner),
                        Named.of("Company User w/ Partner", companyUserWithPartner)),
                Arguments.of(Named.of("Cost with Partner", costWithPartner),
                        Named.of("Partner User", partnerUser)),
                Arguments.of(Named.of("Cost with Partner", costWithPartner),
                        Named.of("Company User w/ Partner", companyUserWithPartner)),
                Arguments.of(Named.of("Cost with Partner", costWithPartner),
                        Named.of("Partner User w/ extra Partner", partnerUserWithPartner))
        );
    }

    private static Stream<Arguments> provideMismatchCostEntityAndUserDetails() {
        String partnerId1 = "partner-uuid-1";
        String partnerId2 = "partner-uuid-2";

        CostEntity costNoPartner = new CostEntity();
        CostEntity costWithPartner = new CostEntity();
        costWithPartner.setPartnerId(partnerId1);

        UserDetailsProvider userNoPartner = mock(UserDetailsProvider.class);
        when(userNoPartner.getCurrentPartnerId()).thenReturn(null);
        when(userNoPartner.getCurrentUserPartners()).thenReturn(Collections.emptyList());

        QuincusUserPartner user1 = new QuincusUserPartner();
        user1.setPartnerId(partnerId1);

        UserDetailsProvider companyUserWithPartner = mock(UserDetailsProvider.class);
        when(companyUserWithPartner.getCurrentPartnerId()).thenReturn(null);
        when(companyUserWithPartner.getCurrentUserPartners()).thenReturn(List.of(user1));

        UserDetailsProvider partnerUser = mock(UserDetailsProvider.class);
        when(partnerUser.getCurrentPartnerId()).thenReturn(partnerId1);
        when(partnerUser.getCurrentUserPartners()).thenReturn(Collections.emptyList());

        UserDetailsProvider partnerUser2 = mock(UserDetailsProvider.class);
        when(partnerUser2.getCurrentPartnerId()).thenReturn(partnerId2);
        when(partnerUser2.getCurrentUserPartners()).thenReturn(Collections.emptyList());

        UserDetailsProvider partnerUserWithPartner = mock(UserDetailsProvider.class);
        when(partnerUserWithPartner.getCurrentPartnerId()).thenReturn(partnerId2);
        when(partnerUserWithPartner.getCurrentUserPartners()).thenReturn(List.of(user1));


        return Stream.of(
                Arguments.of(Named.of("Cost from Partner1", costWithPartner),
                        Named.of("Partner2", partnerUser2)),
                Arguments.of(Named.of("Cost from Partner1", costWithPartner),
                        Named.of("Company User", userNoPartner)),
                Arguments.of(Named.of("Cost no Partner", costNoPartner),
                        Named.of("Partner1", partnerUser)),
                Arguments.of(Named.of("Cost no Partner", costNoPartner),
                        Named.of("Partner User w/ extra Partner", partnerUserWithPartner))
        );
    }

    @Test
    void testCheckUserGroupPermissions_WithValidPartnerId() {
        // Mock UserDetailsProvider
        UserDetailsProvider userDetailsProvider = mock(UserDetailsProvider.class);

        QuincusUserPartner partnerA = new QuincusUserPartner();
        partnerA.setPartnerId("partnerA");
        QuincusUserPartner partnerB = new QuincusUserPartner();
        partnerB.setPartnerId("partnerB");

        when(userDetailsProvider.getCurrentUserPartners())
                .thenReturn(List.of(partnerA, partnerB));

        // Create UserGroupPermissionChecker
        UserGroupPermissionChecker userGroupPermissionChecker = new UserGroupPermissionChecker(userDetailsProvider);

        // Perform the check
        userGroupPermissionChecker.checkUserGroupPermissions("partnerA");

        // No exception should be thrown
    }

    @Test
    void testCheckUserGroupPermissions_WithInvalidPartnerId() {
        // Mock UserDetailsProvider
        UserDetailsProvider userDetailsProvider = mock(UserDetailsProvider.class);

        QuincusUserPartner partnerA = new QuincusUserPartner();
        partnerA.setPartnerId("partnerA");
        QuincusUserPartner partnerB = new QuincusUserPartner();
        partnerB.setPartnerId("partnerB");

        when(userDetailsProvider.getCurrentUserPartners())
                .thenReturn(List.of(partnerA, partnerB));

        // Create UserGroupPermissionChecker
        UserGroupPermissionChecker userGroupPermissionChecker = new UserGroupPermissionChecker(userDetailsProvider);

        // Perform the check and verify the exception
        assertThatExceptionOfType(UserGroupNotAllowedException.class)
                .isThrownBy(() -> userGroupPermissionChecker.checkUserGroupPermissions("partnerX"))
                .withMessage("Current user does not meet the required user group to access or modify this record.");
    }

    @ParameterizedTest
    @MethodSource("provideMatchingShipmentAndUserDetails")
    void checkUserGroupPermissions_shipmentCriteria_shouldNotThrowException(Shipment shipment,
                                                                            UserDetailsProvider userDetailsProvider) {
        UserGroupPermissionChecker checker = new UserGroupPermissionChecker(userDetailsProvider);
        assertThatNoException().isThrownBy(() ->
                checker.checkUserGroupPermissions(shipment));
    }

    @ParameterizedTest
    @MethodSource("provideMismatchShipmentAndUserDetails")
    void checkUserGroupPermissions_shipmentCriteria_shouldThrowException(Shipment shipment,
                                                                         UserDetailsProvider userDetailsProvider) {
        UserGroupPermissionChecker checker = new UserGroupPermissionChecker(userDetailsProvider);
        assertThatThrownBy(() -> checker.checkUserGroupPermissions(shipment))
                .isInstanceOf(UserGroupNotAllowedException.class);
    }

    @ParameterizedTest
    @MethodSource("provideMatchingShipmentEntityAndUserDetails")
    void checkUserGroupPermissions_shipmentEntityCriteria_shouldNotThrowException(ShipmentEntity shipmentEntity,
                                                                                  UserDetailsProvider userDetailsProvider) {
        UserGroupPermissionChecker checker = new UserGroupPermissionChecker(userDetailsProvider);
        assertThatNoException().isThrownBy(() ->
                checker.checkUserGroupPermissions(shipmentEntity));
    }

    @ParameterizedTest
    @MethodSource("provideMismatchShipmentEntityAndUserDetails")
    void checkUserGroupPermissions_shipmentEntityCriteria_shouldThrowException(ShipmentEntity shipmentEntity,
                                                                               UserDetailsProvider userDetailsProvider) {
        UserGroupPermissionChecker checker = new UserGroupPermissionChecker(userDetailsProvider);
        assertThatThrownBy(() -> checker.checkUserGroupPermissions(shipmentEntity))
                .isInstanceOf(UserGroupNotAllowedException.class);
    }

    @ParameterizedTest
    @MethodSource("provideMatchingCostEntityAndUserDetails")
    void checkUserGroupPermissions_costEntityCriteria_shouldNotThrowException(CostEntity shipmentEntity,
                                                                              UserDetailsProvider userDetailsProvider) {
        UserGroupPermissionChecker checker = new UserGroupPermissionChecker(userDetailsProvider);
        assertThatNoException().isThrownBy(() ->
                checker.checkUserGroupPermissions(shipmentEntity));
    }

    @ParameterizedTest
    @MethodSource("provideMismatchCostEntityAndUserDetails")
    void checkUserGroupPermissions_costEntityCriteria_shouldThrowException(CostEntity shipmentEntity,
                                                                           UserDetailsProvider userDetailsProvider) {
        UserGroupPermissionChecker checker = new UserGroupPermissionChecker(userDetailsProvider);
        assertThatThrownBy(() -> checker.checkUserGroupPermissions(shipmentEntity))
                .isInstanceOf(UserGroupNotAllowedException.class);
    }
}
