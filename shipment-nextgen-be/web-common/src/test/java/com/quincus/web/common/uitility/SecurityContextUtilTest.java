package com.quincus.web.common.uitility;

import com.quincus.web.common.multitenant.QuincusUserDetails;
import com.quincus.web.common.multitenant.SecurityContextUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityContextUtilTest {

    @Mock
    private Authentication authentication;

    @BeforeEach
    public void setUp() {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testGetQuincusUserDetails_ValidAuthentication_ShouldReturnDetails() {
        QuincusUserDetails userDetails = mock(QuincusUserDetails.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        Optional<QuincusUserDetails> result = SecurityContextUtil.getQuincusUserDetails();

        assertThat(result).isPresent().containsSame(userDetails);
    }

    @Test
    void testGetQuincusUserDetails_InvalidAuthentication_ShouldReturnEmpty() {
        when(authentication.getPrincipal()).thenReturn("INVALID");

        Optional<QuincusUserDetails> result = SecurityContextUtil.getQuincusUserDetails();

        assertThat(result).isEmpty();
    }

    @Test
    void testGetRequestURI_ValidMap_ShouldReturnURI() {
        Map<String, Object> detailsMap = new HashMap<>();
        detailsMap.put("requestURI", "/test/uri");
        when(authentication.getDetails()).thenReturn(detailsMap);

        String result = SecurityContextUtil.getRequestURI();

        assertThat(result).isEqualTo("/test/uri");
    }

    @Test
    void testGetRequestURI_InvalidMap_ShouldReturnNull() {
        when(authentication.getDetails()).thenReturn("INVALID");

        String result = SecurityContextUtil.getRequestURI();

        assertThat(result).isNull();
    }
}
