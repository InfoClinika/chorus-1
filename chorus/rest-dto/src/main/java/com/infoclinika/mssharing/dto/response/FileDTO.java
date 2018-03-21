package com.infoclinika.mssharing.dto.response;

import java.util.Date;

/**
 * author: Ruslan Duboveckij
 */
public class FileDTO {
    private long id;
    private String name;
    private long instrumentId;
    private Long specieId;
    private String contentId;
    private String uploadId;
    private String destinationPath;
    private AccessLevel accessLevel;
    private boolean usedInExperiments;
    private long owner;
    private Date lastPingDate;
    private boolean isArchive;
    private boolean invalid;
    private FileColumnsDTO columns;

    public FileDTO(){}

    public FileDTO(long id,
                   String name,
                   long instrumentId,
                   Long specieId,
                   String contentId,
                   String uploadId,
                   String destinationPath,
                   boolean archive,
                   AccessLevel accessLevel,
                   boolean usedInExperiments,
                   long owner,
                   Date lastPingDate,
                   FileColumnsDTO columns,
                   boolean invalid) {
        this.id = id;
        this.name = name;
        this.instrumentId = instrumentId;
        this.specieId = specieId;
        this.contentId = contentId;
        this.uploadId = uploadId;
        this.destinationPath = destinationPath;
        this.accessLevel = accessLevel;
        this.usedInExperiments = usedInExperiments;
        this.owner = owner;
        this.lastPingDate = lastPingDate;
        isArchive = archive;
        this.columns = columns;
        this.invalid = invalid;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(long instrumentId) {
        this.instrumentId = instrumentId;
    }

    public Long getSpecieId() {
        return specieId;
    }

    public void setSpecieId(Long specieId) {
        this.specieId = specieId;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public String getDestinationPath() {
        return destinationPath;
    }

    public void setDestinationPath(String destinationPath) {
        this.destinationPath = destinationPath;
    }

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    public boolean isUsedInExperiments() {
        return usedInExperiments;
    }

    public void setUsedInExperiments(boolean usedInExperiments) {
        this.usedInExperiments = usedInExperiments;
    }

    public long getOwner() {
        return owner;
    }

    public void setOwner(long owner) {
        this.owner = owner;
    }

    public Date getLastPingDate() {
        return lastPingDate;
    }

    public void setLastPingDate(Date lastPingDate) {
        this.lastPingDate = lastPingDate;
    }

    public boolean isArchive() {
        return isArchive;
    }

    public void setArchive(boolean isArchive) {
        this.isArchive = isArchive;
    }

    public boolean isInvalid() {
        return invalid;
    }

    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }

    public FileColumnsDTO getColumns() {
        return columns;
    }

    public void setColumns(FileColumnsDTO columns) {
        this.columns = columns;
    }

    @Override
    public String toString() {
        return "FileDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", instrumentId=" + instrumentId +
                ", specieId=" + specieId +
                ", contentId='" + contentId + '\'' +
                ", uploadId='" + uploadId + '\'' +
                ", destinationPath='" + destinationPath + '\'' +
                ", accessLevel=" + accessLevel +
                ", usedInExperiments=" + usedInExperiments +
                ", owner=" + owner +
                ", lastPingDate=" + lastPingDate +
                ", isArchive=" + isArchive +
                ", invalid=" + invalid +
                ", columns=" + columns +
                '}';
    }

    public static enum AccessLevel {
        PRIVATE, SHARED, PUBLIC
    }
}
