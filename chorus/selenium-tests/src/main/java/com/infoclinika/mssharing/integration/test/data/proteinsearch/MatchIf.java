package com.infoclinika.mssharing.integration.test.data.proteinsearch;

/**
 * @author Alexander Orlov
 */
public enum MatchIf {

    EQUALS("equals"),
    DOESNT_EQUAL("doesn't equal"),
    GREATER_THAN("greater than"),
    LESS_THAN("less than");

    private String value;

    private MatchIf(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
