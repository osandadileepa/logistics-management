package com.quincus.shipment.impl.aspect;

import com.quincus.shipment.impl.resolver.AttachmentControllerPermissionChecker;
import lombok.AllArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Aspect
@Component
@AllArgsConstructor
public class AttachmentControllerAspect {

    private final AttachmentControllerPermissionChecker attachmentControllerPermissionChecker;

    @Before("@annotation(com.quincus.shipment.impl.aspect.AttachmentDownloadCsvTemplatePermission)")
    public void checkDownloadCsvTemplatePermission(final JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();

        String attachmentType = (String) args[0];

        if (!attachmentControllerPermissionChecker.hasDownloadCsvTemplatePermission(attachmentType)) {
            throw new AccessDeniedException(String.format("Access denied for attachmentType `%s`", attachmentType));
        }
    }
}
