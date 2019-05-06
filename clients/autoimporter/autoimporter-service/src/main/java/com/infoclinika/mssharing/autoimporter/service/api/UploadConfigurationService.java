package com.infoclinika.mssharing.autoimporter.service.api;

import java.util.Date;
import java.util.List;

/**
 * @author timofey.kasyanov
 *     12.03.14
 */
public interface UploadConfigurationService {

    List<UploadConfiguration> list();

    UploadConfiguration create(UploadConfiguration configuration);

    void remove(long configurationId);

    UploadConfiguration change(UploadConfiguration configuration);

    public enum CompleteAction {

        NOTHING,
        DELETE_FILE,
        MOVE_FILE

    }

    public class UploadConfiguration {

        private long id;
        private String name;
        private String folder;
        private String labels;
        private long instrument;
        private long specie;
        private Date created;
        private CompleteAction completeAction;
        private String folderToMoveFiles;
        private boolean started;

        public UploadConfiguration(long id, String name, String folder, String labels,
                                   long instrument, long specie, Date created,
                                   CompleteAction completeAction, String folderToMoveFiles, boolean started) {
            this.id = id;
            this.name = name;
            this.folder = folder;
            this.labels = labels;
            this.instrument = instrument;
            this.specie = specie;
            this.created = created;
            this.completeAction = completeAction;
            this.folderToMoveFiles = folderToMoveFiles;
            this.started = started;
        }

        public UploadConfiguration() {}

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

        public long getInstrument() {
            return instrument;
        }

        public void setInstrument(long instrument) {
            this.instrument = instrument;
        }

        public long getSpecie() {
            return specie;
        }

        public void setSpecie(long specie) {
            this.specie = specie;
        }

        public Date getCreated() {
            return created;
        }

        public void setCreated(Date created) {
            this.created = created;
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

        public boolean isStarted() {
            return started;
        }

        public void setStarted(boolean started) {
            this.started = started;
        }
    }

}
