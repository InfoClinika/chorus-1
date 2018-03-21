package com.infoclinika.mssharing.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author timofey.kasyanov
 *         date: 12.02.14.
 */
public class SimpleUploadFilesDTOResponse {

    private long instrument;
    private List<UploadFilePath> files;

    public SimpleUploadFilesDTOResponse(){}

    public SimpleUploadFilesDTOResponse(long instrument, List<UploadFilePath> files) {

        this.instrument = instrument;
        this.files = files;
    }

    public long getInstrument() {
        return instrument;
    }

    public void setInstrument(long instrument) {
        this.instrument = instrument;
    }

    public List<UploadFilePath> getFiles() {
        return files;
    }

    public void setFiles(List<UploadFilePath> files) {
        this.files = files;
    }

    public static class UploadFilePath {

        private String path;

        public UploadFilePath(){}

        public UploadFilePath(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

}
