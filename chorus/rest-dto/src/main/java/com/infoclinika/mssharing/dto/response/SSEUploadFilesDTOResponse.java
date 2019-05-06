package com.infoclinika.mssharing.dto.response;

import java.util.List;

public class SSEUploadFilesDTOResponse {
    private long instrument;
    private List<UploadFileItem> files;

    public SSEUploadFilesDTOResponse() {
    }

    public SSEUploadFilesDTOResponse(long instrument, List<UploadFileItem> files) {
        this.instrument = instrument;
        this.files = files;
    }

    public long getInstrument() {
        return instrument;
    }

    public void setInstrument(long instrument) {
        this.instrument = instrument;
    }

    public List<UploadFileItem> getFiles() {
        return files;
    }

    public void setFiles(List<UploadFileItem> files) {
        this.files = files;
    }

    public static class UploadFileItem {
        private String path;
        private String authorization;
        private String formattedDate;
        private boolean sseEnabled;

        public UploadFileItem() {
        }

        public UploadFileItem(String path, String authorization, String formattedDate, boolean sseEnabled) {
            this.path = path;
            this.authorization = authorization;
            this.formattedDate = formattedDate;
            this.sseEnabled = sseEnabled;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getAuthorization() {
            return authorization;
        }

        public void setAuthorization(String authorization) {
            this.authorization = authorization;
        }

        public String getFormattedDate() {
            return formattedDate;
        }

        public void setFormattedDate(String formattedDate) {
            this.formattedDate = formattedDate;
        }

        public boolean isSseEnabled() {
            return sseEnabled;
        }

        public void setSseEnabled(boolean sseEnabled) {
            this.sseEnabled = sseEnabled;
        }
    }

}
