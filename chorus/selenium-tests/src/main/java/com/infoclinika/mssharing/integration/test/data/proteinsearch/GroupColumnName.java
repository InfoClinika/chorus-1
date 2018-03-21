package com.infoclinika.mssharing.integration.test.data.proteinsearch;

/**
 * @author Alexander Orlov
 */
public enum GroupColumnName {

    CONDITION_ID("Condition ID"),
    CONDITION_NAME("Condition Name"),
    FILE_ID("File ID"),
    FILE_NAME("File Name"),
    FACTOR_CONCENTRATION("Factor:Concentration");

    private String value;

    private GroupColumnName (String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
