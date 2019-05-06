package com.infoclinika.mssharing.web.controller.v2.dto;

import com.infoclinika.mssharing.model.internal.entity.upload.UploadType;

import java.util.List;

/**
 * @author Vitalii Petkanych
 */
public class UploadUpdateDTO {
    private String id;
    private long instrumentId;
    private boolean done;
    private UploadType type;
    private List<String> files;

    public UploadUpdateDTO() {
    }

    public UploadUpdateDTO(
        String id,
        long instrumentId,
        boolean done,
        UploadType type,
        List<String> files
    ) {
        this.id = id;
        this.instrumentId = instrumentId;
        this.done = done;
        this.type = type;
        this.files = files;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(long instrumentId) {
        this.instrumentId = instrumentId;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public UploadType getType() {
        return type;
    }

    public void setType(UploadType type) {
        this.type = type;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }
}
