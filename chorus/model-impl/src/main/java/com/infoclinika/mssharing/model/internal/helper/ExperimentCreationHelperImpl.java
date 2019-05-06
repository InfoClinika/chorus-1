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
package com.infoclinika.mssharing.model.internal.helper;

import com.google.common.base.Optional;
import com.infoclinika.mssharing.model.PaginationItems;
import com.infoclinika.mssharing.model.PaginationItems.AdvancedFilterQueryParams;
import com.infoclinika.mssharing.model.PaginationItems.AdvancedFilterQueryParams.AdvancedFilterPredicateItem;
import com.infoclinika.mssharing.model.helper.ExperimentCreationHelper;
import com.infoclinika.mssharing.model.internal.entity.ExperimentLabelType;
import com.infoclinika.mssharing.model.internal.entity.Instrument;
import com.infoclinika.mssharing.model.internal.entity.restorable.LibraryPrep;
import com.infoclinika.mssharing.model.internal.read.Transformers;
import com.infoclinika.mssharing.model.internal.repository.*;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.read.FileLine;
import com.infoclinika.mssharing.platform.entity.InstrumentModel;
import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.common.items.DictItem;
import com.infoclinika.mssharing.platform.model.impl.helper.adapters.DefaultExperimentCreationHelperAdapter;
import com.infoclinika.mssharing.platform.model.read.Filter;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate.Restriction;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Stanislav Kurilin
 */
@Service
public class ExperimentCreationHelperImpl extends DefaultExperimentCreationHelperAdapter
    implements ExperimentCreationHelper {

    private static final String ID_FIELD = "id";
    @Inject
    private ExperimentLabelRepository experimentLabelRepository;
    @Inject
    private ExperimentLabelTypeRepository experimentLabelTypeRepository;
    @Inject
    private InstrumentRepository instrumentRepository;
    @Inject
    private DashboardReader dashboardReader;
    @Inject
    private NtExtractionMethodRepository ntExtractionMethodRepository;
    @Inject
    private NgsExperimentTypeRepository ngsExperimentTypeRepository;

    @Override
    public Set<ExperimentLabelItem> experimentLabels() {
        return experimentLabelRepository
            .findAll()
            .stream()
            .map(label -> new ExperimentLabelItem(
                label.getId(),
                label.getName(),
                label.getType().getId(),
                label.getAcid()
            ))
            .collect(Collectors.toSet());
    }

    @Override
    public Set<ExperimentLabelItem> experimentLabels(long experimentLabelType) {
        final ExperimentLabelType labelType = experimentLabelTypeRepository.findOne(experimentLabelType);
        return experimentLabelRepository
            .findByType(labelType)
            .stream()
            .map(label -> new ExperimentLabelItem(
                label.getId(),
                label.getName(),
                label.getType().getId(),
                label.getAcid()
            ))
            .collect(Collectors.toSet());
    }


    @Override
    public Set<ExperimentLabelTypeItem> experimentLabelTypes() {
        return experimentLabelTypeRepository.findAll()
            .stream()
            .map(type -> new ExperimentLabelTypeItem(type.getId(), type.getName(), type.getMaxSamples()))
            .collect(Collectors.toSet());
    }

    private PaginationItems.PagedItemInfo createEmptyPagedItemInfo() {
        return new PaginationItems.PagedItemInfo(1, 0, ID_FIELD, false, "", Optional.absent());
    }

    @Override
    public boolean hasFilesByParams(long actor, Long speciesId, Long instrumentId, Long instrumentModelId, Long labId) {
        final PagedItem<FileLine> files = getFiles(actor, speciesId, instrumentId, instrumentModelId, labId);
        return !files.items.isEmpty();
    }

    @Override
    public Restriction getRestrictionForInstrument(long instrumentId) {
        final Instrument instrument = instrumentRepository.findOne(instrumentId);
        final InstrumentModel model = instrument.getModel();
        return new Restriction(
            model.getStudyType().getId(),
            model.getVendor().getId(),
            model.getType().getId(),
            model.getId(),
            Optional.of(instrumentId)
        );
    }

    @Override
    public List<DictItem<String, String>> getNgsLibraryPrepTypes() {
        return Arrays
            .stream(LibraryPrep.values())
            .map(l -> new DictItem<>(l.name(), l.getTitle()))
            .collect(Collectors.toList());
    }

    @Override
    public List<DictItem<Integer, String>> getNgsExperimentPrepMethodsByExperimentType(int ngsExperimentTypeId) {
        return ngsExperimentTypeRepository
            .findOne(ngsExperimentTypeId)
            .getPrepMethods()
            .stream()
            .map(m -> new DictItem<>(m.getId(), m.getTitle()))
            .collect(Collectors.toList());
    }

    @Override
    public List<DictItem<Long, String>> getNtExtractionMethods() {
        return ntExtractionMethodRepository
            .findAll()
            .stream()
            .map(m -> new DictItem<>(m.getId(), m.getTitle()))
            .collect(Collectors.toList());
    }

    @Override
    public List<DictItem<Integer, String>> getNgsExperimentTypes() {
        return ngsExperimentTypeRepository
            .findAll()
            .stream()
            .map(t -> new DictItem<>(t.getId(), t.getTitle()))
            .collect(Collectors.toList());
    }

    private PagedItem<FileLine> getFiles(long actor,
                                         Long speciesId,
                                         Long instrumentId,
                                         Long instrumentModelId,
                                         Long labId) {
        final PaginationItems.PagedItemInfo info = createEmptyPagedItemInfo();
        return dashboardReader.filterPageableFile(
            actor,
            Filter.ALL,
            new PaginationItems.PagedItemInfo(
                info.items,
                info.page,
                info.sortingField,
                info.isSortingAsc,
                info.filterQuery,
                Optional.of(createAdvancedFilter(speciesId, instrumentId, instrumentModelId, labId))
            )
        );
    }

    private AdvancedFilterQueryParams createAdvancedFilter(Long speciesId,
                                                           Long instrumentId,
                                                           Long instrumentModelId,
                                                           Long labId) {

        final AdvancedFilterQueryParams params = new AdvancedFilterQueryParams(true, new LinkedList<>());

        if (speciesId != null && speciesId > 0) {
            params.predicates.add(new AdvancedFilterPredicateItem(
                    Transformers.SPECIE_ID_FIELD,
                    Long.toString(speciesId),
                    AdvancedFilterPredicateItem.AdvancedFilterOperator.EQUAL
                )
            );
        }

        if (instrumentId != null && instrumentId > 0) {
            params.predicates.add(new AdvancedFilterPredicateItem(
                    Transformers.INSTRUMENT_ID_FIELD,
                    Long.toString(instrumentId),
                    AdvancedFilterPredicateItem.AdvancedFilterOperator.EQUAL
                )
            );
        }

        if (instrumentModelId != null && instrumentModelId > 0) {
            params.predicates.add(new AdvancedFilterPredicateItem(
                    Transformers.INSTRUMENT_MODEL_ID_FIELD,
                    Long.toString(instrumentModelId),
                    AdvancedFilterPredicateItem.AdvancedFilterOperator.EQUAL
                )
            );
        }

        if (labId != null && labId > 0) {
            params.predicates.add(new AdvancedFilterPredicateItem(
                    Transformers.LABORATORY_ID_FIELD,
                    Long.toString(labId),
                    AdvancedFilterPredicateItem.AdvancedFilterOperator.EQUAL
                )
            );
        }

        return params;
    }
}
