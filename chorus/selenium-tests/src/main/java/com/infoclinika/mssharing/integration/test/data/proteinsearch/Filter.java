package com.infoclinika.mssharing.integration.test.data.proteinsearch;

/**
 * @author Alexander Orlov
 */
public enum Filter {

    AGGREGATED_PROTEIN_ID("Aggregated Protein ID"),
    PROTEIN_ID("Protein ID"),
    PROTEIN_DESCRIPTION("Protein Description"),
    PROTEIN_COVERAGE("Protein Coverage"),
    MOL_WT("Mol. Wt. (kDa)");

    private String value;

    private Filter(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }


}
