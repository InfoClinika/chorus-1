/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.web.controller;

import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.model.Searcher;
import com.infoclinika.mssharing.model.read.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.security.Principal;

import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;

/**
 * @author Stanislav Kurilin
 */
@Controller
@RequestMapping("/search")
public class SearchController extends PagedItemsController{
    @Inject
    Searcher searcher;

    @RequestMapping("/projects")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Iterable<ProjectLine> projects(Principal principal, @RequestParam String query) {
        return searcher.projects(getUserId(principal), query);
    }

    @RequestMapping("/experiments")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Iterable<ExperimentLine> experiments(Principal principal, @RequestParam String query) {
        return searcher.experiments(getUserId(principal), query);
    }

    @RequestMapping("/files")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Iterable<FileLine> files(Principal principal, @RequestParam String query) {
        return searcher.files(getUserId(principal), query);
    }

    @RequestMapping("/instruments")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Iterable<InstrumentLine> instruments(Principal principal, @RequestParam String query) {
        return searcher.instruments(getUserId(principal), query);
    }

    @RequestMapping("/paged/projects")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PagedItem<ProjectLine> pagedProjects(Principal principal, @RequestParam String query,
                                                                                @RequestParam int page, @RequestParam int items,
                                                                                @RequestParam(required = false) String sortingField, @RequestParam boolean asc) {
        return searcher.pagedProjectsWithId(getUserId(principal), createPagedInfo(page, items, sortingField, asc, query));
    }

    @RequestMapping("/paged/experiments")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PagedItem<ExperimentLine> pagedExperiments(Principal principal, @RequestParam String query,
                                                                                      @RequestParam int page, @RequestParam int items,
                                                                                      @RequestParam(required = false) String sortingField, @RequestParam boolean asc) {
        return searcher.pagedExperiments(getUserId(principal), createPagedInfo(page, items, sortingField, asc, query));
    }

    @RequestMapping("/paged/files")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PagedItem<FileLine> pagedFiles(Principal principal, @RequestParam String query,
                                                                          @RequestParam int page, @RequestParam int items,
                                                                          @RequestParam(required = false) String sortingField, @RequestParam boolean asc) {
        return searcher.pagedFiles(getUserId(principal), createPagedInfo(page, items, sortingField, asc, query));
    }

    @RequestMapping("/paged/instruments")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PagedItem<InstrumentLine> pagedInstruments(Principal principal, @RequestParam String query,
                                                                                      @RequestParam int page, @RequestParam int items,
                                                                                      @RequestParam(required = false) String sortingField, @RequestParam boolean asc) {
        return searcher.pagedInstruments(getUserId(principal), createPagedInfo(page, items, sortingField, asc, query));
    }

    @RequestMapping("/count")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Searcher.Count itemsCount(Principal principal, @RequestParam String query){
        return searcher.getItemsCount(createPagedInfo(1, 1, "", false, query), getUserId(principal));
    }
}
