package com.infoclinika.mssharing.integration.test.data.experiment;

/**
 * @author Alexander Orlov
 */
public enum FactorType {

    TEXT("Text"),
    NUMBER("Number");

    private String value;

    private FactorType(String value) {
        this.value = value;
    }

    public String getName() {
        return value;
    }
}
