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

import com.google.common.base.Optional;
import com.infoclinika.mssharing.model.PaginationItems;
import com.infoclinika.mssharing.model.PaginationItems.PagedItem;
import com.infoclinika.mssharing.model.jira.JiraService;
import com.infoclinika.mssharing.model.jira.JiraService.BasicIssueProperty;
import com.infoclinika.mssharing.model.write.IssueManagement;
import com.infoclinika.mssharing.model.write.IssuesService;
import com.infoclinika.mssharing.model.write.IssuesService.IssueDetails;
import com.infoclinika.mssharing.model.write.IssuesService.IssueShortDetails;
import com.infoclinika.mssharing.platform.web.security.RichUser;
import com.infoclinika.mssharing.web.controller.request.PostIssueRequest;
import com.infoclinika.mssharing.web.controller.request.ReportIssueRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.security.Principal;
import java.util.List;

import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;

/**
 * @author Oleksii Tymchenko
 */
@Controller
@RequestMapping("/issues")
public class IssueReportingController extends ErrorHandler {
    private final IssueManagement issueManagement;
    private final JiraService jiraService;
    private final IssuesService issuesService;

    @Inject
    public IssueReportingController(
        IssueManagement issueManagement, JiraService jiraService,
        IssuesService issuesService
    ) {
        this.issueManagement = issueManagement;
        this.jiraService = jiraService;
        this.issuesService = issuesService;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    void save(@RequestBody PostIssueRequest issue, Principal principal) {
        final long userId = RichUser.getUserId(principal);
        issueManagement.postIssue(userId, issue.getTitle(), issue.getContents());
    }

    @RequestMapping(value = "/report", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void reportIssue(@RequestBody ReportIssueRequest request, Principal principal) {
        final long actor = RichUser.getUserId(principal);
        issuesService.reportIssue(
            actor,
            request.getIssueType(),
            request.getPriority(),
            request.getTitle(),
            request.getDescription(),
            request.getStepsToReproduce(),
            request.getAttachments()
        );
    }

    @RequestMapping(value = "/edit/{id}", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void reportIssue(
        @PathVariable(value = "id") Long id,
        @RequestBody ReportIssueRequest request,
        Principal principal
    ) {
        final long actor = RichUser.getUserId(principal);
        issuesService.editIssue(
            actor,
            id,
            request.getIssueType(),
            request.getPriority(),
            request.getTitle(),
            request.getDescription(),
            request.getStepsToReproduce(),
            request.getAttachments()
        );
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteIssue(@PathVariable(value = "id") Long id, Principal principal) {
        final long actor = getUserId(principal);
        issuesService.deleteIssue(actor, id);
    }

    @RequestMapping(value = "/my-issues", method = RequestMethod.GET)
    @ResponseBody
    public PagedItem<IssueShortDetails> getUserIssues(
        @RequestParam int page, @RequestParam int items,
        @RequestParam String sortingField, @RequestParam boolean asc,
        Principal principal
    ) {
        final long actor = getUserId(principal);
        final PaginationItems.PagedItemInfo pagedItemInfo = new PaginationItems.PagedItemInfo(items, page - 1,
            sortingField, asc, null, Optional.absent()
        );
        return issuesService.readUserIssues(actor, pagedItemInfo);
    }

    @RequestMapping(value = "/all-issues", method = RequestMethod.GET)
    @ResponseBody
    public PagedItem<IssueShortDetails> getAllIssues(
        @RequestParam int page, @RequestParam int items,
        @RequestParam String sortingField, @RequestParam boolean asc,
        Principal principal
    ) {
        final long actor = getUserId(principal);
        final PaginationItems.PagedItemInfo pagedItemInfo = new PaginationItems.PagedItemInfo(items, page - 1,
            sortingField, asc, null, Optional.absent()
        );
        return issuesService.readAllIssues(actor, pagedItemInfo);
    }

    @RequestMapping(value = "/details/{id}", method = RequestMethod.GET)
    @ResponseBody
    public IssueDetails getAllIssues(@PathVariable(value = "id") Long id, Principal principal) {
        final long actor = getUserId(principal);
        return issuesService.readIssueDetails(actor, id);
    }

    @RequestMapping(value = "/priorities", method = RequestMethod.GET)
    @ResponseBody
    public List<BasicIssueProperty> getPriorities(Principal principal) {
        final long actor = RichUser.getUserId(principal);
        return jiraService.getPriorities();
    }

}
