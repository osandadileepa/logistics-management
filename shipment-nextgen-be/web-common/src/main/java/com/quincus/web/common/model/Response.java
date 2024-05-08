package com.quincus.web.common.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Response<E> extends Container<E> {

    private String status;
    private String message;
    private List<String> errors;

    private String id;

    public Response(E e) {
        setData(e);
    }

}
