package com.infoclinika.mssharing.model.write;

import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author Ruslan Duboveckij
 */
@Transactional
public interface UploadAppManagement {
    void configurationStarted(long actor, long configurationId);

    void configurationStopped(long actor, long configurationId);

    Configuration createConfiguration(long actor, Configuration configuration);

    void deleteConfiguration(long actor, long configurationId);

    List<Configuration> readConfiguration(long actor);

    public enum CompleteAction {

        NOTHING,
        DELETE_FILE,
        MOVE_FILE

    }

    public class Configuration {
        public final long id;
        public final String name;
        public final String folder;
        public final boolean started;
        public final String labels;
        public final long instrument;
        public final long specie;
        public final Date created;
        public final CompleteAction completeAction;
        public final String folderToMoveFiles;

        public Configuration(long id,
                             String name,
                             String folder,
                             boolean started,
                             String labels,
                             long instrument,
                             long specie,
                             Date created,
                             CompleteAction completeAction,
                             String folderToMoveFiles) {

            this.id = id;
            this.name = name;
            this.folder = folder;
            this.started = started;
            this.labels = labels;
            this.instrument = instrument;
            this.specie = specie;
            this.created = created;
            this.completeAction = completeAction;
            this.folderToMoveFiles = folderToMoveFiles;
        }
    }

}
