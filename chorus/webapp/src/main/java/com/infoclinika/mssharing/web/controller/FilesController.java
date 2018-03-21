/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.web.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.infoclinika.mssharing.dto.FileDescription;
import com.infoclinika.mssharing.model.PaginationItems;
import com.infoclinika.mssharing.model.read.AdministrationToolsReader;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.read.DetailsReader;
import com.infoclinika.mssharing.model.read.FileLine;
import com.infoclinika.mssharing.model.read.dto.details.FileItem;
import com.infoclinika.mssharing.model.write.FileOperationsManager;
import com.infoclinika.mssharing.model.write.InstrumentManagement;
import com.infoclinika.mssharing.model.write.StudyManagement;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.common.items.InstrumentItem;
import com.infoclinika.mssharing.platform.model.common.items.VendorItem;
import com.infoclinika.mssharing.platform.model.read.Filter;
import com.infoclinika.mssharing.web.ResourceDeniedException;
import com.infoclinika.mssharing.web.controller.request.BulkFileLabelUpdateRequest;
import com.infoclinika.mssharing.web.controller.request.BulkFileUpdateSpeciesRequest;
import com.infoclinika.mssharing.web.controller.request.FileOperationRequest;
import com.infoclinika.mssharing.web.controller.request.PageRequest;
import com.infoclinika.mssharing.web.controller.response.ChartUrlResponse;
import com.infoclinika.mssharing.web.controller.response.ValueResponse;
import com.infoclinika.mssharing.web.rest.FilesReadyToUploadRequest;
import com.infoclinika.mssharing.web.rest.FilesReadyToUploadResponse;
import com.infoclinika.mssharing.web.uploader.FileUploadHelper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Sets.newHashSet;
import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;

/**
 * @author Oleksii Tymchenko
 */
@Controller
@RequestMapping("/files")
public class FilesController extends PagedItemsController {

    private static final Logger LOGGER = Logger.getLogger(FilesController.class);
    private static final String USER = "User ";
    private static final String READING_FILES_BY_INSTRUMENT_WITH_ID = "Reading files by instrument with ID: ";

    @Inject
    private DashboardReader dashboardReader;

    @Inject
    private DetailsReader detailsReader;

    @Inject
    private InstrumentManagement instrumentManagement;

    @Inject
    private StudyManagement studyManagement;

    @Inject
    private AdministrationToolsReader administrationToolsReader;

    @Value("${base.url}")
    private String baseUrl;
    @Inject
    private FileOperationsManager fileOperationsManager;

    public FilesController() {
    }

    @RequestMapping(value = "/bylab/{labId}", method = RequestMethod.GET)
    @ResponseBody
    public Set<FileLine> myLabFiles(@PathVariable("labId") long labId, Principal principal) {

        final long userId = getUserId(principal);
        //this is basically the same as just my files for now
        return dashboardReader.readFilesByLab(userId, labId);
    }

    @RequestMapping(value = "/{filter}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Set<FileLine> getFiles(@PathVariable("filter") Filter filter, Principal principal) {
        final long userId = getUserId(principal);
        return dashboardReader.readFiles(userId, filter);
    }

    @RequestMapping(value = "/by-experiment/{experiment}", method = RequestMethod.GET)
    @ResponseBody
    public Set<FileLine> getExperimentFiles(Principal principal, @PathVariable long experiment) {
        try {
            return dashboardReader.readFilesByExperiment(getUserId(principal), experiment);
        } catch (AccessDenied e) {
            throw new ResourceDeniedException(e.getMessage());
        }
    }

    @RequestMapping(value = "/my/instrument/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Set<FileLine> filesByInstrument(@PathVariable("id") final Long instrumentId, Principal principal) {

        LOGGER.debug(READING_FILES_BY_INSTRUMENT_WITH_ID + instrumentId);
        final long userId = getUserId(principal);
        return dashboardReader.readFilesByInstrument(userId, instrumentId);
    }

    @RequestMapping(value = "/isReadyToUpload", method = RequestMethod.POST)
    @ResponseBody
    public ValueResponse<FilesReadyToUploadResponse> isReadyToUpload(
            @RequestBody FilesReadyToUploadRequest request,
            Principal principal
    ) {
        final long instrumentId = request.instrumentId;
        LOGGER.debug("Checking if files are ready to upload. Instrument: " + instrumentId);

        final InstrumentItem instrument = dashboardReader.readInstrument(instrumentId);
        final VendorItem vendor = instrument.vendor;

        final FileDescription[] fileDescriptions = FileUploadHelper.filesReadyToUpload(
                getUserId(principal),
                instrumentId,
                vendor,
                request.fileDescriptions,
                instrumentManagement,
                dashboardReader
        );

        final FilesReadyToUploadResponse response = new FilesReadyToUploadResponse();
        response.fileDescriptions = fileDescriptions;

        return new ValueResponse<>(response);
    }

    @RequestMapping(value = "/details/{id}", method = RequestMethod.GET)
    @ResponseBody
    public FileItem fileDetails(@PathVariable final Long id, Principal principal) {
        try {
            return detailsReader.readFile(getUserId(principal), id);
        } catch (AccessDenied e) {
            throw new ResourceDeniedException(e.getMessage());
        }
    }

    @RequestMapping(value = "/detailsWithConditions/{experimentId}/{id}", method = RequestMethod.GET)
    @ResponseBody
    public FileItem fileDetailsWithConditions(@PathVariable final Long id, @PathVariable final Long experimentId, Principal principal) {
        try {
            return detailsReader.readFileDetailsWithConditions(getUserId(principal), id, experimentId);
        } catch (AccessDenied e) {
            throw new ResourceDeniedException(e.getMessage());
        }
    }

    @RequestMapping(method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public void updateFile(@RequestBody FileOperationRequest fileOperationRequest, Principal principal) throws Exception {
        //TODO: add validation
        instrumentManagement.setLabels(getUserId(principal), fileOperationRequest.getFileId(),
                fileOperationRequest.getLabels());
    }

    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void removeFiles(@RequestParam List<Long> files, Principal principal) {
        instrumentManagement.moveFilesToTrash(getUserId(principal), files);
    }

    @RequestMapping(value = "/delete-permanently", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void removePermanently(@RequestParam Set<Long> files, Principal principal) {
        instrumentManagement.removeFilesPermanently(getUserId(principal), files);
    }

    @RequestMapping(value = "/bulk/labels", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public void bulkUpdateLabels(@RequestBody BulkFileLabelUpdateRequest request, Principal principal) {
        final Set<Long> fileIds = new HashSet<Long>(request.getFileIds());
        instrumentManagement.bulkSetLabels(getUserId(principal), fileIds, request.getNewValue(), request.isAppendLabels());
    }

    @RequestMapping(value = "/bulk/species", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public void bulkUpdateSpecies(@RequestBody BulkFileUpdateSpeciesRequest request, Principal principal) {
        final Set<Long> fileIds = new HashSet<Long>(request.getFileIds());
        instrumentManagement.bulkSetSpecies(getUserId(principal), fileIds, request.getNewValue());
    }


    //Paged files

    @RequestMapping(value = "paged/bylab/{labId}", method = RequestMethod.POST)
    @ResponseBody
    public PagedItem<FileLine> myLabFiles(@PathVariable("labId") long labId,
                                          @RequestBody PagedFileRequest request,
                                          Principal principal) {

        final long userId = getUserId(principal);

        return dashboardReader.readFilesByLab(userId, labId, createPagedInfo(request.page, request.items, request.sortingField, request.asc, request.filterQuery, request.advancedFilter));
    }

    @RequestMapping(value = "paged/my/instrument/{id}", method = RequestMethod.POST)
    @ResponseBody
    public PagedItem<FileLine> filesByInstrument(@PathVariable("id") final Long instrumentId,
                                                 @RequestBody PagedFileRequest request,
                                                 Principal principal) {

        LOGGER.debug(READING_FILES_BY_INSTRUMENT_WITH_ID + instrumentId);
        final long userId = getUserId(principal);
        return dashboardReader.readFilesByInstrument(userId, instrumentId, createPagedInfo(request.page, request.items, request.sortingField, request.asc, request.filterQuery, request.advancedFilter));
    }


    @RequestMapping(value = "/paged/{filter}", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public PagedItem<FileLine> getFiles(@PathVariable("filter") Filter filter,
                                        @RequestBody PagedFileRequest request,
                                        Principal principal) {
        final long userId = getUserId(principal);
        return dashboardReader.readFiles(userId, filter, createPagedInfo(request.page, request.items, request.sortingField, request.asc, request.filterQuery, request.advancedFilter));
    }

    @RequestMapping(value = "/paged/by-experiment/{experiment}", method = RequestMethod.POST)
    @ResponseBody
    public PagedItem<FileLine> getExperimentFiles(@PathVariable long experiment,
                                                  @RequestBody PagedFileRequest request, Principal principal) {
        try {
            return dashboardReader.readFilesByExperiment(getUserId(principal), experiment, createPagedInfo(request.page, request.items, request.sortingField, request.asc, request.filterQuery, request.advancedFilter));
        } catch (AccessDenied e) {
            throw new ResourceDeniedException(e.getMessage());
        }
    }

    @RequestMapping(value = "/charts/url", method = RequestMethod.GET)
    @ResponseBody
    public ChartUrlResponse getChartUrlForFiles(@RequestParam(value = "fileIds", required = true) String[] rawIds, Principal principal) {
        final List<Long> ids = Lists.newArrayListWithCapacity(rawIds.length);
        for (String fileIdString : rawIds) {
            ids.add(Long.parseLong(fileIdString));
        }
        final String url = dashboardReader.getChartsUrlForFiles(getUserId(principal), ids);
        return new ChartUrlResponse(url);
    }

    @RequestMapping(value = "/archive", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public void archiveFiles(@RequestBody FilesRequest request, Principal principal) {
        LOGGER.info(USER + getUserId(principal) + " requested archive files: " + request);
        fileOperationsManager.markFilesToArchive(getUserId(principal), copyOf(request.files));
    }

    @RequestMapping(value = "/un-archive", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public void unarchiveFiles(@RequestBody FilesRequest request, Principal principal) {
        LOGGER.info(USER + getUserId(principal) + " requested Un-archive files: " + request);
        fileOperationsManager.markFilesToUnarchive(getUserId(principal), copyOf(request.files));
    }

    private static class FilesRequest {
        public List<Long> files;
        public long lab;
        public boolean metadataOnly;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("files", files)
                    .add("lab", lab)
                    .add("metadataOnly", metadataOnly)
                    .toString();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PagedFileRequest {
        public int page;
        public int items;
        public String sortingField;
        public boolean asc;
        public String filterQuery;// nullable
        public PaginationItems.AdvancedFilterQueryParams advancedFilter;// nullable
    }
}
