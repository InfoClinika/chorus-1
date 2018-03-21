package com.infoclinika.mssharing.integration.test.data.proteinsearch;

/**
 * @author Alexander Orlov
 */
public enum Algorithm {

    SIMPLE_MATH("Simple Math"),
    ANALYSIS("Analysis"),
    COMMON("Common"),
    NORMALIZATION("Normalization"),
    RATIO("Ratio"),
    LOG2("Log2"),
    LOG10("Log10"),
    LOGN("LogN"),
    CLUSTERING("Clustering"),
    Z_SCORE("z-score"),
    ANOVA("ANOVA"),
    FILTERING("Filtering"),
    SORTING("Sorting");

    private String value;

    private Algorithm(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
