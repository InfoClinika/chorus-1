/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.web.controller;

import com.infoclinika.mssharing.model.helper.StoredObjectPaths;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.read.DetailsReader;
import com.infoclinika.mssharing.model.read.ProteinDatabaseReader;
import com.infoclinika.mssharing.model.read.ProteinDatabaseReader.ProteinDBDetails;
import com.infoclinika.mssharing.model.read.ProteinDatabaseReader.ProteinDBItem;
import com.infoclinika.mssharing.model.read.ProteinDatabaseReader.ProteinDBLine;
import com.infoclinika.mssharing.model.write.AttachmentManagement;
import com.infoclinika.mssharing.model.write.ExperimentCategory;
import com.infoclinika.mssharing.model.write.ProteinDatabaseManagement;
import com.infoclinika.mssharing.platform.fileserver.model.NodePath;
import com.infoclinika.mssharing.platform.model.read.AttachmentsReaderTemplate;
import com.infoclinika.mssharing.platform.web.uploader.ProjectAttachmentsUploadHelper;
import com.infoclinika.mssharing.web.controller.request.CompleteProteinDatabaseUploadRequest;
import com.infoclinika.mssharing.web.controller.request.ProteinDatabaseRequest;
import com.infoclinika.mssharing.web.controller.request.StartProteinDatabaseUploadRequest;
import com.infoclinika.mssharing.web.controller.response.StartAttachmentUploadResponse;
import com.infoclinika.mssharing.web.controller.response.UploadFilePathResponse;
import com.infoclinika.mssharing.web.controller.response.ValueResponse;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.security.Principal;
import java.util.List;

import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;

/**
 * @author Oleksii Tymchenko, Andrii Loboda
 */
@Controller
@RequestMapping("/proteindbs")
public class ProteinDatabaseController extends AbstractAttachmentsController {

    private static final Logger LOG = Logger.getLogger(ProteinDatabaseController.class);

    @Inject
    private ProteinDatabaseManagement proteinDatabaseManagement;
    @Inject
    private ProteinDatabaseReader proteinDatabaseReader;

    //TODO: Consider remove AbstractAttachmentsController superclass
    @Inject
    public ProteinDatabaseController(ProjectAttachmentsUploadHelper asyncUploadHelper,
                                     DetailsReader detailsReader1,
                                     AttachmentsReaderTemplate attachmentsReaderTemplate, AttachmentManagement attachmentManagement,
                                     StoredObjectPaths storedObjectPaths,
                                     DashboardReader dashboardReader) {
        super(attachmentsReaderTemplate, attachmentManagement, asyncUploadHelper, storedObjectPaths, dashboardReader, detailsReader1);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ProteinDBDetails getProteinDatabase(@PathVariable long id, Principal principal) {
        return proteinDatabaseReader.readProteinDatabase(getUserId(principal), id);
    }

    @RequestMapping(value = "/items", method = RequestMethod.GET)
    @ResponseBody
    public List<ProteinDBItem> getProteinDatabases(Principal principal) {
        return proteinDatabaseReader.readAllAvailableProteinDatabases(getUserId(principal));
    }

    @RequestMapping(value = "/itemsAccessibleByUser", method = RequestMethod.GET)
    @ResponseBody
    public List<ProteinDBLine> getProteinDatabasesAccessibleByUser(Principal principal) {
        return proteinDatabaseReader.readProteinDatabasesAccessibleByUser(getUserId(principal));
    }

    @RequestMapping(value = "/my", method = RequestMethod.GET)
    @ResponseBody
    public List<ProteinDBLine> getUserProteinDatabases(Principal principal) {
        return proteinDatabaseReader.readUserProteinDatabases(getUserId(principal));
    }

    @RequestMapping(value = "/public", method = RequestMethod.GET)
    @ResponseBody
    public List<ProteinDBLine> getPublicProteinDatabases(Principal principal) {
        return proteinDatabaseReader.readPublicProteinDatabases(getUserId(principal));
    }

    /**
     * Save the metadata of the attachment and return the attachment ID.
     *
     * @param request   the request containing the attachment metadata
     * @param principal the user performing the request
     * @return the attachment ID along with the filename so that client is able to match the filename with the attachment ID
     */
    @RequestMapping(value = "/items", method = RequestMethod.POST)
    @ResponseBody
    public StartAttachmentUploadResponse save(@RequestBody final StartProteinDatabaseUploadRequest request, final Principal principal) {
        LOG.debug("Saving the metadata for the protein fasta FB: " + request);
        final long userId = getUserId(principal);
        final long attachmentId = proteinDatabaseManagement.createDatabase(userId, request.name, request.dbType, request.sizeInBytes, request.bPublic, request.bReversed, ExperimentCategory.PROTEOMICS);
        return new StartAttachmentUploadResponse(request.filename, attachmentId);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void removeProteinDatabase(Principal principal, @PathVariable long id) {
        long userId = getUserId(principal);
        proteinDatabaseManagement.deleteDatabase(userId, id);
    }

    /**
     * Complete the attachment of the binary file to the appropriate experiment.
     * Should be called once the experiment creation or editing is confirmed by the client.
     *
     * @param request   the request containing the experiment and attachment IDs to wire up.
     * @param principal the user performing the request
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void updateProteinDatabase(@RequestBody final CompleteProteinDatabaseUploadRequest request, final Principal principal) {
        LOG.debug("Attaching the binary files to the experiment:" + request);
        final long userId = getUserId(principal);
        proteinDatabaseManagement.specifyProteinDatabaseContent(userId, request.proteinDbId, request.contentUrl);
    }

    @RequestMapping(method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public void updateProteinDatabaseDetails(@RequestBody ProteinDatabaseRequest database, Principal principal) {
        final long userId = getUserId(principal);
        proteinDatabaseManagement.updateDatabaseDetails(userId, database.databaseId, database.name, database.typeId);
    }

    @RequestMapping(value = "/destination/{id}", method = RequestMethod.GET)
    @ResponseBody
    public UploadFilePathResponse composeProjectAttachmentDestination(@PathVariable("id") long databaseId, Principal principal) {
        final long userId = getUserId(principal);
        //TBD: should we verify attachment exists?
        final String dbName = proteinDatabaseReader.readProteinDatabase(userId, databaseId).name;
        final NodePath nodePath = storedObjectPaths.proteinDatabasePath(userId, databaseId, dbName);
        return new UploadFilePathResponse(nodePath.getPath());
    }

    @RequestMapping(value = "/maxSizeInBytes", method = RequestMethod.GET)
    @ResponseBody
    public ValueResponse<Long> getMaxSize() {
        return new ValueResponse<Long>(proteinDatabaseManagement.getMaxDatabaseSize());
    }

    @RequestMapping(value = "/allAvailable", method = RequestMethod.GET)
    @ResponseBody
    public List<ProteinDBItem> allAvailable(@RequestParam("experimentId") final long experimentId, Principal principal) {
        return proteinDatabaseReader.readAvailableProteinDatabasesByExperiment(getUserId(principal), experimentId);
    }

    @RequestMapping(value = "/share/{id}", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public void share(@RequestBody ShareDatabaseRequest request, Principal principal) {
        proteinDatabaseManagement.updateDatabaseSharing(getUserId(principal), request.bPublic, request.id);
    }

    @RequestMapping(value = "/duplicate/{id}", method = RequestMethod.POST)
    @ResponseBody
    public ProteinDatabaseManagement.DuplicateResponse duplicate(@PathVariable("id") long databaseId, Principal principal) {
        return proteinDatabaseManagement.duplicateDatabase(getUserId(principal), databaseId);
    }

    public static class ShareDatabaseRequest {
        public boolean bPublic;
        public long id;

        /*package*/ ShareDatabaseRequest() {
        }
    }

}
