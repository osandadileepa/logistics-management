package com.quincus.web.common.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;

@Getter
@Setter
public class Container<E> {

    @Valid
    private E data;

}
