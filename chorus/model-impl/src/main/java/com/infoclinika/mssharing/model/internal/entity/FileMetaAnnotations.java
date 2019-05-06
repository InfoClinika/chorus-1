package com.infoclinika.mssharing.model.internal.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * @author Herman Zamula
 */
@Entity
public class FileMetaAnnotations {

    @Id
    private long id;
    private Date creationDate;
    private String comment;
    private String instrumentMethod;
    private String fileName;
    private String seqRowPosition;
    private String sampleName;
    private String instrument;
    private String userName;

    @Column(length = 512)
    private String userLabels;

    private String fileCondition;

    private String instrumentSerialNumber;
    private String phone;
    private String instrumentName;

    public FileMetaAnnotations() {
    }

    public FileMetaAnnotations(long id) {
        this.id = id;
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

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public String getUserLabels() {
        return userLabels;
    }

    public void setUserLabels(String userLabels) {
        this.userLabels = userLabels;
    }

    @Transient
    public Map<String, Object> getColumnsMap() {
        final HashMap<String, Object> map = newHashMap();
        map.put("creationDate", creationDate);
        map.put("comment", comment);
        map.put("instrumentMethod", instrumentMethod);
        map.put("fileName", fileName);
        map.put("seqRowPosition", seqRowPosition);
        map.put("sampleName", sampleName);
        map.put("instrument", instrument);
        map.put("userName", userName);
        map.put("userLabels", userLabels);
        map.put("fileCondition", fileCondition);
        map.put("instrumentSerialNumber", instrumentSerialNumber);
        map.put("phone", phone);
        map.put("instrumentName", instrumentName);

        return map;
    }

    public long getId() {
        return id;
    }
}
