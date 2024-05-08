package com.quincus.web.security;

import com.quincus.web.common.multitenant.UserDetailsContextHolder;
import org.hibernate.Session;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class TenantHandlerInterceptor implements AsyncHandlerInterceptor {

    private static final String ORGANIZATION_FILTER = "organizationFilter";
    private static final String ORGANIZATION_ID_PARAM = "organizationId";
    private final UserDetailsContextHolder userDetailsContextHolder = new UserDetailsContextHolder();
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        applyOrganizationFilter();
        return true;
    }

    @Override
    @Transactional
    public void afterConcurrentHandlingStarted(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        disableOrganizationFilter();
    }

    public void applyOrganizationFilter() {
        if (userDetailsContextHolder.isQuincusUserDetailsPresent()) {
            Session session = entityManager.unwrap(Session.class);
            session.enableFilter(ORGANIZATION_FILTER).setParameter(ORGANIZATION_ID_PARAM, userDetailsContextHolder.getCurrentOrganizationId());
        }
    }

    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, ModelAndView modelAndView) {
        disableOrganizationFilter();
    }

    public void disableOrganizationFilter() {
        if (userDetailsContextHolder.isQuincusUserDetailsPresent()) {
            Session session = entityManager.unwrap(Session.class);
            session.disableFilter(ORGANIZATION_FILTER);
        }
    }
}