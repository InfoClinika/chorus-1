package com.infoclinika.mssharing.autoimporter.model.bean;

import com.google.common.base.MoreObjects;
import com.infoclinika.mssharing.autoimporter.model.util.AbstractUploadFile;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * author Ruslan Duboveckij
 */
public class UploadItem extends AbstractUploadFile implements Serializable {

    private long fileId;
    private String contentId;
    private long zippedValue;
    private long uploadedValue;
    private boolean isArchive = false;
    private long zipSize;
    private boolean canceled = false;
    private ItemStatus status = ItemStatus.WAITING;
    private List<File> filesToZip = newArrayList();
    private boolean repeat = false;
    private String authorization;
    private String formattedDate;
    private boolean sseEnabled;
    private int numberOfTries = 0;
    private Date lastErrorDate;

    public UploadItem(File file) {
        super(file);
    }

    public double getUploadRatio() {

        if (getSize() == 0) {
            return 0;
        }

        return (double) uploadedValue / (double) getSize();

    }

    public double getZipRation() {

        if (zipSize == 0) {
            return 0;
        }

        return (double) zippedValue / (double) zipSize;

    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public long getZipSize() {
        return zipSize;
    }

    public void setZipSize(long zipSize) {
        this.zipSize = zipSize;
    }

    public List<File> getFilesToZip() {
        return filesToZip;
    }

    public void setFilesToZip(List<File> filesToZip) {
        this.filesToZip = filesToZip;
    }

    public long getZippedValue() {
        return zippedValue;
    }

    public void setZippedValue(long zippedValue) {
        this.zippedValue = zippedValue;
    }

    public long getUploadedValue() {
        return uploadedValue;
    }

    public void setUploadedValue(long uploadedValue) {
        this.uploadedValue = uploadedValue;
    }

    public boolean isArchive() {
        return isArchive;
    }

    public void setArchive(boolean isArchive) {
        this.isArchive = isArchive;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    public long getFileId() {
        return fileId;
    }

    public void setFileId(long fileId) {
        this.fileId = fileId;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public String getFormattedDate() {
        return formattedDate;
    }

    public void setFormattedDate(String formattedDate) {
        this.formattedDate = formattedDate;
    }

    public boolean isSseEnabled() {
        return sseEnabled;
    }

    public void setSseEnabled(boolean sseEnabled) {
        this.sseEnabled = sseEnabled;
    }

    public int getNumberOfTries() {
        return numberOfTries;
    }

    public void incrementNumberOfTries() {
        this.numberOfTries++;
    }

    public Date getLastErrorDate() {
        return lastErrorDate;
    }

    public void setLastErrorDate(Date lastErrorDate) {
        this.lastErrorDate = lastErrorDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AbstractUploadFile that = (AbstractUploadFile) o;

        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return getName() != null ? getName().hashCode() : 0;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("file", getFile())
            .add("fileId", fileId)
            .add("contentId", contentId)
            .add("zippedValue", zippedValue)
            .add("uploadedValue", uploadedValue)
            .add("status", status)
            .add("file size", getSize())
            .toString();
    }
}
