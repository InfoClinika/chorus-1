package com.infoclinika.mssharing.integration.test.data.proteinsearch;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexander Orlov
 */
public class AlgorithmData {

    private final Algorithm algorithm;
    private final Sheet sheet;
    private final boolean featureZScore;
    private final boolean sampleZScore;
    private final boolean runFDR;
    private final GroupColumnName groupColumnName;

    private List<FilterSettingsData> filterSettingsData = new ArrayList<>();
    private final SortingSettingsData sortingSettingsData;

    private AlgorithmData(Builder builder) {
        this.algorithm = builder.algorithm;
        this.sheet = builder.sheet;
        this.filterSettingsData = builder.filterSettingsData;
        this.sortingSettingsData = builder.sortingSettingsData;
        this.featureZScore = builder.featureZScore;
        this.sampleZScore = builder.sampleZScore;
        this.runFDR = builder.runFDR;
        this.groupColumnName = builder.groupColumnName;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public Sheet getSheet() {
        return sheet;
    }

    public List<FilterSettingsData> getFilterSettingsData() {
        return filterSettingsData;
    }

    public SortingSettingsData getSortingSettingsData() {
        return sortingSettingsData;
    }

    public boolean isFeatureZScore() {
        return featureZScore;
    }

    public boolean isSampleZScore() {
        return sampleZScore;
    }

    public boolean isRunFDR() {
        return runFDR;
    }

    public GroupColumnName getGroupColumnName() {
        return groupColumnName;
    }

    public static class Builder {

        private Algorithm algorithm;
        private Sheet sheet;
        private List<FilterSettingsData> filterSettingsData = new ArrayList<>();
        private SortingSettingsData sortingSettingsData;
        private boolean featureZScore;
        private boolean sampleZScore;
        private boolean runFDR;
        private GroupColumnName groupColumnName;

        public Builder algorithm(Algorithm algorithm) {
            this.algorithm = algorithm;
            return this;
        }

        public Builder sheet(Sheet sheet) {
            this.sheet = sheet;
            return this;
        }

        public Builder filterSettingsData(List<FilterSettingsData> filterSettingsData) {
            this.filterSettingsData = filterSettingsData;
            return this;
        }

        public Builder sortingSettingsData(SortingSettingsData sortingSettingsData) {
            this.sortingSettingsData = sortingSettingsData;
            return this;
        }

        public Builder filterZScore(boolean filterZScore) {
            this.featureZScore = filterZScore;
            return this;
        }

        public Builder sampleZScore(boolean sampleZScore) {
            this.sampleZScore = sampleZScore;
            return this;
        }

        public Builder runFDR(boolean runFDR) {
            this.runFDR = runFDR;
            return this;
        }

        public Builder groupColumnName(GroupColumnName groupColumnName) {
            this.groupColumnName = groupColumnName;
            return this;
        }

        public AlgorithmData build() {
            return new AlgorithmData(this);
        }

    }
}
