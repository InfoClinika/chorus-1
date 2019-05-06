/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika,
 * Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use,
 * duplication or disclosure by the
 */
package com.infoclinika.mssharing.platform.web.uploader;

import com.infoclinika.mssharing.platform.fileserver.StoredObjectPathsTemplate;
import com.infoclinika.mssharing.platform.model.read.AttachmentsReaderTemplate;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Oleksii Tymchenko
 */
@Service
public class ProjectAttachmentsUploadHelper extends AbstractStorageHelper {

    @Inject
    private StoredObjectPathsTemplate storedObjectPaths;

    @Inject
    private AttachmentsReaderTemplate attachmentsReader;

    @Override
    protected FileData getData(long item, long userId) {
        final AttachmentsReaderTemplate.AttachmentItem attachmentItem = attachmentsReader.readAttachment(userId, item);

        return new FileData(attachmentItem.name, storedObjectPaths.projectAttachmentPath(userId, item));
    }
}
