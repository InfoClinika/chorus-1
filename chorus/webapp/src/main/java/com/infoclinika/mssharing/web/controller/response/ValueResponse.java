package com.infoclinika.mssharing.web.controller.response;

/**
 * @author Herman Zamula
 */
public class ValueResponse<T> {

    public T value;

    public ValueResponse(T value) {
        this.value = value;
    }
}
