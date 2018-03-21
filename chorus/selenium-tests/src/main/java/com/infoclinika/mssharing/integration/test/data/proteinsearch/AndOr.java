package com.infoclinika.mssharing.integration.test.data.proteinsearch;

/**
 * @author Alexander Orlov
 */
public enum AndOr {

    AND("And"),
    OR("OR");

    private String value;

    private AndOr (String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }


}
