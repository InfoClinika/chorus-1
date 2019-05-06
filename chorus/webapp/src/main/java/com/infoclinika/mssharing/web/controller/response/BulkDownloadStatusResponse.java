package com.infoclinika.mssharing.web.controller.response;

/**
 * @author Alexei Tymchenko
 */
public class BulkDownloadStatusResponse {
    public final long requestId;
    public final boolean completed;
    public final int percentage;
    public final boolean interrupted;

    public BulkDownloadStatusResponse(long requestId, boolean completed, int percentage, boolean interrupted) {
        this.requestId = requestId;
        this.completed = completed;
        this.percentage = percentage;
        this.interrupted = interrupted;
    }

    @Override
    public String toString() {
        return "BulkDownloadStatusResponse{" +
            "requestId=" + requestId +
            ", completed=" + completed +
            ", percentage=" + percentage +
            ", interrupted=" + interrupted +
            '}';
    }
}
