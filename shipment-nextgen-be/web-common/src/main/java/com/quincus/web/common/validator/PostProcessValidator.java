package com.quincus.web.common.validator;

public interface PostProcessValidator<T> {

    boolean isValid(T obj);
}
