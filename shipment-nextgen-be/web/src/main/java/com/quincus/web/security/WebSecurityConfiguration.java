package com.quincus.web.security;

import com.quincus.ext.YamlPropertySourceFactory;
import com.quincus.web.logging.LoggingFilter;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableConfigurationProperties(SecurityProperties.class)
@AllArgsConstructor
@PropertySource(value = {"classpath:config/security.yml", "classpath:config/security-${spring.profiles.active}.yml"}, factory = YamlPropertySourceFactory.class)
public class WebSecurityConfiguration {
    private final LoggingFilter loggingFilter;
    private final AuthenticationProvider authenticationProvider;
    private final SecurityProperties securityProperties;
    private final TokenResolver tokenResolver;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.cors();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.authorizeRequests()
                .antMatchers(securityProperties.getAllowed()).permitAll()
                .anyRequest().authenticated()
                .and()
                .exceptionHandling().authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));

        final JwtTokenAuthenticationFilter jwtTokenAuthenticationFilter = new JwtTokenAuthenticationFilter(authenticationProvider, tokenResolver);
        final S2STokenAuthenticationFilter s2STokenAuthenticationFilter = new S2STokenAuthenticationFilter(authenticationProvider, tokenResolver, securityProperties.getS2sTokens());
        final ActuatorAuthenticationFilter actuatorAuthenticationFilter = new ActuatorAuthenticationFilter(authenticationProvider, tokenResolver);
        http.addFilterBefore(jwtTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(s2STokenAuthenticationFilter, JwtTokenAuthenticationFilter.class);
        http.addFilterAfter(actuatorAuthenticationFilter, S2STokenAuthenticationFilter.class);
        http.addFilterBefore(loggingFilter, JwtTokenAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(securityProperties.getOrigins()));
        configuration.setAllowedMethods(Arrays.asList(securityProperties.getMethods()));
        configuration.setAllowedHeaders(Arrays.asList(securityProperties.getHeaders()));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
