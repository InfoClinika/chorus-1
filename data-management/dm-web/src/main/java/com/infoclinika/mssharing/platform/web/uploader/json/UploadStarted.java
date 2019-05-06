package com.infoclinika.mssharing.platform.web.uploader.json;

import java.util.UUID;

/**
 * @author Pavel Kaplin
 */
public class UploadStarted {
    private UUID fileid;

    public UploadStarted(UUID fileid) {
        this.fileid = fileid;
    }

    public String getAction() {
        return "new_upload";
    }

    public UUID getFileid() {
        return fileid;
    }
}
