package com.infoclinika.mssharing.model.features;

/**
 * Represents pluggable components of application which could be safely turned on/off
 *
 * @author Andrii Loboda
 */
public enum ApplicationFeature {
    PROTEIN_ID_SEARCH("proteinIdSearch"),
    PROTEIN_ID_SEARCH_RESULTS("proteinIdSearchResults"),
    GLACIER("glacier"),
    BILLING("billing"),
    BLOG("blog"),
    EDITABLE_COLUMNS("editableColumns"),
    SUBSCRIBE("subscribe"),
    MICROARRAYS("microArrays"),
    TRANSLATION("translation"),
    ISA_TAB_EXPORT("isaTabExport");

    private final String featureName;

    ApplicationFeature(String featureName) {
        this.featureName = featureName;
    }

    public String getFeatureName() {
        return this.featureName;
    }

    public static ApplicationFeature ofName(String featureName) {
        for (ApplicationFeature applicationFeature : ApplicationFeature.values()) {
            if(applicationFeature.getFeatureName().equals(featureName)) {
                return applicationFeature;
            }
        }
        throw new RuntimeException("Application feature " + featureName + " doesn't exist");
    }
}
