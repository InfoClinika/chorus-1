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
import com.infoclinika.mssharing.web.controller.request.CompleteExperimentAnnotationRequest;
import com.infoclinika.mssharing.web.controller.request.StartAttachmentUploadRequest;
import com.infoclinika.mssharing.web.controller.response.StartAttachmentUploadResponse;
import com.infoclinika.mssharing.web.controller.response.UploadFilePathResponse;
import com.infoclinika.mssharing.web.controller.response.ValueResponse;
import com.infoclinika.mssharing.web.uploader.ExperimentAnnotationAttachmentUploadHelper;
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
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.infoclinika.mssharing.platform.model.read.AttachmentsReaderTemplate.AttachmentItem;
import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;

/**
 * @author Andrii Loboda
 */
@Controller
@RequestMapping("/annotations/experiment")
public class ExperimentAnnotationsController extends AbstractAttachmentsController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExperimentAnnotationsController.class);

    @Inject
    public ExperimentAnnotationsController(
        ExperimentAnnotationAttachmentUploadHelper asyncUploadHelper,
        AttachmentManagement attachmentManagement,
        AttachmentsReaderTemplate attachmentsReaderTemplate,
        DetailsReader detailsReader,
        StoredObjectPaths storedObjectPaths,
        DashboardReader dashboardReader
    ) {
        super(
            attachmentsReaderTemplate,
            attachmentManagement,
            asyncUploadHelper,
            storedObjectPaths,
            dashboardReader,
            detailsReader
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
        @RequestBody final CompleteExperimentAnnotationRequest request,
        final Principal principal
    ) {
        LOGGER.debug("Attaching the binary files to the experiment: {}", request);
        final long userId = getUserId(principal);

        attachmentManagement.updateExperimentAnnotationAttachment(
            userId,
            request.experimentId,
            request.annotationAttachmentId
        );
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
        LOGGER.debug("Saving the metadata for the annotation: {}", request);
        final long userId = getUserId(principal);
        final long attachmentId =
            attachmentManagement.newAnnotationAttachment(userId, request.filename, request.sizeInBytes);
        return new StartAttachmentUploadResponse(request.filename, attachmentId);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public List<AttachmentItem> getAnnotationForExperiment(
        @PathVariable final Long id,
        Principal principal
    ) {
        LOGGER.debug("Getting annotation for Experiment: {}", id);
        final AttachmentItem attachmentItem =
            detailsReader.readExperimentAnnotationAttachment(getUserId(principal), id);
        if (attachmentItem == null) {
            return Collections.emptyList();
        }

        return newArrayList(attachmentItem);
    }

    @RequestMapping(value = "/download/{id}", method = RequestMethod.GET)
    @ResponseBody
    public void attachmentDownload(
        @PathVariable("id") final long annotationAttachmentId,
        HttpServletRequest request,
        HttpServletResponse response,
        Principal principal
    ) {
        try {
            final long userId = getUserId(principal);
            LOGGER.debug(
                "Got the download request for the experiment annotation attachment with ID = {} from user with ID {} ",
                annotationAttachmentId,
                userId
            );
            final AttachmentItem annotationAttachment =
                detailsReader.readAnnotationAttachmentDetails(userId, annotationAttachmentId);
            postAttachmentToResponse(
                annotationAttachment,
                request,
                response,
                new Function<Long, AttachmentItem>() {
                    @Override
                    public AttachmentItem apply(Long annotationAttachmentId) {
                        return detailsReader.readAnnotationAttachmentDetails(userId, annotationAttachmentId);
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
    public void removeUnfinishedAnnotationAttachment(Principal principal, @PathVariable long id) {
        long userId = getUserId(principal);
        attachmentManagement.discardAnnotationAttachment(userId, id);
    }

    @RequestMapping(value = "/destination/{id}", method = RequestMethod.GET)
    @ResponseBody
    public UploadFilePathResponse composeExperimentAnnotationAttachmentDestination(
        @PathVariable("id") long annotationAttachmentID,
        Principal principal
    ) {
        final long userId = getUserId(principal);
        final NodePath nodePath = storedObjectPaths.experimentAnnotationAttachmentPath(userId, annotationAttachmentID);

        return new UploadFilePathResponse(nodePath.getPath());
    }

    @RequestMapping(value = "/maxSizeInBytes", method = RequestMethod.GET)
    @ResponseBody
    public ValueResponse<Long> getMaxSize() {
        return new ValueResponse<Long>(attachmentManagement.getMaxAttachmentSize());
    }
}

