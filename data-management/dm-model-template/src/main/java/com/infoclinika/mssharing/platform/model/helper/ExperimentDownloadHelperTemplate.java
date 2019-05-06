package com.infoclinika.mssharing.platform.model.helper;

import java.util.*;

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
        public static final String EXPERIMENT_NAME = "Experiment";

        private final LinkedHashMap<String, String> linkedHashMap;

        public final List<AttachmentDataTemplate> attachments;
        public final List<FileDataTemplate> files;

        public ExperimentDownloadDataTemplate(List<AttachmentDataTemplate> attachments,
                                              List<FileDataTemplate> files) {
            linkedHashMap = new LinkedHashMap<>();

            this.attachments = attachments;
            this.files = files;
        }

        public Collection<String> getFieldNames() {
            return linkedHashMap.keySet();
        }

        public Collection<Map.Entry<String, String>> getEntries() {
            return linkedHashMap.entrySet();
        }

        public String getFieldValue(String name) {
            return linkedHashMap.get(name);
        }

        public void add(String name, String value) {
            linkedHashMap.put(name, value);
        }

        public String getExperimentName() {
            return linkedHashMap.get(EXPERIMENT_NAME);
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
        public final String bucket;
        public final String contentId;
        public final String name;
        public final boolean invalid;
        public final List<ConditionDataTemplate> conditions;
        public final long lab;
        public final String instrumentName;

        public FileDataTemplate(long id, String bucket, String contentId, String name, boolean invalid,
                                List<ConditionDataTemplate> conditions, long lab, String instrumentName) {
            this.id = id;
            this.bucket = bucket;
            this.contentId = contentId;
            this.name = name;
            this.invalid = invalid;
            this.conditions = conditions;
            this.lab = lab;
            this.instrumentName = instrumentName;
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
