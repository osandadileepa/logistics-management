package com.quincus.web;

import com.quincus.authentication.model.AuthenticationUser;
import com.quincus.web.dto.AuthenticationRequest;
import com.quincus.web.dto.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.constraints.NotNull;

@RequestMapping(path = "/sessions")
@Tag(name = "sessions", description = "Endpoint for managing authentication sessions.")
public interface SessionController {

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create Session API", description = "Create a new session.", tags = "sessions")
    Response<AuthenticationUser> create(@RequestBody @NotNull final AuthenticationRequest authenticationRequest);

}
