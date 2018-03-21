package com.infoclinika.mssharing.integration.test.data.proteinsearch;

/**
 * @author Alexander Orlov
 */
public enum FilterBy {

    ROW_ANNOTATIONS("Row Annotations"),
    COLUMN_ANNOTATIONS("Column Annotations");

    private String value;

    private FilterBy (String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
