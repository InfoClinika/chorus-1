/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.web.controller;

import com.infoclinika.mssharing.model.write.IssueManagement;
import com.infoclinika.mssharing.web.controller.request.PostIssueRequest;
import com.infoclinika.mssharing.platform.web.security.RichUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.inject.Inject;
import java.security.Principal;

/**
 * @author Oleksii Tymchenko
 */
@Controller
@RequestMapping("/issues")
public class IssueReportingController extends ErrorHandler{
    private final IssueManagement issueManagement;

    @Inject
    public IssueReportingController(IssueManagement issueManagement) {
        this.issueManagement = issueManagement;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    void save(@RequestBody PostIssueRequest issue, Principal principal) {
        final long userId = RichUser.getUserId(principal);
        issueManagement.postIssue(userId, issue.getTitle(), issue.getContents());
    }
}
