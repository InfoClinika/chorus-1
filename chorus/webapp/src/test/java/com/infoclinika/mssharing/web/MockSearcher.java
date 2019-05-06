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
package com.infoclinika.mssharing.web;

import com.google.common.collect.ImmutableList;
import com.infoclinika.mssharing.model.Searcher;
import com.infoclinika.mssharing.model.read.ExperimentLine;
import com.infoclinika.mssharing.model.read.FileLine;
import com.infoclinika.mssharing.model.read.InstrumentLine;
import com.infoclinika.mssharing.model.read.ProjectLine;
import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;

/**
 * @author Stanislav Kurilin
 */
public class MockSearcher implements Searcher {
    @Override
    public boolean isSearchEnabled() {
        return true;
    }

    @Override
    public ImmutableList<ProjectLine> projects(long actor, String query) {
        return null;
    }

    @Override
    public ImmutableList<ExperimentLine> experiments(long actor, String query) {
        return null;
    }

    @Override
    public ImmutableList<FileLine> files(long actor, String query) {
        return null;
    }

    @Override
    public ImmutableList<InstrumentLine> instruments(long actor, String query) {
        return null;
    }

    @Override
    public PagedItem<ProjectLine> pagedProjects(long actor, PagedItemInfo pagedItemInfo) {
        return null;
    }

    @Override
    public PagedItem<ProjectLine> pagedProjectsWithId(long actor, PagedItemInfo pagedItemInfo) {
        return null;
    }

    @Override
    public PagedItem<ExperimentLine> pagedExperiments(long actor, PagedItemInfo pagedItemInfo) {
        return null;
    }

    @Override
    public PagedItem<FileLine> pagedFiles(long actor, PagedItemInfo pagedItemInfo) {
        return null;
    }

    @Override
    public PagedItem<InstrumentLine> pagedInstruments(long actor, PagedItemInfo pagedItemInfo) {
        return null;
    }

    @Override
    public Count getItemsCount(PagedItemInfo pagedItemInfo, long actor) {
        return null;
    }
}
