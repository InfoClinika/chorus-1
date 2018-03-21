package com.infoclinika.mssharing.integration.test.data.proteinsearch;

/**
 * @author Alexander Orlov
 */
public enum Direction {

    ASC("Asc"),
    DESC("Desc");

    private String value;

    private Direction (String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
