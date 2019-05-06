package com.infoclinika.mssharing.platform.web.uploader;

public class FileDetails {
    private final String fileName;
    private final long size;

    public FileDetails(String fileName, long size) {
        this.fileName = fileName;
        this.size = size;
    }

    public String getName() {
        return fileName;
    }

    public long getSize() {
        return size;
    }
}
