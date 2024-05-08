package com.quincus.web.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Response<E> {
    E data;

    public Response(E e) {
        this.data = e;
    }
}