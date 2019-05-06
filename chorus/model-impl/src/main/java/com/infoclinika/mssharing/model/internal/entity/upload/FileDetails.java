package com.infoclinika.mssharing.model.internal.entity.upload;

import com.infoclinika.mssharing.platform.entity.AbstractPersistable;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @author Vitalii Petkanych
 */
@Entity
@Table(name = "UPL_FILES")
public class FileDetails extends AbstractPersistable {
    private long fileId;
    private String fileName;
    private long lastUpdated;
    private long size;
    private long sizeUploaded;

    @Convert(converter = StringArrayJpaConverter.class)
    private String[] labels;

    private long specieId;

    @Enumerated(EnumType.STRING)
    private UploadFileStatus status;

    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime uploadStarted;

    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime uploadFinished;

    private String uploadRecoveryId;

    public FileDetails() {
    }

    public FileDetails(String fileName, long lastUpdated, long size) {
        this.fileName = fileName;
        this.lastUpdated = lastUpdated;
        this.size = size;
    }

    public FileDetails(
        long fileId,
        String fileName,
        long lastUpdated,
        long size,
        long sizeUploaded,
        String[] labels,
        long specieId,
        UploadFileStatus status,
        LocalDateTime uploadStarted,
        LocalDateTime uploadFinished,
        String uploadRecoveryId
    ) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.lastUpdated = lastUpdated;
        this.size = size;
        this.sizeUploaded = sizeUploaded;
        this.labels = labels;
        this.specieId = specieId;
        this.status = status;
        this.uploadStarted = uploadStarted;
        this.uploadFinished = uploadFinished;
        this.uploadRecoveryId = uploadRecoveryId;
    }

    public long getFileId() {
        return fileId;
    }

    public void setFileId(long fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
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

    public String[] getLabels() {
        return labels;
    }

    public void setLabels(String[] labels) {
        this.labels = labels;
    }

    public long getSpecieId() {
        return specieId;
    }

    public void setSpecieId(long specieId) {
        this.specieId = specieId;
    }

    public UploadFileStatus getStatus() {
        return status;
    }

    public void setStatus(UploadFileStatus status) {
        this.status = status;
    }

    public LocalDateTime getUploadStarted() {
        return uploadStarted;
    }

    public void setUploadStarted(LocalDateTime uploadStarted) {
        this.uploadStarted = uploadStarted;
    }

    public LocalDateTime getUploadFinished() {
        return uploadFinished;
    }

    public void setUploadFinished(LocalDateTime uploadFinished) {
        this.uploadFinished = uploadFinished;
    }

    public String getUploadRecoveryId() {
        return uploadRecoveryId;
    }

    public void setUploadRecoveryId(String uploadRecoveryId) {
        this.uploadRecoveryId = uploadRecoveryId;
    }
}
