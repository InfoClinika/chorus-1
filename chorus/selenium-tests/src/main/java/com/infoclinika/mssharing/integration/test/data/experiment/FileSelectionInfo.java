package com.infoclinika.mssharing.integration.test.data.experiment;

/**
 * @author Alexander Orlov
 */
public class FileSelectionInfo {

    private int numberOfSelectedFiles;

    private FileSelectionInfo(Builder builder) {
        this.numberOfSelectedFiles = builder.numberOfSelectedFiles;
    }

    public int getNumberOfSelectedFiles() {
        return numberOfSelectedFiles;
    }

    public static class Builder {

        private int numberOfSelectedFiles;

        public Builder numberOfSelectedFiles(int numberOfSelectedFiles) {
            this.numberOfSelectedFiles = numberOfSelectedFiles;
            return this;
        }

        public FileSelectionInfo build() {
            return new FileSelectionInfo(this);
        }
    }
}
