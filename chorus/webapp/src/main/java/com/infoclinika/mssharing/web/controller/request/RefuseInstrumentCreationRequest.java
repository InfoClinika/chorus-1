package com.infoclinika.mssharing.web.controller.request;

/**
 * @author timofey.kasyanov
 *         date: 13.05.2014
 */
public class RefuseInstrumentCreationRequest {
    private long requestId;
    private String comment;

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
