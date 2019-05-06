package com.infoclinika.mssharing.model.features;

/**
 * Represents pluggable components of application which could be safely turned on/off
 *
 * @author Andrii Loboda
 */
public enum ApplicationFeature {
    GLACIER("glacier"),
    BILLING("billing"),
    BLOG("blog"),
    EDITABLE_COLUMNS("editableColumns"),
    SUBSCRIBE("subscribe"),
    MICROARRAYS("microArrays"),
    ISA_TAB_EXPORT("isaTabExport"),
    ALIS("alis"),
    LTQ("ltq"),
    PARSE_RULES_SELECTION("parseRulesSelection");

    private final String featureName;

    ApplicationFeature(String featureName) {
        this.featureName = featureName;
    }

    public String getFeatureName() {
        return this.featureName;
    }

    public static ApplicationFeature ofName(String featureName) {
        for (ApplicationFeature applicationFeature : ApplicationFeature.values()) {
            if (applicationFeature.getFeatureName().equals(featureName)) {
                return applicationFeature;
            }
        }
        throw new RuntimeException("Application feature " + featureName + " doesn't exist");
    }
}
