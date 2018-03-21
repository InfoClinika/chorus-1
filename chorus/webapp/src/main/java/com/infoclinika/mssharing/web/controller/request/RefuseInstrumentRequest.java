package com.infoclinika.mssharing.web.controller.request;

/**
 * @author Oleksii Tymchenko
 */
public class RefuseInstrumentRequest {
    private long requesterId;
    private long instrumentId;
    private String comment;

    public long getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(long requesterId) {
        this.requesterId = requesterId;
    }

    public long getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(long instrumentId) {
        this.instrumentId = instrumentId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
