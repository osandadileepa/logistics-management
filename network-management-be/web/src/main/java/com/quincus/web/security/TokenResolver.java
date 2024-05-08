package com.quincus.web.security;

import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class TokenResolver {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER = "Bearer ";
    private static final String X_API_AUTHORIZATION_HEADER = "X-API-AUTHORIZATION";
    private static final String ORGANIZATION_ID_HEADER = "X-ORGANISATION-ID";


    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (bearerToken != null && bearerToken.startsWith(BEARER)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public String resolveS2SToken(HttpServletRequest request) {
        return request.getHeader(X_API_AUTHORIZATION_HEADER);
    }

    public String resolveOrganizationId(HttpServletRequest request) {
        return request.getHeader(ORGANIZATION_ID_HEADER);
    }
}
