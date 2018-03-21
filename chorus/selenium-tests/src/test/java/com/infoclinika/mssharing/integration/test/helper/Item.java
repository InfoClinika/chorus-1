package com.infoclinika.mssharing.integration.test.helper;

/**
 * @author Alexander Orlov
 */
public class Item <T>{

    private String name;
    private T data;

    public Item(String name, T data) {
        this.name = name;
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
