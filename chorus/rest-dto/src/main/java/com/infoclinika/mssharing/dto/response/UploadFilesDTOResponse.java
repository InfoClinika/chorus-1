package com.infoclinika.mssharing.dto.response;

import java.util.List;

/**
 * Holds meta information about the files being uploaded.
 * Does NOT know anything about binary content. Binary content is uploaded separately.
 * <p/>
 * author: Ruslan Duboveckij
 */
public class UploadFilesDTOResponse {

    private long instrument;
    private List<UploadFile> files;

    public UploadFilesDTOResponse() {
    }

    public UploadFilesDTOResponse(long instrument, List<UploadFile> files) {
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

    @Override
    public String toString() {
        return "UploadFilesDTOResponse{" +
            "instrument=" + instrument +
            ", files=" + files +
            '}';
    }

    public static class UploadFile {
        private String name;
        private long fileId;
        private String contentId;
        private boolean started;

        public UploadFile() {
        }

        public UploadFile(String name,
                          long fileId,
                          String contentId) {
            this.name = name;
            this.fileId = fileId;
            this.contentId = contentId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getFileId() {
            return fileId;
        }

        public void setFileId(long fileId) {
            this.fileId = fileId;
        }

        public String getContentId() {
            return contentId;
        }

        public void setContentId(String contentId) {
            this.contentId = contentId;
        }

        public boolean isStarted() {
            return started;
        }

        public void setStarted(boolean started) {
            this.started = started;
        }

        @Override
        public String toString() {
            return "UploadFile{" +
                "name='" + name + '\'' +
                ", fileId=" + fileId +
                ", contentId='" + contentId + '\'' +
                '}';
        }
    }
}
