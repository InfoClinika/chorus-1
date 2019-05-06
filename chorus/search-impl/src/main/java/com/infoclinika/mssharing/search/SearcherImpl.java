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
package com.infoclinika.mssharing.search;

import com.google.common.collect.ImmutableList;
import com.infoclinika.mssharing.model.Searcher;
import com.infoclinika.mssharing.model.internal.entity.Instrument;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveExperiment;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveProject;
import com.infoclinika.mssharing.model.internal.entity.view.ExperimentDashboardRecord;
import com.infoclinika.mssharing.model.internal.read.Transformers;
import com.infoclinika.mssharing.model.internal.repository.ExperimentRepository;
import com.infoclinika.mssharing.model.read.ExperimentLine;
import com.infoclinika.mssharing.model.read.FileLine;
import com.infoclinika.mssharing.model.read.InstrumentLine;
import com.infoclinika.mssharing.model.read.ProjectLine;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import com.infoclinika.mssharing.platform.model.helper.read.InstrumentReaderHelper;
import com.infoclinika.mssharing.platform.model.impl.searcher.DefaultSearcherTemplate;
import com.infoclinika.mssharing.platform.model.read.ExperimentReaderTemplate;
import com.infoclinika.mssharing.platform.repository.InstrumentRepositoryTemplate;
import org.springframework.data.domain.Page;

import javax.inject.Inject;
import java.util.List;

import static com.google.common.collect.FluentIterable.from;
import static com.infoclinika.mssharing.platform.model.DefaultTransformers.toPagedItem;

/**
 * @author Stanislav Kurilin
 */
public class SearcherImpl

    extends

    DefaultSearcherTemplate<
        ActiveProject,
        ActiveExperiment,
        ActiveFileMetaData,
        Instrument,
        ProjectLine,
        ExperimentLine,
        FileLine,
        InstrumentLine
        >

    implements Searcher {

    @Inject
    private Transformers transformers;
    @Inject
    private ExperimentRepository experimentRepository;
    @Inject
    private InstrumentReaderHelper<Instrument, InstrumentLine> instrumentReaderHelper;

    @Override
    public ImmutableList<ExperimentLine> experiments(long actor, String query) {
        final List<ExperimentDashboardRecord> items =
            experimentRepository.searchExperimentsRecords(actor, transformQueryString(query));
        final List<ExperimentLine> result = transformers.transformExperimentRecords(actor, items).toList();
        return from(result).toList();
    }

    @Override
    public PagedItem<ExperimentLine> pagedExperiments(long actor, PagedItemInfo pagedItemInfo) {

        final Page<ExperimentDashboardRecord> itemsPage = experimentRepository.searchPagedExperimentsRecords(
            actor,
            transformQueryString(pagedItemInfo.filterQuery),
            pagedItemsTransformer.toPageRequest(ExperimentTemplate.class, pagedItemInfo)
        );

        final List<ExperimentLine> result = transformers.transformExperimentRecords(actor, itemsPage).toList();

        return new PagedItem<>(
            itemsPage.getTotalPages(),
            itemsPage.getTotalElements(),
            itemsPage.getNumber(),
            itemsPage.getSize(),
            result
        );
    }

    @Override
    protected ImmutableList<FileLine> afterReadFiles(
        List<ActiveFileMetaData> activeFileMetaDatas,
        long actor,
        String query
    ) {

        return from(activeFileMetaDatas)
            .transform(transformers.transformToFileLineFunction(actor))
            .toList();

    }


    @Override
    protected PagedItem<FileLine> afterReadFilesPage(
        Page<ActiveFileMetaData> itemsPage,
        long actor,
        PagedItemInfo pagedItemInfo
    ) {

        return toPagedItem(itemsPage, transformers.transformToFileLineFunction(actor));

    }

    @Override
    protected ProjectLine transformProject(ActiveProject activeProject) {
        return transformers.projectTransformer.apply(activeProject);
    }

    @Override
    protected FileLine transformFile(ActiveFileMetaData activeFileMetaData) {
        return null;
    }

    @Override
    protected ExperimentLine transformExperiment(ActiveExperiment experiment) {
        final ExperimentReaderTemplate.ExperimentLineTemplate lineTemplate =
            transformers.defaultExperimentTransformer().apply(experiment);
        return new ExperimentLine(lineTemplate, experiment.getBillLaboratory().getId());
    }

    @Override
    protected InstrumentLine transformInstrument(InstrumentRepositoryTemplate.AccessedInstrument<Instrument> accessed) {
        return new InstrumentLine(
            instrumentReaderHelper.getDefaultTransformer().apply(accessed));
    }

    @Override
    public boolean isSearchEnabled() {
        return true;
    }


}
