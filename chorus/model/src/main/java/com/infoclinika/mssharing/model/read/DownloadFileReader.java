package com.infoclinika.mssharing.model.read;

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.Date;

/**
 * @author Elena Kurilina
 */
public interface DownloadFileReader {

    DownloadFileJob readJobByFile(Long id);

    FileItemLocation readFileLocation(Long id);

    ImmutableList<DownloadFileGroup> readGroupByJob(Long id);

    final class DownloadFileJob {
        public long id;
        public boolean completed = false;
        public Long fileMetaData;
        public Long experimentId;

        public DownloadFileJob(Long id, boolean completed, Long fileMetaData) {
            this.id = id;
            this.completed = completed;
            this.fileMetaData = fileMetaData;
        }

        @Override
        public String toString() {
            return "DownloadFileJob{" +
                "id=" + id +
                ", completed=" + completed +
                ", fileMetaData=" + fileMetaData +
                ", experimentId=" + experimentId +
                '}';
        }
    }

    final class FileItemLocation {
        public String contendId;
        public String archiveId;
        public Date lastAccess;

        public FileItemLocation(String contendId, String archiveId, Date lastAccess) {
            this.contendId = contendId;
            this.archiveId = archiveId;
            this.lastAccess = lastAccess;
        }
    }

    final class DownloadFileGroup {
        public Long experimentId;
        public Collection<Long> users;
        public Collection<Long> group;


        public DownloadFileGroup(Long experimentId, Collection<Long> users, Collection<Long> group) {
            this.experimentId = experimentId;
            this.group = group;
            this.users = users;

        }
    }
}
