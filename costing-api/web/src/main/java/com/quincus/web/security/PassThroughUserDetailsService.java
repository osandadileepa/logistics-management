package com.quincus.web.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class PassThroughUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final GrantedAuthority[] authorities = new GrantedAuthority[]{
                () -> "ADMIN"
        };

        return User//
                .withUsername(username)//
                .password(UUID.randomUUID().toString())//
                .authorities(authorities)//
                .accountExpired(false)//
                .accountLocked(false)//
                .credentialsExpired(false)//
                .disabled(false)//
                .build();
    }

    public UserDetails loadUserFromAuth(String username, List<SimpleGrantedAuthority> auth) {
        return User//
                .withUsername(username)//
                .password(UUID.randomUUID().toString())//
                .authorities(auth)//
                .accountExpired(false)//
                .accountLocked(false)//
                .credentialsExpired(false)//
                .disabled(false)//
                .build();
    }
}