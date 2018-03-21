package com.infoclinika.mssharing.model.internal.entity;

import com.infoclinika.mssharing.platform.entity.Dictionary;
import com.infoclinika.mssharing.platform.entity.Species;
import com.infoclinika.mssharing.search.LowerCaseStringBridge;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Ruslan Duboveckij
 */
@Entity
@Table(name = "upload_app_configuration")
@AttributeOverride(name = "name", column = @Column(unique = false))
public class UploadAppConfiguration extends Dictionary {

    public enum UploadCompleteAction {

        NOTHING,
        DELETE_FILE,
        MOVE_FILE

    }

    @Column(unique = false, nullable = false)
    private String folder;
    @Column(nullable = false)
    private boolean started;
    @Fields({
            @Field(analyze = Analyze.NO),
            @Field(boost = @Boost(3000f), name = "labels.sort", analyze = Analyze.NO, bridge = @FieldBridge(impl = LowerCaseStringBridge.class))
    })
    private String labels;
    @ManyToOne(optional = false)
    private Instrument instrument;
    @ManyToOne(optional = false)
    private User user;
    @ManyToOne(optional = false)
    private Species specie;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date created;
    @Enumerated
    @Column(name = "complete_action", nullable = false)
    private UploadCompleteAction completeAction = UploadCompleteAction.NOTHING;
    @Column(name = "folder_to_move_files")
    private String folderToMoveFiles;

    public UploadAppConfiguration() {
    }

    public UploadAppConfiguration(String name,
                                  String folder,
                                  boolean started,
                                  String labels,
                                  Instrument instrument,
                                  User user,
                                  Species specie,
                                  UploadCompleteAction completeAction,
                                  String folderToMoveFiles) {
        super(name);
        this.folder = folder;
        this.started = started;
        this.labels = labels;
        this.instrument = instrument;
        this.user = user;
        this.specie = specie;
        this.completeAction = completeAction;
        this.folderToMoveFiles = folderToMoveFiles;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Species getSpecie() {
        return specie;
    }

    public void setSpecie(Species specie) {
        this.specie = specie;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public UploadCompleteAction getCompleteAction() {
        return completeAction;
    }

    public void setCompleteAction(UploadCompleteAction completeAction) {
        this.completeAction = completeAction;
    }

    public String getFolderToMoveFiles() {
        return folderToMoveFiles;
    }

    public void setFolderToMoveFiles(String folderToMoveFiles) {
        this.folderToMoveFiles = folderToMoveFiles;
    }

    @PrePersist
    protected void onCreate() {
        created = new Date();
    }
}
