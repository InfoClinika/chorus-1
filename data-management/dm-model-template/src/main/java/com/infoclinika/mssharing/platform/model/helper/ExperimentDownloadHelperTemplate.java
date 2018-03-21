package com.infoclinika.mssharing.platform.model.helper;

import java.util.List;
import java.util.Set;

/**
 * @author Herman Zamula
 */
public interface ExperimentDownloadHelperTemplate<
        EXPERIMENT_ITEM extends ExperimentDownloadHelperTemplate.ExperimentItemTemplate,
        EXPERIMENT_DOWNLOAD_DATA extends ExperimentDownloadHelperTemplate.ExperimentDownloadDataTemplate,
        FILE_DATA extends ExperimentDownloadHelperTemplate.FileDataTemplate> {

    boolean isDownloadTokenAvailable(String token);

    EXPERIMENT_ITEM getExperimentByDownloadToken(String token);

    void sendDownloadExperimentLinkEmail(long actor, long experiment, String email);

    EXPERIMENT_DOWNLOAD_DATA readExperimentDownloadData(long userId, long experimentId);

    List<FILE_DATA> readFilesDownloadData(long userId, Set<Long> fileIds);

    class ExperimentItemTemplate {
        public final long creator;
        public final long experiment;
        public final Set<Long> files;

        public ExperimentItemTemplate(long creator, long experiment, Set<Long> files) {
            this.creator = creator;
            this.experiment = experiment;
            this.files = files;
        }
    }

    class ExperimentDownloadDataTemplate {
        public final long creatorId;
        public final String name;
        public final String description;
        public final String projectName;
        public final String specie;
        public final String experimentType;
        public final boolean allow2dLc;
        public final String instrumentName;
        public final List<AttachmentDataTemplate> attachments;
        public final List<FileDataTemplate> files;

        public ExperimentDownloadDataTemplate(long creatorId,
                                              String name,
                                              String description,
                                              String projectName,
                                              String specie,
                                              String experimentType,
                                              boolean allow2dLc,
                                              String instrumentName,
                                              List<AttachmentDataTemplate> attachments,
                                              List<FileDataTemplate> files) {
            this.creatorId = creatorId;
            this.name = name;
            this.description = description;
            this.projectName = projectName;
            this.specie = specie;
            this.experimentType = experimentType;
            this.allow2dLc = allow2dLc;
            this.instrumentName = instrumentName;
            this.attachments = attachments;
            this.files = files;
        }
    }

    class AttachmentDataTemplate {
        public final long id;
        public final String name;
        public final String contentId;

        public AttachmentDataTemplate(long id, String name, String contentId) {
            this.id = id;
            this.name = name;
            this.contentId = contentId;
        }
    }

    class FileDataTemplate {
        public final long id;
        public final String contentId;
        public final String name;
        public final boolean invalid;
        public final List<ConditionDataTemplate> conditions;
        public final long lab;

        public FileDataTemplate(long id, String contentId, String name, boolean invalid, List<ConditionDataTemplate> conditions, long lab) {
            this.id = id;
            this.contentId = contentId;
            this.name = name;
            this.invalid = invalid;
            this.conditions = conditions;
            this.lab = lab;
        }
    }

    class ConditionDataTemplate {
        public final long id;
        public final String name;
        public final String experimentName;

        public ConditionDataTemplate(long id, String name, String experimentName) {
            this.id = id;
            this.name = name;
            this.experimentName = experimentName;
        }
    }


}
