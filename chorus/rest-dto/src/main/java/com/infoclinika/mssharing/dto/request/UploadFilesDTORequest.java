package com.infoclinika.mssharing.dto.request;

import java.util.List;

/**
 * Holds meta information about the files being uploaded.
 * Does NOT know anything about binary content. Binary content is uploaded separately.
 * <p/>
 * author: Ruslan Duboveckij
 */
public class UploadFilesDTORequest {

    private long instrument;
    private List<UploadFile> files;

    public UploadFilesDTORequest() {
    }

    public UploadFilesDTORequest(long instrument, List<UploadFile> files) {
        this.instrument = instrument;
        this.files = files;
    }

    public long getInstrument() {
        return instrument;
    }

    public void setInstrument(long instrument) {
        this.instrument = instrument;
    }

    public List<UploadFile> getFiles() {
        return files;
    }

    public void setFiles(List<UploadFile> files) {
        this.files = files;
    }

    public static class UploadFile {
        private String name;
        private String labels;
        private long size;
        private long specie;
        private boolean archive;

        public UploadFile() {
        }

        public UploadFile(String name,
                          String labels,
                          long size,
                          long specie,
                          boolean archive) {

            this.name = name;
            this.labels = labels;
            this.size = size;
            this.specie = specie;
            this.archive = archive;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLabels() {
            return labels;
        }

        public void setLabels(String labels) {
            this.labels = labels;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public long getSpecie() {
            return specie;
        }

        public void setSpecie(long specie) {
            this.specie = specie;
        }

        public boolean isArchive() {
            return archive;
        }

        public void setArchive(boolean archive) {
            this.archive = archive;
        }
    }
}
