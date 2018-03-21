package com.infoclinika.mssharing.integration.test.data.proteinsearch;

import com.infoclinika.mssharing.integration.test.data.experiment.ExperimentData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexander Orlov
 */
public class ShotGunSearchData {

    private final String name;
    private final ExperimentData experimentData;
    private final int numberOfFiles;
    private List<String> annotationsItemsFromCsvFile = new ArrayList<>();
    private List<String> dataItemsFromCsvFile = new ArrayList<>();
    private final String composeBy;
    private final String viewLevel;
    private List<AlgorithmData> algorithmData = new ArrayList<>();

    private ShotGunSearchData(Builder builder) {
        this.name = builder.name;
        this.experimentData = builder.experimentData;
        this.numberOfFiles = builder.numberOfFiles;
        this.annotationsItemsFromCsvFile = builder.annotationItemsFromCsvFile;
        this.composeBy = builder.composeBy;
        this.viewLevel = builder.viewLevel;
        this.dataItemsFromCsvFile = builder.dataItemsFromCsvFile;
        this.algorithmData = builder.algorithmData;
    }

    public String getName() {
        return name;
    }

    public ExperimentData getExperimentData() {
        return experimentData;
    }

    public int getNumberOfFiles() {
        return numberOfFiles;
    }

    public List<String> getAnnotationsItemsFromCsvFile() {
        return annotationsItemsFromCsvFile;
    }

    public List<String> getDataItemsFromCsvFile() {
        return dataItemsFromCsvFile;
    }

    public String getComposeBy() {
        return composeBy;
    }

    public String getViewLevel() {
        return viewLevel;
    }

    public List<AlgorithmData> getAlgorithmData() {
        return algorithmData;
    }

    public static class Builder {
        private String name;
        private ExperimentData experimentData;
        private int numberOfFiles;
        private List<String> annotationItemsFromCsvFile = new ArrayList<>();
        private List<String> dataItemsFromCsvFile = new ArrayList<>();
        private String composeBy;
        private String viewLevel;
        private List<AlgorithmData> algorithmData = new ArrayList<>();

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder experimentData(ExperimentData experimentData){
            this.experimentData = experimentData;
            return this;
        }

        public Builder numberOfFiles(int numberOfFiles){
            this.numberOfFiles = numberOfFiles;
            return this;
        }

        public Builder annotationItemsFromCsvFile(List<String> annotationItemsFromCsvFile){
            this.annotationItemsFromCsvFile = annotationItemsFromCsvFile;
            return this;
        }

        public Builder dataItemsFromCsvFile(List<String> dataItemsFromCsvFile){
            this.dataItemsFromCsvFile = dataItemsFromCsvFile;
            return this;
        }

        public Builder composeBy(String value){
            this.composeBy = value;
            return this;
        }

        public Builder viewLevel(String value){
            this.viewLevel = value;
            return this;
        }

        public Builder algorithmData(List<AlgorithmData> algorithmData){
            this.algorithmData = algorithmData;
            return this;
        }

        public ShotGunSearchData build() {
            return new ShotGunSearchData(this);
        }

    }

    @Override
    public String toString() {
        return "ShotGunSearchData{" +
                "name='" + name + '\'' +
                ", experimentData=" + experimentData +
                ", numberOfFiles=" + numberOfFiles +
                ", annotationsItemsFromCsvFile=" + annotationsItemsFromCsvFile +
                ", dataItemsFromCsvFile=" + dataItemsFromCsvFile +
                ", composeBy='" + composeBy + '\'' +
                ", viewLevel='" + viewLevel + '\'' +
                ", algorithmData=" + algorithmData +
                '}';
    }
}
