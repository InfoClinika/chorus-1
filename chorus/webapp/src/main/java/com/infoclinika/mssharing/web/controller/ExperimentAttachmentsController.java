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
package com.infoclinika.mssharing.web.controller;

import com.google.common.base.Function;
import com.infoclinika.mssharing.model.helper.StoredObjectPaths;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.read.DetailsReader;
import com.infoclinika.mssharing.model.write.AttachmentManagement;
import com.infoclinika.mssharing.platform.fileserver.model.NodePath;
import com.infoclinika.mssharing.platform.model.read.AttachmentsReaderTemplate;
import com.infoclinika.mssharing.platform.web.uploader.ExperimentAttachmentsUploadHelper;
import com.infoclinika.mssharing.web.controller.request.CompleteExperimentAttachmentRequest;
import com.infoclinika.mssharing.web.controller.request.StartAttachmentUploadRequest;
import com.infoclinika.mssharing.web.controller.response.StartAttachmentUploadResponse;
import com.infoclinika.mssharing.web.controller.response.UploadFilePathResponse;
import com.infoclinika.mssharing.web.controller.response.ValueResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.List;

import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;

/**
 * @author Oleksii Tymchenko
 */
@Controller
@RequestMapping("/attachments/experiment")
public class ExperimentAttachmentsController extends AbstractAttachmentsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExperimentAttachmentsController.class);

    @Inject
    public ExperimentAttachmentsController(
        ExperimentAttachmentsUploadHelper asyncUploadHelper,
        AttachmentManagement attachmentManagement,
        DetailsReader detailsReader1,
        AttachmentsReaderTemplate detailsReader,
        StoredObjectPaths storedObjectPaths,
        DashboardReader dashboardReader
    ) {
        super(
            detailsReader,
            attachmentManagement,
            asyncUploadHelper,
            storedObjectPaths,
            dashboardReader,
            detailsReader1
        );
    }

    /**
     * Complete the attachment of the binary file to the appropriate experiment.
     * Should be called once the experiment creation or editing is confirmed by the client.
     *
     * @param request   the request containing the experiment and attachment IDs to wire up.
     * @param principal the user performing the request
     */
    @RequestMapping(value = "/attach", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void attachToExperiment(
        @RequestBody final CompleteExperimentAttachmentRequest request,
        final Principal principal
    ) {
        LOGGER.debug("Attaching the binary files to the experiment: {}", request);
        final long userId = getUserId(principal);
        attachmentManagement.updateExperimentAttachments(userId, request.experimentId, request.attachmentIds);
    }


    /**
     * Save the metadata of the attachment and return the attachment ID.
     *
     * @param request   the request containing the attachment metadata
     * @param principal the user performing the request
     * @return the attachment ID along with the filename so that client is able to match the filename with the
     *     attachment ID
     */
    @RequestMapping(value = "/items", method = RequestMethod.POST)
    @ResponseBody
    public StartAttachmentUploadResponse save(
        @RequestBody final StartAttachmentUploadRequest request,
        final Principal principal
    ) {
        LOGGER.debug("Saving the metadata for the attachment: {}", request);
        final long userId = getUserId(principal);
        final long attachmentId = attachmentManagement.newAttachment(userId, request.filename, request.sizeInBytes);
        return new StartAttachmentUploadResponse(request.filename, attachmentId);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public List<AttachmentsReaderTemplate.AttachmentItem> attachmentsForExperiment(
        @PathVariable final Long id,
        Principal principal
    ) {
        return attachmentsReader.readAttachments(
            AttachmentsReaderTemplate.AttachmentType.EXPERIMENT,
            getUserId(principal),
            id
        );
    }

    @RequestMapping(value = "/download/{id}", method = RequestMethod.GET)
    @ResponseBody
    public void attachmentDownload(
        @PathVariable("id") final long attachmentId,
        HttpServletRequest request,
        HttpServletResponse response,
        Principal principal
    ) {
        try {
            final long userId = getUserId(principal);
            LOGGER.debug(
                "Got the download request for the experiment attachment with ID = {} from user with ID {}",
                attachmentId,
                userId
            );
            final AttachmentsReaderTemplate.AttachmentItem attachment =
                attachmentsReader.readAttachment(userId, attachmentId);
            postAttachmentToResponse(
                attachment,
                request,
                response,
                new Function<Long, AttachmentsReaderTemplate.AttachmentItem>() {
                    @Override
                    public AttachmentsReaderTemplate.AttachmentItem apply(Long input) {
                        return attachmentsReader.readAttachment(userId, attachmentId);
                    }
                }
            );
        } catch (IOException e) {
            LOGGER.error("Error writing file to output stream.", e);
            throw new RuntimeException("IOError writing file to output stream");
        }
    }

    @RequestMapping(value = "/interrupt/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void removeUnfinishedAttachment(Principal principal, @PathVariable long id) {
        long userId = getUserId(principal);
        attachmentManagement.discardAttachment(userId, id);
    }

    @RequestMapping(value = "/destination/{id}", method = RequestMethod.GET)
    @ResponseBody
    public UploadFilePathResponse composeExperimentAttachmentDestination(
        @PathVariable("id") long attachmentId,
        Principal principal
    ) {
        final long userId = getUserId(principal);
        //TBD: should we verify attachment exists?
        final NodePath nodePath = storedObjectPaths.experimentAttachmentPath(userId, attachmentId);
        return new UploadFilePathResponse(nodePath.getPath());
    }

    @RequestMapping(value = "/maxSizeInBytes", method = RequestMethod.GET)
    @ResponseBody
    public ValueResponse<Long> getMaxSize() {
        return new ValueResponse<Long>(attachmentManagement.getMaxAttachmentSize());
    }
}
