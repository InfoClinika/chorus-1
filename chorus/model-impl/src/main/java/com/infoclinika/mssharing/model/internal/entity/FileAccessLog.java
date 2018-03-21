package com.infoclinika.mssharing.model.internal.entity;

import javax.persistence.*;
import java.util.Date;

/**
 * @author vladislav.kovchug
 */

@Entity
@Table(name = "file_access_log")
public class FileAccessLog {

    @Id
    @Column(name = "id")
    @GeneratedValue
    private Long id;

    @Column(name = "userId")
    private Long userId;

    @Column(name = "userEmail")
    private String userEmail;

    @Column(name = "userLabId")
    private Long userLabId;

    @Column(name = "userLabName")
    private String userLabName;

    @Column(name = "fileId")
    private Long fileId;

    @Column(name = "fileSize")
    private Long fileSize;

    @Column(name = "fileContentId")
    private String fileContentId;

    @Column(name = "fileArchiveId")
    private String fileArchiveId;

    @Column(name = "fileName")
    private String fileName;

    @Column(name = "operationType")
    @Enumerated(EnumType.STRING)
    private OperationType operationType;

    @Column(name = "operationDate")
    private Date operationDate;


    public FileAccessLog(Long userId, String userEmail, Long userLabId, String userLabName, Long fileId, Long fileSize, String fileContentId, String fileArchiveId, String fileName, OperationType operationType, Date operationDate) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.userLabId = userLabId;
        this.userLabName = userLabName;
        this.fileId = fileId;
        this.fileSize = fileSize;
        this.fileContentId = fileContentId;
        this.fileArchiveId = fileArchiveId;
        this.fileName = fileName;
        this.operationType = operationType;
        this.operationDate = operationDate;
    }

    public FileAccessLog() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Long getUserLabId() {
        return userLabId;
    }

    public void setUserLabId(Long userLabId) {
        this.userLabId = userLabId;
    }

    public String getUserLabName() {
        return userLabName;
    }

    public void setUserLabName(String userLabName) {
        this.userLabName = userLabName;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileContentId() {
        return fileContentId;
    }

    public void setFileContentId(String fileContentId) {
        this.fileContentId = fileContentId;
    }

    public String getFileArchiveId() {
        return fileArchiveId;
    }

    public void setFileArchiveId(String fileArchiveId) {
        this.fileArchiveId = fileArchiveId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public Date getOperationDate() {
        return operationDate;
    }

    public void setOperationDate(Date operationDate) {
        this.operationDate = operationDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public enum OperationType{
        FILE_UPLOAD_STARTED,
        FILE_UPLOAD_CONFIRMED,
        FILE_DELETED,
        FILE_DELETED_PERMANENTLY,
        FILE_ARCHIVE_STARTED,
        FILE_ARCHIVE_CONFIRMED,
        FILE_DOWNLOAD_STARTED
    }

}
