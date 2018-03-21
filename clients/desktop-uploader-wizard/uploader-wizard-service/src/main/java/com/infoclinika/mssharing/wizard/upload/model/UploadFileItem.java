package com.infoclinika.mssharing.wizard.upload.model;

/**
 * @author timofey.kasyanov
 *         date:   28.01.14
 */
public class UploadFileItem extends ZipFileItem {
    private long specieId;
    private String labels;
    private long fileId;
    private String contentId;
    private boolean needZipping;
    private long uploadedValue;
    private String authorization;
    private String formattedDate;
    private boolean sseEnabled;

    public double getUploadRatio(){
        if(getFileSize() == 0){
            return 0;
        }

        return (double) uploadedValue / (double) getFileSize();
    }

    public long getSpecieId() {
        return specieId;
    }

    public void setSpecieId(long specieId) {
        this.specieId = specieId;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
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

    public boolean isNeedZipping() {
        return needZipping;
    }

    public void setNeedZipping(boolean needZipping) {
        this.needZipping = needZipping;
    }

    public long getUploadedValue() {
        return uploadedValue;
    }

    public void setUploadedValue(long uploadedValue) {
        this.uploadedValue = uploadedValue;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UploadFileItem)) return false;

        UploadFileItem that = (UploadFileItem) o;

        if (!getName().equals(that.getName())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

}
