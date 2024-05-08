package com.quincus.web.common.multitenant;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.Optional;

@UtilityClass
public class SecurityContextUtil {
    
    private static final String REQUEST_URI = "requestURI";

    public static Optional<QuincusUserDetails> getQuincusUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof QuincusUserDetails quincusUserDetails) {
            return Optional.of(quincusUserDetails);
        }
        return Optional.empty();
    }

    public static String getRequestURI() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getDetails() instanceof Map)) return null;
        return ((Map<?, ?>) authentication.getDetails()).get(REQUEST_URI).toString();
    }
}

