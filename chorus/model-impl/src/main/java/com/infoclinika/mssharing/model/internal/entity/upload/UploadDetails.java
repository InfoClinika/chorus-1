package com.infoclinika.mssharing.model.internal.entity.upload;

import com.infoclinika.mssharing.platform.entity.AbstractPersistable;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Vitalii Petkanych
 */
@Entity
@Table(name = "UPL_UPLOADS")
public class UploadDetails extends AbstractPersistable {
    @Enumerated(EnumType.STRING)
    private UploadType type;

    private String url;
    private String login;

    @Convert(converter = EncryptedPasswordJpaConverter.class)
    private String password;

    @Convert(converter = StringArrayJpaConverter.class)
    private String[] masks;

    private boolean recursive;
    private boolean makeReferences;

    @Enumerated(EnumType.STRING)
    private UploadStatus status = UploadStatus.CREATED;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "UPL_UPLOADS_TO_FILES")
    private List<FileDetails> files;

    private long instrumentId;
    private long userId;

    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime uploadStarted;

    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime uploadFinished;

    public UploadDetails() {
    }

    public UploadDetails(
        UploadType type,
        String url,
        String login,
        String password,
        String[] masks,
        boolean recursive,
        boolean makeReferences,
        UploadStatus status,
        List<FileDetails> files,
        long instrumentId,
        long userId,
        LocalDateTime uploadStarted,
        LocalDateTime uploadFinished
    ) {
        this.type = type;
        this.url = url;
        this.login = login;
        this.password = password;
        this.masks = masks;
        this.recursive = recursive;
        this.makeReferences = makeReferences;
        this.status = status;
        this.files = files;
        this.instrumentId = instrumentId;
        this.userId = userId;
        this.uploadStarted = uploadStarted;
        this.uploadFinished = uploadFinished;
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

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public boolean isMakeReferences() {
        return makeReferences;
    }

    public void setMakeReferences(boolean makeReferences) {
        this.makeReferences = makeReferences;
    }

    public UploadStatus getStatus() {
        return status;
    }

    public void setStatus(UploadStatus status) {
        this.status = status;
    }

    public List<FileDetails> getFiles() {
        return files;
    }

    public void setFiles(List<FileDetails> files) {
        this.files = files;
    }

    public long getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(long instrumentId) {
        this.instrumentId = instrumentId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
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
}
