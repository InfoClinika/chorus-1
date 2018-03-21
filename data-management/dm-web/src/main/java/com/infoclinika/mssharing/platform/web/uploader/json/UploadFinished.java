package com.infoclinika.mssharing.platform.web.uploader.json;

import java.util.UUID;

/**
 * @author Pavel Kaplin
 */
public class UploadFinished {
    private UUID file;

    public UploadFinished(UUID file) {
        this.file = file;
    }

    public String getAction() {
        return "complete";
    }

    public UUID getFile() {
        return file;
    }
}
