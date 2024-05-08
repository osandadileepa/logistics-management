package com.quincus.networkmanagement.impl.aspect;

import com.quincus.web.security.TenantHandlerInterceptor;
import lombok.AllArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@AllArgsConstructor
public class TenantAsyncAspect {
    private final TenantHandlerInterceptor tenantHandlerInterceptor;

    @Around("@annotation(org.springframework.scheduling.annotation.Async)")
    public Object applyOrganizationFilter(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            tenantHandlerInterceptor.applyOrganizationFilter();
            return joinPoint.proceed();
        } finally {
            tenantHandlerInterceptor.disableOrganizationFilter();
        }
    }
}
