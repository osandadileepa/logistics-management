package com.quincus.web.controller;

import com.quincus.web.security.AuthUser;
import com.quincus.web.security.JwtTokenProvider;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = {"/sessions"})
public class SessionController {

    private final JwtTokenProvider provider;

    public SessionController(JwtTokenProvider provider) {
        this.provider = provider;
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE})
    public String create(@RequestBody final AuthUser authUser) {
        return provider.createToken(authUser);
    }

}