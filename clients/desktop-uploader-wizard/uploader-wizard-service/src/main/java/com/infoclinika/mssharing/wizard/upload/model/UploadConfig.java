package com.infoclinika.mssharing.wizard.upload.model;

import com.infoclinika.mssharing.dto.response.InstrumentDTO;
import com.infoclinika.mssharing.upload.common.transfer.api.Uploader;

/**
 * @author timofey.kasyanov
 *         date:   28.01.14
 */
public class UploadConfig {

    private Uploader uploader;
    private final String bucket;
    private InstrumentDTO instrument;
    private final ZipConfig zipConfig;

    public UploadConfig(String bucket, ZipConfig zipConfig) {
        this.bucket = bucket;
        this.zipConfig = zipConfig;
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

    public void setUploader(Uploader uploader) {
        this.uploader = uploader;
    }

    public String getBucket() {
        return bucket;
    }
}
