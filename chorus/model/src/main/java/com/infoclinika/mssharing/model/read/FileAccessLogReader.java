package com.infoclinika.mssharing.model.read;

import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;

import java.util.Date;

/**
 * @author vladislav.kovchug
 */
public interface FileAccessLogReader {

    PagedItem<FileAccessLogDTO> readLogs(long actor, PagedItemInfo pagedItem);

    class FileAccessLogDTO {
        public final Long id;
        public final String userEmail;
        public final String userLabName;
        public final Long fileSize;
        public final String fileContentId;
        public final String fileArchiveId;
        public final String fileName;
        public final String operationType;
        public final Date operationDate;

        public FileAccessLogDTO(Long id, String userEmail, String userLabName, Long fileSize,
                                String fileContentId, String fileArchiveId, String fileName,
                                String operationType, Date operationDate) {
            this.id = id;
            this.userEmail = userEmail;
            this.userLabName = userLabName;
            this.fileSize = fileSize;
            this.fileContentId = fileContentId;
            this.fileArchiveId = fileArchiveId;
            this.fileName = fileName;
            this.operationType = operationType;
            this.operationDate = operationDate;
        }
    }

}
