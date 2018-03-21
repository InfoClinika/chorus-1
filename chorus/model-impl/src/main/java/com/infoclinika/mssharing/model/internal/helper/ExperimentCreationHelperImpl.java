/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.internal.helper;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.model.PaginationItems;
import com.infoclinika.mssharing.model.helper.ExperimentCreationHelper;
import com.infoclinika.mssharing.model.internal.entity.ExperimentLabel;
import com.infoclinika.mssharing.model.internal.entity.ExperimentLabelType;
import com.infoclinika.mssharing.model.internal.entity.Instrument;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.read.AdvancedFilterQueryReader;
import com.infoclinika.mssharing.model.internal.repository.ExperimentLabelRepository;
import com.infoclinika.mssharing.model.internal.repository.ExperimentLabelTypeRepository;
import com.infoclinika.mssharing.model.internal.repository.FileMetaDataRepository;
import com.infoclinika.mssharing.model.internal.repository.InstrumentRepository;
import com.infoclinika.mssharing.platform.entity.InstrumentModel;
import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import com.infoclinika.mssharing.platform.model.TransformersTemplate;
import com.infoclinika.mssharing.platform.model.common.items.FileItem;
import com.infoclinika.mssharing.platform.model.helper.PagedItemsTransformerTemplate;
import com.infoclinika.mssharing.platform.model.impl.helper.adapters.DefaultExperimentCreationHelperAdapter;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate.Restriction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.FluentIterable.from;
import static com.infoclinika.mssharing.model.internal.read.AdvancedFilterCreationHelper.getAdvancedFilterQueryStringWithCondition;
import static com.infoclinika.mssharing.model.internal.read.AdvancedFilterCreationHelper.getOrderingString;
import static org.springframework.data.domain.Sort.Direction.ASC;

/**
 * @author Stanislav Kurilin
 */
@Service
public class ExperimentCreationHelperImpl extends DefaultExperimentCreationHelperAdapter implements ExperimentCreationHelper {

    public static final String NAME_FIELD = "name";
    public static final String ID_FIELD = "id";
    @Inject
    private ExperimentLabelRepository experimentLabelRepository;
    @Inject
    private ExperimentLabelTypeRepository experimentLabelTypeRepository;

    @Inject
    private FileMetaDataRepository fileMetaDataRepository;

    @Inject
    private PagedItemsTransformerTemplate pagedItemsTransformer;

    @Inject
    private TransformersTemplate transformers;

    @Inject
    private AdvancedFilterQueryReader advancedFilterQueryReader;

    @Inject
    private InstrumentRepository instrumentRepository;

    @Override
    public ImmutableSet<ExperimentLabelItem> experimentLabels() {
        return from(experimentLabelRepository.findAll()).transform(new Function<ExperimentLabel, ExperimentLabelItem>() {
            @Override
            public ExperimentLabelItem apply(ExperimentLabel label) {
                return new ExperimentLabelItem(label.getId(), label.getName(), label.getType().getId(), label.getAcid());
            }
        }).toSet();
    }

    @Override
    public ImmutableSet<ExperimentLabelItem> experimentLabels(long experimentLabelType) {
        final ExperimentLabelType labelType = experimentLabelTypeRepository.findOne(experimentLabelType);
        return from(experimentLabelRepository.findByType(labelType)).transform(
                label -> new ExperimentLabelItem(label.getId(), label.getName(), label.getType().getId(), label.getAcid())
        ).toSet();
    }


    @Override
    public ImmutableSet<ExperimentLabelTypeItem> experimentLabelTypes() {
        return from(experimentLabelTypeRepository.findAll()).transform(new Function<ExperimentLabelType, ExperimentLabelTypeItem>() {
            @Override
            public ExperimentLabelTypeItem apply(ExperimentLabelType type) {
                return new ExperimentLabelTypeItem(type.getId(), type.getName(), type.getMaxSamples());
            }
        }).toSet();
    }

    private PaginationItems.PagedItemInfo createEmptyPagedItemInfo() {
        return new PaginationItems.PagedItemInfo(1, 0, ID_FIELD, false, "", Optional.absent());
    }

    @Override
    public boolean hasFilesByModel(final long actor, final long specie, final long model, final Long lab) {
        final PagedItem<FileItem> fileItems = availableFilesByInstrumentModel(actor, specie, model, lab, createEmptyPagedItemInfo());
        return !fileItems.items.isEmpty();
    }

    @Override
    public PagedItem<FileItem> availableFilesByInstrumentModel(long actor, long specie, long model, Long lab, PagedItemInfo pagedItemInfo) {

        PaginationItems.PagedItemInfo pagedInfo = (PaginationItems.PagedItemInfo) pagedItemInfo;

        final PageRequest pageRequest = pagedItemsTransformer.toPageRequest(ActiveFileMetaData.class, pagedItemInfo);

        final List<Sort.Order> orders = Arrays.asList(new Sort.Order(ASC, NAME_FIELD), new Sort.Order(ASC, ID_FIELD));
        final PageRequest pageRequestWithSorting = new PageRequest(pageRequest.getPageNumber(), pageRequest.getPageSize(),
                pageRequest.getSort().and(new Sort(orders)));

        final Page<ActiveFileMetaData> files;

        if (!pagedInfo.advancedFilter.isPresent()) {
            files = fileMetaDataRepository.availableFilesByInstrumentModel(actor, model, specie, lab == null ? 0 : lab, pageRequestWithSorting);
        } else {
            final String predicatesQueryString = getAdvancedFilterQueryStringWithCondition(ActiveFileMetaData.class, pagedInfo);
            final String orderingString = getOrderingString(ActiveFileMetaData.class, pageRequestWithSorting);

            final Map<String, Object> parameters = new HashMap<String, Object>() {{
                put("user", actor);
                put("model", model);
                put("specie", specie);
                put("lab", lab == null ? 0 : lab);
            }};

            files = advancedFilterQueryReader.readQuery(pageRequestWithSorting,
                    FileMetaDataRepository.SELECT_AVAILABLE_FILES_BY_INSTRUMENT_MODEL_WITH_ADVANCED_FILTER + predicatesQueryString + orderingString,
                    FileMetaDataRepository.COUNT_AVAILABLE_FILES_BY_INSTRUMENT_MODEL_WITH_ADVANCED_FILTER + predicatesQueryString + orderingString,
                    parameters);
        }

        return new PagedItem<>(files.getTotalPages(),
                files.getTotalElements(),
                files.getNumber(),
                files.getNumberOfElements(),
                from(files).transform(transformers.fileTransformer()).toList());
    }

    @Override
    public boolean hasFilesByInstrument(final long actor, final long specie, final long instrument) {
        final PagedItem<FileItem> fileItems = availableFilesByInstrument(actor, specie, instrument, createEmptyPagedItemInfo());
        return !fileItems.items.isEmpty();
    }

    @Override
    public PagedItem<FileItem> availableFilesByInstrument(long actor, long specie, long instrument, PagedItemInfo pagedItemInfo) {

        PaginationItems.PagedItemInfo pagedInfo = (PaginationItems.PagedItemInfo) pagedItemInfo;

        final PageRequest pageRequest = pagedItemsTransformer.toPageRequest(ActiveFileMetaData.class, pagedItemInfo);

        final List<Sort.Order> orders = Arrays.asList(new Sort.Order(ASC, NAME_FIELD), new Sort.Order(ASC, ID_FIELD));
        final PageRequest pageRequestWithSorting = new PageRequest(pageRequest.getPageNumber(), pageRequest.getPageSize(),
                pageRequest.getSort().and(new Sort(orders)));

        final Page<ActiveFileMetaData> files;

        if (!pagedInfo.advancedFilter.isPresent()) {
            files = fileMetaDataRepository.availableFilesByInstrumentAndSpecie(actor, specie, instrument, pageRequestWithSorting);
        } else {
            final String predicatesQueryString = getAdvancedFilterQueryStringWithCondition(ActiveFileMetaData.class, pagedInfo);
            final String orderingString = getOrderingString(ActiveFileMetaData.class, pageRequestWithSorting);

            final Map<String, Object> parameters = new HashMap<String, Object>() {{
                put("user", actor);
                put("specie", specie);
                put("instrument", instrument);
            }};

            files = advancedFilterQueryReader.readQuery(pageRequestWithSorting,
                    FileMetaDataRepository.SELECT_AVAILABLE_FILES_BY_INSTRUMENT_AND_SPECIE_WITH_ADVANCED_FILTER + predicatesQueryString + orderingString,
                    FileMetaDataRepository.COUNT_AVAILABLE_FILES_BY_INSTRUMENT_AND_SPECIE_WITH_ADVANCED_FILTER + predicatesQueryString + orderingString,
                    parameters);
        }

        return new PagedItem<>(files.getTotalPages(),
                files.getTotalElements(),
                files.getNumber(),
                files.getNumberOfElements(),
                from(files).transform(transformers.fileTransformer()).toList());
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
}
