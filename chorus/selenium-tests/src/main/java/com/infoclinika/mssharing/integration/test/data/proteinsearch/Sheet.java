package com.infoclinika.mssharing.integration.test.data.proteinsearch;

/**
 * @author Alexander Orlov
 */
public enum Sheet {

    INTENSITIES_SHEET("intensitiesSheet"),
    RT_SHEET("rtSheet"),
    COVERAGE_SHEET("coverageSheet"),
    Q_VALUES_SHEET("qvaluesSheet");

    private String value;

    private Sheet (String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
