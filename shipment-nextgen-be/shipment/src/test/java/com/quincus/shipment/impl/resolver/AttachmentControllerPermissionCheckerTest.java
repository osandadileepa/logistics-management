package com.quincus.shipment.impl.resolver;

import com.quincus.web.common.multitenant.QuincusUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttachmentControllerPermissionCheckerTest {

    @Test
    void testHasPermission_WithRestrictedAttachmentTypeAndPermission() {
        UserDetailsProvider userDetailsProvider = mock(UserDetailsProvider.class);
        when(userDetailsProvider.getQuincusUserDetails()).thenReturn(createQuincusUser(List.of(new SimpleGrantedAuthority("SHIPMENT_STATUS_VIEW"))));

        AttachmentControllerPermissionChecker checker = new AttachmentControllerPermissionChecker(userDetailsProvider);

        assertThat(checker.hasDownloadCsvTemplatePermission("milestone")).isTrue();
    }

    @Test
    void testHasPermission_WithRestrictedAttachmentTypeAndNoPermission() {
        UserDetailsProvider userDetailsProvider = mock(UserDetailsProvider.class);
        when(userDetailsProvider.getQuincusUserDetails()).thenReturn(createQuincusUser(List.of()));

        AttachmentControllerPermissionChecker checker = new AttachmentControllerPermissionChecker(userDetailsProvider);

        assertThat(checker.hasDownloadCsvTemplatePermission("milestone")).isFalse();
    }

    @Test
    void testHasPermission_WithUnrestrictedAttachmentType() {
        UserDetailsProvider userDetailsProvider = mock(UserDetailsProvider.class);

        AttachmentControllerPermissionChecker checker = new AttachmentControllerPermissionChecker(userDetailsProvider);

        assertThat(checker.hasDownloadCsvTemplatePermission("package-journey-air-segment")).isTrue();
        assertThat(checker.hasDownloadCsvTemplatePermission("network-lane")).isTrue();
    }

    private QuincusUser createQuincusUser(List<SimpleGrantedAuthority> authorities) {
        return new QuincusUser(
                "testuser",
                "123",
                "Test User",
                "org123",
                "Test Organization",
                "partner123",
                "Test Partner",
                authorities,
                null,
                null,
                null,
                "API"
        );
    }
}
