package com.quincus.shipment.impl.aspect;

import com.quincus.shipment.impl.resolver.AttachmentControllerPermissionChecker;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttachmentControllerAspectTest {

    @InjectMocks
    private AttachmentControllerAspect aspect;

    @Mock
    private AttachmentControllerPermissionChecker permissionChecker;

    @Mock
    private JoinPoint joinPoint;

    @Test
    void testCheckDownloadAttachmentPermission_whenHasPermission() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{"milestone"});
        when(permissionChecker.hasDownloadCsvTemplatePermission(anyString())).thenReturn(true);
        aspect.checkDownloadCsvTemplatePermission(joinPoint);
        verify(permissionChecker, times(1)).hasDownloadCsvTemplatePermission("milestone");
    }

    @Test
    void testCheckDownloadAttachmentPermission_whenNoPermission() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{"milestone"});
        when(permissionChecker.hasDownloadCsvTemplatePermission(anyString())).thenReturn(false);

        assertThatThrownBy(() -> aspect.checkDownloadCsvTemplatePermission(joinPoint))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Access denied for attachmentType `milestone`");

        verify(permissionChecker, times(1)).hasDownloadCsvTemplatePermission("milestone");
    }

}
