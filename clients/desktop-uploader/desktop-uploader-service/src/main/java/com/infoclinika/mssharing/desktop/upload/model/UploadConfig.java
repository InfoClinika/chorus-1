package com.infoclinika.mssharing.desktop.upload.model;

import com.infoclinika.mssharing.clients.common.transfer.api.Uploader;
import com.infoclinika.mssharing.clients.common.util.PauseSemaphore;
import com.infoclinika.mssharing.dto.response.InstrumentDTO;

/**
 * @author timofey.kasyanov
 *     date:   28.01.14
 */
public class UploadConfig {

    private final Uploader uploader;
    private final String bucket;
    private InstrumentDTO instrument;
    private final ZipConfig zipConfig;
    private final PauseSemaphore pauseSemaphore;

    public UploadConfig(String bucket, ZipConfig zipConfig, Uploader uploader, PauseSemaphore pauseSemaphore) {
        this.bucket = bucket;
        this.zipConfig = zipConfig;
        this.uploader = uploader;
        this.pauseSemaphore = pauseSemaphore;
    }

    public ZipConfig getZipConfig() {
        return zipConfig;
    }

    public void setInstrument(InstrumentDTO instrument) {
        this.instrument = instrument;
    }

    public InstrumentDTO getInstrument() {
        return instrument;
    }

    public Uploader getUploader() {
        return uploader;
    }

    public PauseSemaphore getPauseSemaphore() {
        return pauseSemaphore;
    }

    public String getBucket() {
        return bucket;
    }
}
