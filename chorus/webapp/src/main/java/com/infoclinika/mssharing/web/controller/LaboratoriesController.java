/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.web.controller;

import com.infoclinika.mssharing.platform.model.common.items.LabItem;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.read.DetailsReader;
import com.infoclinika.mssharing.model.write.LabManagement;
import com.infoclinika.mssharing.model.write.UserManagement;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.Filter;
import com.infoclinika.mssharing.platform.model.read.LabReaderTemplate;
import com.infoclinika.mssharing.platform.model.write.LabManagementTemplate;
import com.infoclinika.mssharing.web.controller.request.LaboratoryOperationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.security.Principal;
import java.util.Set;

import static com.infoclinika.mssharing.platform.web.security.RichUser.get;
import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;

/**
 * @author Oleksii Tymchenko
 */
@Controller
@RequestMapping("/laboratories")
public class LaboratoriesController extends ErrorHandler{

    @Inject
    private DashboardReader dashboardReader;
    @Inject
    private LabManagement labManagement;
    @Inject
    private DetailsReader detailsReader;

    @RequestMapping(value = "/{filter}/labitems", method = RequestMethod.GET)
    @ResponseBody
    public  Set<LabItem>  getLaboratoriesInfo(@PathVariable final Filter filter, Principal principal) {
        return dashboardReader.readLabItems(getUserId(principal));
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Set<LabReaderTemplate.LabLineTemplate> getLaboratoriesByPrincipal(Principal principal) {
        return dashboardReader.readUserLabs(getUserId(principal));
    }

    @RequestMapping(value = "/short/{id}", method = RequestMethod.GET)
    @ResponseBody
    public LabReaderTemplate.LabLineTemplate getLabInfo(@PathVariable final long id, Principal principal) {
        return dashboardReader.readLab(id);
    }

    @RequestMapping(value = "/short", method = RequestMethod.GET)
    @ResponseBody
    public LabReaderTemplate.LabLineTemplate getLabInfo(@RequestParam("name") String name, Principal principal) {
        return dashboardReader.readLabByName(name);
    }

    @RequestMapping(value = "/{filter}", method = RequestMethod.GET)
    @ResponseBody
    public Set<LabReaderTemplate.LabLineTemplate> getLaboratories(@PathVariable final Filter filter, Principal principal) {
        return dashboardReader.readAllLabs(getUserId(principal));
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void save(@RequestBody LaboratoryOperationRequest laboratoryOperationRequest, Principal principal) {

        //TODO: add validation

        UserManagement.PersonInfo personInfoLab = new UserManagement.PersonInfo(laboratoryOperationRequest.getHeadFirstName(),
                laboratoryOperationRequest.getHeadLastName(), laboratoryOperationRequest.getHeadEmail());
        long lab = labManagement.requestLabCreation(
                new LabManagementTemplate.LabInfoTemplate(laboratoryOperationRequest.getInstitutionUrl(), personInfoLab,
                        laboratoryOperationRequest.getName()), get(principal).getUsername());

        labManagement.confirmLabCreation(getUserId(principal), lab);
    }

    @RequestMapping(value = "/details/{id}", method = RequestMethod.GET)
    @ResponseBody
    public DetailsReaderTemplate.LabItemTemplate getDetails(@PathVariable final Long id, Principal principal) {
        return detailsReader.readLab(getUserId(principal), id);
    }

    @RequestMapping(method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public void updateLaboratory(@RequestBody LaboratoryOperationRequest laboratoryOperationRequest, Principal principal) throws Exception {

        UserManagement.PersonInfo personInfo = new UserManagement.PersonInfo(laboratoryOperationRequest.getHeadFirstName(),
                laboratoryOperationRequest.getHeadLastName(), laboratoryOperationRequest.getHeadEmail());
        LabManagementTemplate.LabInfoTemplate labInfo = new LabManagementTemplate.LabInfoTemplate(laboratoryOperationRequest.getInstitutionUrl(),
                personInfo, laboratoryOperationRequest.getName());
        labManagement.editLab(getUserId(principal), laboratoryOperationRequest.getId(), labInfo);
    }

}
