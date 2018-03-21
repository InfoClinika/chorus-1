/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.web.controller;

import com.infoclinika.mssharing.model.write.FileMovingManager;
import com.infoclinika.mssharing.model.write.FileOperationsManager;
import com.infoclinika.mssharing.model.write.StudyManagement;
import com.infoclinika.mssharing.platform.web.downloader.DownloadExperimentDeniedException;
import com.infoclinika.mssharing.web.controller.response.ValueResponse;
import com.infoclinika.mssharing.web.downloader.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.Set;

import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;

/**
 * @author Oleksii Tymchenko
 */
@Controller
@RequestMapping("/download")
public class RawFileDownloadController extends ErrorHandler {

    private static final Logger LOG = Logger.getLogger(RawFileDownloadController.class);

    @Inject
    private BulkDownloadHelper bulkDownloadHelper;
    @Inject
    private ChorusSingleFileDownloadHelper singleFileDownloadHelper;
    @Inject
    private FileMovingManager fileMovingManager;
    @Inject
    private FileOperationsManager fileOperationsManager;
    @Inject
    private StudyManagement studyManagement;


    @Value("${base.url}")
    private String baseUrl;

    @RequestMapping(value = "singleFileDownloadUrl", method = RequestMethod.GET)
    @ResponseBody
    public ValueResponse getSingleFileDownloadUrl(@RequestParam(required = true) long file,
                                                  @RequestParam(required = false) Long lab,
                                                  Principal principal) {

        long userId = getUserId(principal);
        final URL downloadUrl = singleFileDownloadHelper.getDownloadUrl(userId, new ChorusDownloadData(file, lab));

        return new ValueResponse<>(downloadUrl.toString());

    }

    @RequestMapping(value = "directDownload", method = RequestMethod.GET)
    public String directDownload(@RequestParam(required = true) long file,
                                        @RequestParam(required = false) Long lab,
                                        Principal principal) {

        long userId = getUserId(principal);
        final URL downloadUrl = singleFileDownloadHelper.getDownloadUrl(userId, new ChorusDownloadData(file, lab));

        return "redirect:" + downloadUrl.toString();
    }

    @RequestMapping(value = "/bulk")
    @ResponseBody
    public void download(@RequestParam(required = false) Set<Long> files, Principal principal,
                         @RequestParam(required = false) Long experiment,
                         @RequestParam(required = false) Long lab,
                         HttpServletRequest request, HttpServletResponse response) throws IOException {
        long userId = getUserId(principal);
        try {
            bulkDownloadHelper.download(new BulkDownloadHelper.ChorusRequest(userId, files, experiment, false, lab), response);
        } catch (DownloadExperimentDeniedException e) {
            response.sendRedirect("/pages/requestDownloadExperiment.html?downloadLink=" + getEncodedDownloadExperimentLink(request));
        } catch (LabToSendBillingNotSpecifiedException e) {
            response.sendRedirect("/pages/dashboard.html#/experiments/all?downloadExperiment=" + experiment);
        }
    }

    private String getEncodedDownloadExperimentLink(HttpServletRequest request) {
        String downloadExperimentUrl = request.getRequestURL().toString() + "?" + request.getQueryString();

        String encodedDownloadExperimentLink = "";
        try {
            encodedDownloadExperimentLink = URLEncoder.encode(downloadExperimentUrl, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error(e);
        }
        return encodedDownloadExperimentLink;
    }

    @RequestMapping(value = "/moveToStorage", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public void moveToStorage(@RequestParam(required = true) Set<Long> files, Principal principal) throws IOException {
        long userId = getUserId(principal);

        fileOperationsManager.makeFilesAvailableForDownload(userId, files);

    }

}
