package com.infoclinika.mssharing.clients.common.transfer.impl;

import java.io.File;

/**
 * @author timofey.kasyanov
 *     date: 24.02.14.
 */
public class UploadItem {
    private File file;
    private String key;
    private String authorization;
    private String date;
    private boolean serverSideEncryption;
    private volatile UploadItemState state = UploadItemState.WAITING;

    public UploadItem(File file, String key) {
        this.file = file;
        this.key = key;
    }

    public UploadItem(File file, String key, String authorization, String date, boolean serverSideEncryption) {
        this.file = file;
        this.key = key;
        this.authorization = authorization;
        this.date = date;
        this.serverSideEncryption = serverSideEncryption;
    }

    public UploadItemState getState() {
        return state;
    }

    public void setState(UploadItemState state) {
        this.state = state;
    }

    public File getFile() {
        return file;
    }

    public String getKey() {
        return key;
    }

    public String getAuthorization() {
        return authorization;
    }

    public String getDate() {
        return date;
    }

    public boolean isServerSideEncryption() {
        return serverSideEncryption;
    }
}
