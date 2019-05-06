package com.infoclinika.mssharing.web.controller.request;

/**
 * @author timofey.kasyanov
 *     date: 08.05.2014
 */
public class CheckUploadLimitRequest {
    private long instrumentId;
    private long bytesToUpload;
    private long labId;

    public long getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(long instrumentId) {
        this.instrumentId = instrumentId;
    }

    public long getBytesToUpload() {
        return bytesToUpload;
    }

    public void setBytesToUpload(long bytesToUpload) {
        this.bytesToUpload = bytesToUpload;
    }

    public long getLabId() {
        return labId;
    }

    public void setLabId(long labId) {
        this.labId = labId;
    }
}
