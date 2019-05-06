package com.infoclinika.mssharing.web.controller.v2.dto;

import com.infoclinika.mssharing.model.internal.entity.upload.UploadType;

/**
 * @author Vitalii Petkanych
 */
public class UploadCreateDTO {
    private UploadType type;
    private String url;
    private String user;
    private String pass;
    private String[] masks;
    private boolean recursive;
    private long instrumentId;

    public UploadCreateDTO() {
    }

    public UploadCreateDTO(
        UploadType type,
        String url,
        String user,
        String pass,
        String[] masks,
        boolean recursive,
        long instrumentId
    ) {
        this.type = type;
        this.url = url;
        this.user = user;
        this.pass = pass;
        this.masks = masks;
        this.recursive = recursive;
        this.instrumentId = instrumentId;
    }

    public UploadType getType() {
        return type;
    }

    public void setType(UploadType type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String[] getMasks() {
        return masks;
    }

    public void setMasks(String[] masks) {
        this.masks = masks;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public long getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(long instrumentId) {
        this.instrumentId = instrumentId;
    }
}
