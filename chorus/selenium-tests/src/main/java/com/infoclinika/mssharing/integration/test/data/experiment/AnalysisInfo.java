package com.infoclinika.mssharing.integration.test.data.experiment;

/**
 * @author Alexander Orlov
 */
public class AnalysisInfo {

    private final String experimentType;
    private final boolean is2Dlc;
    private TranslationRange translationRange;

    private AnalysisInfo(Builder builder) {
        this.experimentType = builder.experimentType;
        this.is2Dlc = builder.is2Dlc;
        this.translationRange = builder.translationRange;
    }

    public String getExperimentType() {
        return experimentType;
    }


    public boolean is2Dlc() {
        return is2Dlc;
    }

    public TranslationRange getTranslationRange() {
        return translationRange;
    }

    public static class Builder {
        private String experimentType;
        private boolean is2Dlc;
        private TranslationRange translationRange;

        public Builder experimentType(String experimentType) {
            this.experimentType = experimentType;
            return this;
        }


        public Builder is2Dlc(boolean is2Dlc) {
            this.is2Dlc = is2Dlc;
            return this;
        }

        public Builder translationRange(TranslationRange translationRange) {
            this.translationRange = translationRange;
            return this;
        }

        public AnalysisInfo build() {
            return new AnalysisInfo(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AnalysisInfo that = (AnalysisInfo) o;

        if (is2Dlc != that.is2Dlc) return false;
        if (experimentType != null ? !experimentType.equals(that.experimentType) : that.experimentType != null)
            return false;
        if (translationRange != null ? !translationRange.equals(that.translationRange) : that.translationRange != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = experimentType != null ? experimentType.hashCode() : 0;
        result = 31 * result + (is2Dlc ? 1 : 0);
        result = 31 * result + (translationRange != null ? translationRange.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AnalysisInfo{" +
                "experimentType='" + experimentType + '\'' +
                ", is2Dlc=" + is2Dlc +
                ", translationRange=" + translationRange +
                '}';
    }
}
