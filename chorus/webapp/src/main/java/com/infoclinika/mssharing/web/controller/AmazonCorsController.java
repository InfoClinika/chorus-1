/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.web.controller;

import com.infoclinika.mssharing.model.helper.CloudFileHelper;
import com.infoclinika.mssharing.platform.model.helper.CorsRequestSignerTemplate;
import com.infoclinika.mssharing.model.write.InstrumentManagement;
import com.infoclinika.mssharing.web.controller.response.ChunkUrlResponse;
import com.infoclinika.mssharing.web.services.upload.cors.*;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;

import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;

/**
 * @author Oleksii Tymchenko
 */
@Controller
@RequestMapping("/cors")
public class AmazonCorsController {

    private static final Logger LOGGER = Logger.getLogger(AmazonCorsController.class);

    @Inject
    private CorsRequestSignerTemplate requestSigner;
    @Inject
    private InstrumentManagement instrumentManagement;
    @Inject
    private CloudFileHelper cloudFileHelper;

    //used when no multipart upload is available, i.e. when file size is <= chunk size
    @RequestMapping(value = "/sign/singlefile", method = RequestMethod.POST)
    @ResponseBody
    public ChunkUrlResponse signSingleFileUpload(@RequestBody SingleFileUploadRequest request, Principal principal) {
        LOGGER.debug("Signing the upload request for single file: " + request.objectName);
        final long userId = getUserId(principal);
        final String signedUrl = requestSigner.signSingleFileUploadRequest(userId, request.objectName);
        return new ChunkUrlResponse(signedUrl, requestSigner.useServerSideEncryption());
    }

    //multipart upload methods

    @RequestMapping(value = "/sign/initial", method = RequestMethod.POST)
    @ResponseBody
    public SignedCorsUploadSseResponse signInitialUploadRequest(@RequestBody InitialMultipartCorsUploadRequest request, Principal principal) {
        final long userId = getUserId(principal);
        final CorsRequestSignerTemplate.SignedRequest signed = requestSigner.signInitialUploadRequest(userId, request.objectName);
        return new SignedCorsUploadSseResponse(signed.authorization, signed.host, signed.dateAsString, requestSigner.useServerSideEncryption());
    }

    @RequestMapping(value = "/sign/part", method = RequestMethod.POST)
    @ResponseBody
    public SignedCorsUploadResponse signUploadPartRequest(@RequestBody UploadPartCorsRequest request, Principal principal) {
        final long userId = getUserId(principal);
        final CorsRequestSignerTemplate.SignedRequest signed = requestSigner.signUploadPartRequest(userId, request.objectName, request.partNumber, request.uploadId);
        return new SignedCorsUploadResponse(signed.authorization, signed.host, signed.dateAsString);
    }

    @RequestMapping(value = "/sign/list", method = RequestMethod.POST)
    @ResponseBody
    public SignedCorsUploadResponse signListPartsRequest(@RequestBody ListPartsCorsRequest request, Principal principal) {
        final long userId = getUserId(principal);
        final CorsRequestSignerTemplate.SignedRequest signed = requestSigner.signListPartsRequest(userId, request.objectName, request.uploadId);
        return new SignedCorsUploadResponse(signed.authorization, signed.host, signed.dateAsString);
    }

    @RequestMapping(value = "/sign/abort", method = RequestMethod.POST)
    @ResponseBody
    public SignedCorsUploadResponse signAbortUploadRequest(@RequestBody AbortMultipartCorsUploadRequest request, Principal principal) {
        final long userId = getUserId(principal);
        final CorsRequestSignerTemplate.SignedRequest signed = requestSigner.signAbortUploadRequest(userId, request.objectName, request.uploadId);
        return new SignedCorsUploadResponse(signed.authorization, signed.host, signed.dateAsString);
    }

    @RequestMapping(value = "/sign/complete", method = RequestMethod.POST)
    @ResponseBody
    public SignedCorsUploadResponse signCompleteUploadRequest(@RequestBody CompleteMultipartUploadCorsRequest request, Principal principal) {
        final long userId = getUserId(principal);
        final CorsRequestSignerTemplate.SignedRequest signed = requestSigner.signCompleteUploadRequest(userId, request.objectName, request.uploadId, request.addCharsetToContentType);
        return new SignedCorsUploadResponse(signed.authorization, signed.host, signed.dateAsString);
    }

    @RequestMapping(value = "/confirm", method = RequestMethod.POST)
    public void confirmFileUpload(@RequestBody ConfirmMultipartUploadRequest request, Principal principal, HttpServletResponse response) {

        final long amazonFileSize = cloudFileHelper.getFileSize(request.remoteDestination);

        if(amazonFileSize != request.fileSize) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        } else {
            final long userId = getUserId(principal);
            instrumentManagement.completeMultipartUpload(userId, request.fileId, request.remoteDestination);
            response.setStatus(HttpStatus.OK.value());
        }

    }

    @RequestMapping(value = "/startmultipart", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void startMultipartUpload(@RequestBody StartMultipartUploadRequest request, Principal principal) {
        final long userId = getUserId(principal);
        instrumentManagement.startMultipartUpload(userId, request.fileId, request.uploadId, request.destinationPath);
    }


}
