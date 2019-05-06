package com.infoclinika.mssharing.autoimporter.model.bean;

import com.google.common.base.MoreObjects;
import com.infoclinika.mssharing.dto.response.DictionaryDTO;
import com.infoclinika.mssharing.dto.response.InstrumentDTO;

import java.io.Serializable;
import java.util.Date;

import static com.infoclinika.mssharing.autoimporter.service.api.UploadConfigurationService.CompleteAction;
import static com.infoclinika.mssharing.autoimporter.service.api.UploadConfigurationService.CompleteAction.NOTHING;
import static com.infoclinika.mssharing.autoimporter.service.api.UploadConfigurationService.UploadConfiguration;

/**
 * author Ruslan Duboveckij
 */
public class ContextInfo implements Serializable {
    private long id;
    private String name;
    private String folder;
    private String labels;
    private InstrumentDTO instrument;
    private DictionaryDTO specie;
    private Date created;
    private boolean started;
    private CompleteAction completeAction = NOTHING;
    private String folderToMoveFiles;

    public ContextInfo() {
    }

    public ContextInfo(long id,
                       String name,
                       String folder,
                       boolean started,
                       String labels,
                       InstrumentDTO instrument,
                       DictionaryDTO specie,
                       Date created,
                       CompleteAction completeAction,
                       String folderToMoveFiles) {
        this.id = id;
        this.name = name;
        this.folder = folder;
        this.labels = labels;
        this.instrument = instrument;
        this.specie = specie;
        this.created = created;
        this.started = started;
        this.completeAction = completeAction;
        this.folderToMoveFiles = folderToMoveFiles;
    }

    public UploadConfiguration toDto() {
        return new UploadConfiguration(
            id,
            name,
            folder,
            labels,
            instrument.getId(),
            specie.getId(),
            created,
            completeAction,
            folderToMoveFiles,
            started
        );
    }

    public void onStarted() {
        started = true;
    }

    public void onStopped() {
        started = false;
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

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public InstrumentDTO getInstrument() {
        return instrument;
    }

    public void setInstrument(InstrumentDTO instrument) {
        this.instrument = instrument;
    }

    public DictionaryDTO getSpecie() {
        return specie;
    }

    public void setSpecie(DictionaryDTO specie) {
        this.specie = specie;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public CompleteAction getCompleteAction() {
        return completeAction;
    }

    public void setCompleteAction(CompleteAction completeAction) {
        this.completeAction = completeAction;
    }

    public String getFolderToMoveFiles() {
        return folderToMoveFiles;
    }

    public void setFolderToMoveFiles(String folderToMoveFiles) {
        this.folderToMoveFiles = folderToMoveFiles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ContextInfo that = (ContextInfo) o;

        if (id != that.id) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("instrument", instrument)
            .add("specie", specie)
            .toString();
    }
}
