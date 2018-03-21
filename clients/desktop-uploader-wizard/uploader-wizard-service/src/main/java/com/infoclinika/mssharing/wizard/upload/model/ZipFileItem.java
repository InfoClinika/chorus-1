package com.infoclinika.mssharing.wizard.upload.model;

import java.io.File;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author timofey.kasyanov
 *         date:   29.01.14
 */
public class ZipFileItem {
    private String name;
    private File resultFile;
    private List<File> filesToZip = newArrayList();
    private long zipSize;
    private long zippedValue = 0;
    private String error = "";
    private UploadStatus status = UploadStatus.WAITING;
    private long fileSize;
    private boolean canceled = false;

    public ZipFileItem() {
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public double getZipRatio(){
        return (double) zippedValue / (double) zipSize;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getResultFile() {
        return resultFile;
    }

    public void setResultFile(File resultFile) {
        this.resultFile = resultFile;
    }

    public List<File> getFilesToZip() {
        return filesToZip;
    }

    public void setFilesToZip(List<File> filesToZip) {
        this.filesToZip = filesToZip;
    }

    public long getZipSize() {
        return zipSize;
    }

    public void setZipSize(long zipSize) {
        this.zipSize = zipSize;
    }

    public long getZippedValue() {
        return zippedValue;
    }

    public void setZippedValue(long zippedValue) {
        this.zippedValue = zippedValue;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public UploadStatus getStatus() {
        return status;
    }

    public void setStatus(UploadStatus status) {
        this.status = status;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ZipFileItem)) {
            return false;
        }

        ZipFileItem that = (ZipFileItem) o;

        if (!name.equals(that.name)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
