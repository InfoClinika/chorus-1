/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.web.controller;


import com.infoclinika.mssharing.platform.model.helper.SharingProjectHelperTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.security.Principal;
import java.util.List;

import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;

/**
 * @author Stanislav Kurilin
 */
@Controller
@RequestMapping("/users")
public class UsersController extends ErrorHandler {

    @Inject
    @Named("defaultSharingProjectShortRecordAdapter")
    private SharingProjectHelperTemplate sharingProjectHelper;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<SharingProjectHelperTemplate.UserDetails> getUsers() {
        return sharingProjectHelper.getAvailableUsers();
    }

    @RequestMapping(value = "/project-collaborators/{experimentId}", method = RequestMethod.GET)
    @ResponseBody
    public List<SharingProjectHelperTemplate.UserDetails> getCollaborators(@PathVariable long experimentId, Principal principal) {
        return sharingProjectHelper.getCollaborators(getUserId(principal), experimentId);
    }

}
