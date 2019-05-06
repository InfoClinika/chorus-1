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

import com.infoclinika.mssharing.model.UploadLimitException;
import com.infoclinika.mssharing.model.helper.StoredObjectPaths;
import com.infoclinika.mssharing.model.internal.FileNameSpotter;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.read.DetailsReader;
import com.infoclinika.mssharing.model.read.FileLine;
import com.infoclinika.mssharing.model.read.dto.details.FileItem;
import com.infoclinika.mssharing.model.read.dto.details.InstrumentItem;
import com.infoclinika.mssharing.model.write.FileMetaDataInfo;
import com.infoclinika.mssharing.model.write.InstrumentManagement;
import com.infoclinika.mssharing.model.write.dto.UploadLimitCheckResult;
import com.infoclinika.mssharing.platform.fileserver.model.NodePath;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.web.security.RichUser;
import com.infoclinika.mssharing.web.controller.request.CancelUploadRequest;
import com.infoclinika.mssharing.web.controller.request.CheckUploadLimitRequest;
import com.infoclinika.mssharing.web.controller.request.UploadFilesRequest;
import com.infoclinika.mssharing.web.controller.response.UploadFilePathResponse;
import com.infoclinika.mssharing.web.controller.response.UploadFilesResponse;
import com.infoclinika.mssharing.web.controller.response.ValueResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.io.IOException;
import java.security.Principal;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.infoclinika.mssharing.platform.web.security.RichUser.get;
import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;

/**
 * @author Oleksii Tymchenko
 */
@Controller
@RequestMapping("/upload")
public class FileUploadController extends AbstractFileUploadController {

    private final InstrumentManagement instrumentManagement;
    private final StoredObjectPaths storedObjectPaths;
    private final DetailsReader detailsReader;

    @Inject
    public FileUploadController(
        InstrumentManagement instrumentManagement,
        StoredObjectPaths storedObjectPaths,
        DashboardReader dashboardReader,
        DetailsReader detailsReader
    ) {
        super(dashboardReader);
        this.instrumentManagement = instrumentManagement;
        this.storedObjectPaths = storedObjectPaths;
        this.detailsReader = detailsReader;
    }

    @RequestMapping(value = "/checkMultipleFilesValid", method = RequestMethod.GET)
    @ResponseBody
    public ValueResponse checkMultipleFilesValid(@RequestParam long instrument, @RequestParam List<String> fileNames) {
        final boolean multipleFilesValid = instrumentManagement.checkMultipleFilesValidForUpload(instrument, fileNames);
        return new ValueResponse<>(multipleFilesValid);
    }

    @RequestMapping(value = "/isFileAlreadyUploaded", method = RequestMethod.GET)
    @ResponseBody
    public ValueResponse isFileAlreadyUploaded(
        @RequestParam long instrument,
        Principal principal,
        @RequestParam String fileName
    ) {
        final boolean multipleFilesValid =
            instrumentManagement.isFileAlreadyUploadedForInstrument(instrument, getUserId(principal), fileName);

        return new ValueResponse<>(multipleFilesValid);
    }

    @RequestMapping(value = "/checkUploadLimit", method = RequestMethod.GET)
    @ResponseBody
    public ValueResponse checkUploadLimit(CheckUploadLimitRequest request, Principal principal) {

        final RichUser user = get(principal);
        long userId = user.getId();
        final UploadLimitCheckResult uploadLimit =
            instrumentManagement.checkUploadLimit(userId, request.getLabId(), request.getBytesToUpload());
        final boolean uploadAvailable = !uploadLimit.exceeded;

        return new ValueResponse<>(uploadAvailable);
    }

    @RequestMapping(value = "/items", method = RequestMethod.POST)
    @ResponseBody
    public UploadFilesResponse save(@RequestBody UploadFilesRequest uploadFilesRequest, Principal principal)
        throws IOException {

        final RichUser user = get(principal);
        long userId = user.getId();

        if (user.getLabs().isEmpty()) {
            throw new AccessDenied("User isn't permitted to upload file - laboratory is not specified");
        }

        final long instrumentId = uploadFilesRequest.instrument;
        final InstrumentItem instrumentItem = detailsReader.readInstrument(userId, instrumentId);
        final long bytesToUpload = getUploadSize(uploadFilesRequest);

        final UploadLimitCheckResult uploadLimit =
            instrumentManagement.checkUploadLimit(userId, instrumentItem.lab.id, bytesToUpload);

        if (uploadLimit.exceeded) {
            throw new UploadLimitException(uploadLimit.message);
        }

        final LinkedList<UploadFilesResponse.UploadFileResponseLine> storedFilesInfo = new LinkedList<>();
        final List<InstrumentManagement.UploadFileItem> files = uploadFilesRequest.files;

        for (InstrumentManagement.UploadFileItem file : files) {
            final String fileName = FileNameSpotter.replaceInvalidSymbols(file.name);
            final Set<FileLine> unfinishedUploads =
                dashboardReader.readByNameForInstrument(userId, instrumentId, fileName);

            final long finalFileId;
            if (unfinishedUploads.isEmpty()) {
                final NodePath nodePath = storedObjectPaths.rawFilePath(userId, instrumentId, fileName);
                finalFileId = instrumentManagement.startUploadFile(userId, instrumentId,
                    new FileMetaDataInfo(
                        fileName,
                        file.size,
                        file.labels,
                        nodePath.getPath(),
                        file.specie,
                        file.archive
                    )
                );
            } else {
                final FileLine fileLine = unfinishedUploads.iterator().next();
                finalFileId = fileLine.id;

                if (fileLine.toReplace) {
                    final String destinationPath = StringUtils.isNotEmpty(fileLine.contentId) ?
                        fileLine.contentId :
                        fileLine.archiveId;
                    instrumentManagement.updateFile(
                        userId,
                        fileLine.id,
                        new FileMetaDataInfo(
                            fileName,
                            file.size,
                            file.labels,
                            destinationPath,
                            file.specie,
                            file.archive
                        )
                    );
                }
            }

            storedFilesInfo.add(new UploadFilesResponse.UploadFileResponseLine(fileName, finalFileId, ""));
        }

        return new UploadFilesResponse(instrumentId, storedFilesInfo);
    }

    private Long getUploadSize(UploadFilesRequest uploadFilesRequest) {
        long size = 0;
        for (InstrumentManagement.UploadFileItem file : uploadFilesRequest.files) {
            size += file.size;
        }
        return size;
    }


    @RequestMapping(value = "/unfinished-uploads", method = RequestMethod.GET)
    @ResponseBody
    public List<FileLine> getUnfinishedUploads(Principal principal) {
        long userId = getUserId(principal);
        return super.getUnfinishedUploads(userId);
    }

    @RequestMapping(value = "/ping", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void ping(@RequestBody long fileId, Principal principal) {
        instrumentManagement.pingUpload(getUserId(principal), fileId);
    }


    @RequestMapping(value = "/unfinished-uploads", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void removeUnfinishedUploads(Principal principal, @RequestParam Long id) {
        long userId = getUserId(principal);
        instrumentManagement.cancelUpload(userId, id);
    }

    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void cancelUpload(@RequestBody CancelUploadRequest request, Principal principal) {
        final long actor = getUserId(principal);
        for (CancelUploadRequest.FileItem fileItem : request.files) {
            instrumentManagement.cancelUpload(actor, fileItem.id);
        }
    }

    @RequestMapping(value = "/destination/{fileId}", method = RequestMethod.GET)
    @ResponseBody
    public UploadFilePathResponse composeUploadFilePath(@PathVariable("fileId") long fileId, Principal principal) {
        final long userId = getUserId(principal);

        final FileItem fileItem = detailsReader.readFile(userId, fileId);
        final String fileName = FileNameSpotter.replaceInvalidSymbols(fileItem.name);
        final NodePath nodePath = storedObjectPaths.rawFilePath(userId, fileItem.instrumentId, fileName);
        return new UploadFilePathResponse(nodePath.getPath());
    }
}
