package com.infoclinika.mssharing.dto;

public class FileDescription {
    public String fileName;
    public boolean directory;
    public boolean readyToUpload;

    public FileDescription() {
    }

    public FileDescription(String fileName, boolean directory, boolean readyToUpload) {
        this.fileName = fileName;
        this.directory = directory;
        this.readyToUpload = readyToUpload;
    }
}
