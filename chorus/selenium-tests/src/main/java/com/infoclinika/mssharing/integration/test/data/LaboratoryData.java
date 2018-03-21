package com.infoclinika.mssharing.integration.test.data;

/**
 * @author Sergii Moroz
 */
public class LaboratoryData {

    private final String institutionUrl;
    private final String laboratoryName;
    private final String contactEmail;
    private final String labHeadFirstName;
    private final String labHeadLastName;
    private final String labHeadEmail;

    private LaboratoryData(Builder builder) {
        this.institutionUrl = builder.institutionUrl;
        this.laboratoryName = builder.laboratoryName;
        this.contactEmail = builder.contactEmail;
        this.labHeadFirstName = builder.labHeadFirstName;
        this.labHeadLastName = builder.labHeadLastName;
        this.labHeadEmail = builder.labHeadEmail;
    }

    public String getInstitutionUrl() {
        return institutionUrl;
    }

    public String getLaboratoryName() {
        return laboratoryName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getLabHeadFirstName() {
        return labHeadFirstName;
    }

    public String getLabHeadLastName() {
        return labHeadLastName;
    }

    public String getLabHeadEmail() {
        return labHeadEmail;
    }

    public static class Builder {
        private String institutionUrl;
        private String laboratoryName;
        private String contactEmail;
        private String labHeadFirstName;
        private String labHeadLastName;
        private String labHeadEmail;

        public Builder institutionUrl(String institutionUrl) {
            this.institutionUrl = institutionUrl;
            return this;
        }

        public Builder laboratoryName(String laboratoryName) {
            this.laboratoryName = laboratoryName;
            return this;
        }

        public Builder contactEmail(String contactEmail) {
            this.contactEmail = contactEmail;
            return this;
        }

        public Builder labHeadFirstName(String labHeadFirstName) {
            this.labHeadFirstName = labHeadFirstName;
            return this;
        }

        public Builder labHeadLastName(String labHeadLastName) {
            this.labHeadLastName = labHeadLastName;
            return this;
        }

        public Builder labHeadEmail(String labHeadEmail) {
            this.labHeadEmail = labHeadEmail;
            return this;
        }

        public LaboratoryData build() {
            return new LaboratoryData(this);
        }
    }
}
