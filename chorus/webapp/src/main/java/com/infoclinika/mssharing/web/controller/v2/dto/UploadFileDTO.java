package com.infoclinika.mssharing.web.controller.v2.dto;

import com.infoclinika.mssharing.model.internal.entity.upload.FileDetails;
import com.infoclinika.mssharing.web.controller.v2.util.FileUtil;

/**
 * @author Vitalii Petkanych
 */
public class UploadFileDTO {
    private long fileId;
    private String fullName;
    private String name;
    private long date;
    private long size;
    private long sizeUploaded;
    private long specieId;
    private String status;
    private String[] labels;

    public UploadFileDTO() {
    }

    public UploadFileDTO(
        long fileId,
        String fullName,
        String name,
        long date,
        long size,
        long sizeUploaded,
        long specieId,
        String status,
        String[] labels
    ) {
        this.fileId = fileId;
        this.fullName = fullName;
        this.name = name;
        this.date = date;
        this.size = size;
        this.sizeUploaded = sizeUploaded;
        this.specieId = specieId;
        this.status = status;
        this.labels = labels;
    }

    public static UploadFileDTO of(FileDetails o) {
        final String name = FileUtil.extractName(o.getFileName());
        final String status = o.getStatus() == null ? "" : o.getStatus().name();
        return new UploadFileDTO(
            o.getFileId(),
            o.getFileName(),
            name,
            o.getLastUpdated(),
            o.getSize(),
            o.getSizeUploaded(),
            o.getSpecieId(),
            status,
            o.getLabels()
        );
    }

    public long getFileId() {
        return fileId;
    }

    public void setFileId(long fileId) {
        this.fileId = fileId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getSizeUploaded() {
        return sizeUploaded;
    }

    public void setSizeUploaded(long sizeUploaded) {
        this.sizeUploaded = sizeUploaded;
    }

    public long getSpecieId() {
        return specieId;
    }

    public void setSpecieId(long specieId) {
        this.specieId = specieId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String[] getLabels() {
        return labels;
    }

    public void setLabels(String[] labels) {
        this.labels = labels;
    }

}
