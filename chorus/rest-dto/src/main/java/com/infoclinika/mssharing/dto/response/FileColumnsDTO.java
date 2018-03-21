package com.infoclinika.mssharing.dto.response;

import java.util.Date;

/**
 * author: Ruslan Duboveckij
 */
public class FileColumnsDTO {
    private String name;
    private long sizeInBytes;
    private String instrument;
    private String laboratory;
    private Date uploadDate;
    private String labels;
    //From Annotations
    private Date creationDate;
    private String comment;
    private String instrumentMethod;
    private String startTime;
    private String endTime;
    private String startMz;
    private String endMz;
    private String fileName;
    private String seqRowPosition;
    private String sampleName;
    private String annotationInstrument;
    private String userName;
    private String userLabels;
    private String fileCondition;
    private String instrumentSerialNumber;
    private String phone;
    private String instrumentName;

    public FileColumnsDTO(){}

    public FileColumnsDTO(String name,
                          long sizeInBytes,
                          String instrument,
                          String laboratory,
                          Date uploadDate,
                          String labels) {
        this.name = name;
        this.sizeInBytes = sizeInBytes;
        this.instrument = instrument;
        this.laboratory = laboratory;
        this.uploadDate = uploadDate;
        this.labels = labels;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSizeInBytes() {
        return sizeInBytes;
    }

    public void setSizeInBytes(long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public String getLaboratory() {
        return laboratory;
    }

    public void setLaboratory(String laboratory) {
        this.laboratory = laboratory;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getInstrumentMethod() {
        return instrumentMethod;
    }

    public void setInstrumentMethod(String instrumentMethod) {
        this.instrumentMethod = instrumentMethod;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStartMz() {
        return startMz;
    }

    public void setStartMz(String startMz) {
        this.startMz = startMz;
    }

    public String getEndMz() {
        return endMz;
    }

    public void setEndMz(String endMz) {
        this.endMz = endMz;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSeqRowPosition() {
        return seqRowPosition;
    }

    public void setSeqRowPosition(String seqRowPosition) {
        this.seqRowPosition = seqRowPosition;
    }

    public String getSampleName() {
        return sampleName;
    }

    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }

    public String getAnnotationInstrument() {
        return annotationInstrument;
    }

    public void setAnnotationInstrument(String annotationInstrument) {
        this.annotationInstrument = annotationInstrument;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserLabels() {
        return userLabels;
    }

    public void setUserLabels(String userLabels) {
        this.userLabels = userLabels;
    }

    public String getFileCondition() {
        return fileCondition;
    }

    public void setFileCondition(String fileCondition) {
        this.fileCondition = fileCondition;
    }

    public String getInstrumentSerialNumber() {
        return instrumentSerialNumber;
    }

    public void setInstrumentSerialNumber(String instrumentSerialNumber) {
        this.instrumentSerialNumber = instrumentSerialNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getInstrumentName() {
        return instrumentName;
    }

    public void setInstrumentName(String instrumentName) {
        this.instrumentName = instrumentName;
    }

    @Override
    public String toString() {
        return "FileColumnsDTO{" +
                "creationDate=" + creationDate +
                ", comment='" + comment + '\'' +
                ", instrumentMethod='" + instrumentMethod + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", startMz='" + startMz + '\'' +
                ", endMz='" + endMz + '\'' +
                ", fileName='" + fileName + '\'' +
                ", seqRowPosition='" + seqRowPosition + '\'' +
                ", sampleName='" + sampleName + '\'' +
                ", annotationInstrument='" + annotationInstrument + '\'' +
                ", userName='" + userName + '\'' +
                ", userLabels='" + userLabels + '\'' +
                ", fileCondition='" + fileCondition + '\'' +
                ", instrumentSerialNumber='" + instrumentSerialNumber + '\'' +
                ", phone='" + phone + '\'' +
                ", instrumentName='" + instrumentName + '\'' +
                '}';
    }
}
