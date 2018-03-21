/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.web.controller;

import com.google.common.base.Function;
import com.infoclinika.mssharing.model.read.DetailsReader;
import com.infoclinika.mssharing.platform.fileserver.model.StoredFile;
import com.infoclinika.mssharing.model.helper.StoredObjectPaths;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.write.AttachmentManagement;
import com.infoclinika.mssharing.platform.model.read.AttachmentsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.AttachmentsReaderTemplate.AttachmentItem;
import com.infoclinika.mssharing.platform.web.uploader.AbstractStorageHelper;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import static com.infoclinika.mssharing.web.downloader.AttachmentsDownloadHelper.encodeContentDisposition;

/**
 * @author Oleksii Tymchenko
 */
public abstract class AbstractAttachmentsController extends AbstractFileUploadController {

    private static final Logger LOGGER = Logger.getLogger(AbstractAttachmentsController.class);
    private static final String USER_AGENT = "User-Agent";

    protected final AttachmentManagement attachmentManagement;
    protected final AttachmentsReaderTemplate<AttachmentItem> attachmentsReader;
    protected final DetailsReader detailsReader;
    private final AbstractStorageHelper abstractStorageHelper;
    protected final StoredObjectPaths storedObjectPaths;

    //todo: move AttachmentsReaderTemplate to children
    protected AbstractAttachmentsController(AttachmentsReaderTemplate attachmentsReader,
                                            AttachmentManagement attachmentManagement,
                                            AbstractStorageHelper attachmentsUploadHelper,
                                            StoredObjectPaths storedObjectPaths,
                                            DashboardReader dashboardReader, DetailsReader detailsReader) {
        super(dashboardReader);
        this.attachmentsReader = attachmentsReader;
        this.attachmentManagement = attachmentManagement;
        this.abstractStorageHelper = attachmentsUploadHelper;
        this.storedObjectPaths = storedObjectPaths;
        this.detailsReader = detailsReader;
    }

    protected void postAttachmentToResponse(AttachmentItem attachmentItem, HttpServletRequest request, HttpServletResponse response, Function<Long, AttachmentItem> attachmentItemFunction) throws IOException {

        //todo[tymchenko]: code smell; Refactor File storage API to avoid this
        final StoredFile file = abstractStorageHelper.getContent(attachmentItem.id, attachmentItem.ownerId).get().storedFile;
        final InputStream is = file.getInputStream();

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", encodeContentDisposition(attachmentItem.name, request.getHeader(USER_AGENT)));
        IOUtils.copy(is, response.getOutputStream());

        //Set cookie to satisfy AJAX downloader at the client:
        //http://johnculviner.com/post/2012/03/22/Ajax-like-feature-rich-file-downloads-with-jQuery-File-Download.aspx
        response.setHeader("Set-Cookie", "fileDownload=true; path=/");
        response.flushBuffer();
    }

}
