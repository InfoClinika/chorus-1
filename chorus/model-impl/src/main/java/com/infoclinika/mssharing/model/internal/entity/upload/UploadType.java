package com.infoclinika.mssharing.model.internal.entity.upload;

/**
 * @author Vitalii Petkanych
 */
public enum UploadType {
    DESKTOP("Desktop", false),
    DIRECT("Direct - Local Drive/Network", true),
    S3_COPY("Amazon S3 - Copy files", true),
    S3_LINK("Amazon S3 - Make references", true),
    FTP("FTP", true);

    private final String title;
    private final boolean webEnabled;

    UploadType(String title, boolean webEnabled) {
        this.title = title;
        this.webEnabled = webEnabled;
    }

    public String getTitle() {
        return title;
    }

    public boolean isWebEnabled() {
        return webEnabled;
    }
}
