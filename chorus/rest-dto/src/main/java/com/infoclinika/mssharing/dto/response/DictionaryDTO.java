package com.infoclinika.mssharing.dto.response;

import java.io.Serializable;

/**
 * author: Ruslan Duboveckij
 */
public class DictionaryDTO implements Serializable {
    private long id;
    private String name;

    public DictionaryDTO() {
    }

    public DictionaryDTO(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DictionaryDTO that = (DictionaryDTO) o;

        if (id != that.id) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "DictionaryDTO{" +
            "id=" + id +
            ", name='" + name + '\'' +
            '}';
    }
}

