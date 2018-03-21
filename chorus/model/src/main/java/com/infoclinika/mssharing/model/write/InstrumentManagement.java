/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.write;

import com.infoclinika.mssharing.platform.fileserver.StoredObject;
import com.infoclinika.mssharing.platform.model.write.FileManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.FileUploadManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.InstrumentManagementTemplate;

import java.util.List;
import java.util.Set;

/**
 * Users use Instruments to perform studying.
 * They can share instrument with other persons in their lab.
 *
 * @author Stanislav Kurilin
 */
public interface InstrumentManagement extends
        InstrumentManagementTemplate<InstrumentDetails>,
        FileUploadManagementTemplate,
        FileManagementTemplate<FileMetaDataInfo> {

    /**
     * @see #addOperatorDirectly(long, long, long)
     *
     */
    @Deprecated
    void addOperatorDirectly(long initiator, long instrument, String newOperatorEmail);

    void pingUpload(long actor, long file);

    void setContent(long actor, long file, StoredObject content);

    //mainly for demo data reasons to avoid submitting 300MB sample files and reuse existing
    @Deprecated
    void setContentID(long actor, long file, String contentID);

    void setLabels(long actor, long file, String newLabels);

    void bulkSetLabels(long actor, Set<Long> file, String newLabels, boolean appendExistingLabels);

    void bulkSetSpecies(long userId, Set<Long> fileIds, long newValue);

    void discard(long actor, long file);

    Long findUploadResumableFile(long user, long instrument, String fileName);

    long startUploadFile(long actor, long instrument, FileMetaDataInfo fileMetaDataInfo);

    void deleteFile(long file);

    long moveFileToTrash(long user, long file);

    long restoreFile(long user, long file);

    void moveFilesToTrash(long user, List<Long> files);

    void removeFilesPermanently(long userId, Set<Long> files);

    void checkCanUploadMore(long instrument, long bytes);

    long createDefaultInstrument(long actor, long labId, long modelId);

    class UploadFileItem {
        public final String name;
        public final String labels;
        public final long size;
        public final long specie;
        public final boolean archive;

        public UploadFileItem(String name, String labels, long size, long specie, boolean archive) {
            this.name = name;
            this.labels = labels;
            this.size = size;
            this.specie = specie;
            this.archive = archive;
        }
    }

}
