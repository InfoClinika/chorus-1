package com.infoclinika.mssharing.integration.test.data.experiment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sergii Moroz
 */
public class ExperimentData {

    private GeneralInfo generalInfo;
    private AnalysisInfo analysisInfo;
    private FileToPrepInfo fileToPrepInfo;
    private List<ExperimentDesignInfo> experimentDesignInfo = new ArrayList<>();
    private FileSelectionInfo fileSelectionInfo;

    public ExperimentData(Builder builder) {
        this.generalInfo = builder.generalInfo;
        this.analysisInfo = builder.analysisInfo;
        this.fileToPrepInfo = builder.fileToPrepInfo;
        this.experimentDesignInfo = builder.experimentDesignInfo;
        this.fileSelectionInfo = builder.fileSelectionInfo;
    }

    public GeneralInfo getGeneralInfo() {
        return generalInfo;
    }

    public AnalysisInfo getAnalysisInfo() {
        return analysisInfo;
    }

    public List<ExperimentDesignInfo> getExperimentDesignInfo() {
        return experimentDesignInfo;
    }

    public FileSelectionInfo getFileSelectionInfo() {
        return fileSelectionInfo;
    }

    public FileToPrepInfo getFileToPrepInfo() {
        return fileToPrepInfo;
    }

    public static class Builder {
        private GeneralInfo generalInfo;
        private AnalysisInfo analysisInfo;
        private FileToPrepInfo fileToPrepInfo;
        private List<ExperimentDesignInfo> experimentDesignInfo = new ArrayList<>();
        private FileSelectionInfo fileSelectionInfo;

        public Builder generalInfo(GeneralInfo generalInfo) {
            this.generalInfo = generalInfo;
            return this;
        }

        public Builder analysisInfo(AnalysisInfo analysisInfo) {
            this.analysisInfo = analysisInfo;
            return this;
        }

        public Builder experimentDesignInfo(List<ExperimentDesignInfo> experimentDesignInfo) {
            this.experimentDesignInfo = experimentDesignInfo;
            return this;
        }

        public Builder fileSelectionInfo(FileSelectionInfo fileSelectionInfo) {
            this.fileSelectionInfo = fileSelectionInfo;
            return this;
        }

        public Builder fileToPrepInfo (FileToPrepInfo fileToPrepInfo){
            this.fileToPrepInfo = fileToPrepInfo;
            return this;
        }

        public ExperimentData build() {
            return new ExperimentData(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExperimentData that = (ExperimentData) o;

        if (generalInfo != null ? !generalInfo.equals(that.generalInfo) : that.generalInfo != null) return false;
        if (analysisInfo != null ? !analysisInfo.equals(that.analysisInfo) : that.analysisInfo != null) return false;
        if (fileToPrepInfo != null ? !fileToPrepInfo.equals(that.fileToPrepInfo) : that.fileToPrepInfo != null)
            return false;
        if (experimentDesignInfo != null ? !experimentDesignInfo.equals(that.experimentDesignInfo) : that.experimentDesignInfo != null)
            return false;
        return fileSelectionInfo != null ? fileSelectionInfo.equals(that.fileSelectionInfo) : that.fileSelectionInfo == null;

    }

    @Override
    public int hashCode() {
        int result = generalInfo != null ? generalInfo.hashCode() : 0;
        result = 31 * result + (analysisInfo != null ? analysisInfo.hashCode() : 0);
        result = 31 * result + (fileToPrepInfo != null ? fileToPrepInfo.hashCode() : 0);
        result = 31 * result + (experimentDesignInfo != null ? experimentDesignInfo.hashCode() : 0);
        result = 31 * result + (fileSelectionInfo != null ? fileSelectionInfo.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ExperimentData{" +
                "generalInfo=" + generalInfo +
                ", analysisInfo=" + analysisInfo +
                ", fileToPrepInfo=" + fileToPrepInfo +
                ", experimentDesignInfo=" + experimentDesignInfo +
                ", fileSelectionInfo=" + fileSelectionInfo +
                '}';
    }
}
