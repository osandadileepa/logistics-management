package com.quincus.web.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticationResponse<E> {
    E data;

    public AuthenticationResponse(E e) {
        this.data = e;
    }
}