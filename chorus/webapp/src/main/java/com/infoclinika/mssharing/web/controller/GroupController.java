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

import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.model.helper.SecurityHelper;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.read.DetailsReader;
import com.infoclinika.mssharing.model.write.SharingManagement;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.GroupItemTemplate;
import com.infoclinika.mssharing.web.controller.request.GroupOperationRequest;
import com.infoclinika.mssharing.web.controller.response.DetailsResponse;
import com.infoclinika.mssharing.web.controller.response.SuccessErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.security.Principal;

import static com.infoclinika.mssharing.platform.model.read.GroupsReaderTemplate.GroupLine;
import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;

/**
 * @author Oleksii Tymchenko
 */
@Controller
@RequestMapping("/groups")
public class GroupController extends ErrorHandler {

    @Inject
    SecurityHelper securityHelper;
    @Inject
    private DetailsReader detailsReader;
    @Inject
    private DashboardReader dashboardReader;
    @Inject
    private SharingManagement sharingManagement;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ImmutableSet<GroupLine> getGroups(
        Principal principal,
        @RequestParam(required = false, defaultValue = "false")
            boolean includeAllUsers
    ) {
        return dashboardReader.readGroups(getUserId(principal), includeAllUsers);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public DetailsResponse getDetails(@PathVariable final Long id, Principal principal) {
        final GroupItemTemplate group = detailsReader.readGroup(getUserId(principal), id);
        return DetailsResponse.ok(group);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public SuccessErrorResponse save(@RequestBody GroupOperationRequest group, Principal principal) {
        try {
            sharingManagement.createGroup(getUserId(principal), group.getName(), group.getMembers());
        } catch (IllegalArgumentException e) {
            return new SuccessErrorResponse(e.getMessage(), null);
        }
        return new SuccessErrorResponse(null, "Group successfully created");
    }

    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    public SuccessErrorResponse updateGroup(@RequestBody GroupOperationRequest group, Principal principal)
        throws Exception {

        //TODO: [stanislav.kurilin] Pass variable instead of false
        try {
            sharingManagement.setCollaborators(getUserId(principal), group.getId(), group.getMembers(), false);
            sharingManagement.renameGroup(getUserId(principal), group.getId(), group.getName());
        } catch (IllegalArgumentException e) {
            return new SuccessErrorResponse(e.getMessage(), null);
        }
        return new SuccessErrorResponse(null, "Group successfully updated");
    }

    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void removeGroup(@RequestParam long group, Principal principal) throws Exception {
        sharingManagement.removeGroup(getUserId(principal), group);
    }
}
