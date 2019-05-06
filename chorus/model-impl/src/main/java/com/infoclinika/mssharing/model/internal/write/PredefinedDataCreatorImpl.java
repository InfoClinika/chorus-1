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
package com.infoclinika.mssharing.model.internal.write;

import com.google.common.base.Function;
import com.infoclinika.mssharing.model.PredefinedDataCreator;
import com.infoclinika.mssharing.model.helper.ColumnViewHelper;
import com.infoclinika.mssharing.model.internal.entity.ColumnDefinition;
import com.infoclinika.mssharing.model.internal.entity.ColumnsView;
import com.infoclinika.mssharing.model.internal.entity.ViewColumn;
import com.infoclinika.mssharing.model.internal.entity.payment.BillingProperty;
import com.infoclinika.mssharing.model.internal.repository.*;
import com.infoclinika.mssharing.model.write.InstrumentManagement;
import com.infoclinika.mssharing.platform.entity.InstrumentModel;
import com.infoclinika.mssharing.platform.entity.InstrumentStudyType;
import com.infoclinika.mssharing.platform.entity.Vendor;
import com.infoclinika.mssharing.platform.model.impl.DefaultPredefinedDataCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;

/**
 * @author Stanislav Kurilin
 */
@Component
public class PredefinedDataCreatorImpl extends DefaultPredefinedDataCreator implements PredefinedDataCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(PredefinedDataCreatorImpl.class);

    @Inject
    ColumnDefinitionRepository columnDefinitionRepo;

    @Inject
    private ColumnViewRepository columnViewRepository;

    @Inject
    private BillingPropertyRepository billingPropertyRepository;

    @Inject
    private ExperimentLabelManagement experimentLabelManagement;

    @Inject
    private InstrumentManagement instrumentManagement;

    @Override
    public List<Long> createColumnsDefinitions(List<ColumnViewHelper.Column> info,
                                               ColumnViewHelper.ColumnViewType type) {
        List<Long> ids = newArrayList();
        final ColumnsView.Type columnViewType = asColumnViewType(type);
        for (ColumnViewHelper.Column i : info) {
            final Long id = columnDefinitionRepo.save(new ColumnDefinition(i.name,
                columnViewType,
                i.dataType,
                i.sortable,
                i.hideable,
                i.units
            )).getId();
            ids.add(id);
        }
        return ids;
    }


    @Override
    public long defaultColumnsView(Set<ColumnViewHelper.ColumnInfo> view, ColumnViewHelper.ColumnViewType type) {
        final ColumnsView.Type columnViewType = asColumnViewType(type);
        final ColumnsView columnsView = new ColumnsView("Default", columnViewType, null);
        columnsView.setDefault(true);
        columnsView.getColumns().addAll(from(view).transform(new Function<ColumnViewHelper.ColumnInfo, ViewColumn>() {
            @Override
            public ViewColumn apply(ColumnViewHelper.ColumnInfo input) {
                final ColumnDefinition columnDefinition = columnDefinitionRepo.findOne(input.originalColumn);
                return new ViewColumn(columnDefinition, input.order);
            }
        }).toSet());
        return columnViewRepository.save(columnsView).getId();
    }

    @Override
    public long createExperimentLabelType(String name, int maxSamples) {
        final ExperimentLabelManagement.ExperimentTypeInfo typeToCreate
            = new ExperimentLabelManagement.ExperimentTypeInfo(name, maxSamples);

        return experimentLabelManagement.createLabelType(typeToCreate);
    }

    @Override
    public long createExperimentLabel(long experimentLabelType, String aminoAcid, String name) {
        final ExperimentLabelManagement.ExperimentLabelInfo labelToCreate =
            new ExperimentLabelManagement.ExperimentLabelInfo(aminoAcid, name, experimentLabelType);

        return experimentLabelManagement.createLabel(labelToCreate);
    }

    private static ColumnsView.Type asColumnViewType(ColumnViewHelper.ColumnViewType type) {
        switch (type) {
            case EXPERIMENT:
                return ColumnsView.Type.EXPERIMENT_META;
            case PROJECT:
                return ColumnsView.Type.PROJECT_META;
            case FILE:
                return ColumnsView.Type.FILE_META;
            default:
                throw new IllegalArgumentException("Can't convert unknown type: " + type);
        }
    }

    @Override
    public void billingProperties() {
        billingPropertyRepository.save(new BillingProperty(
            BillingProperty.BillingPropertyName.ENTERPRISE_ACCOUNT_STORAGE_VOLUME_COST,
            Long.toString(4000L)
        ));
        billingPropertyRepository.save(new BillingProperty(
            BillingProperty.BillingPropertyName.ENTERPRISE_ACCOUNT_STORAGE_VOLUME_SIZE,
            Long.toString(536870912000L)
        ));
        billingPropertyRepository.save(new BillingProperty(
            BillingProperty.BillingPropertyName.FREE_ACCOUNT_STORAGE_LIMIT,
            Long.toString(107374182400L)
        ));
        billingPropertyRepository.save(new BillingProperty(
            BillingProperty.BillingPropertyName.PROCESSING_FEATURE_COST,
            Long.toString(20000L)
        ));
        billingPropertyRepository.save(new BillingProperty(
            BillingProperty.BillingPropertyName.FREE_ACCOUNT_ARCHIVE_STORAGE_LIMIT,
            Long.toString(0L)
        ));
        billingPropertyRepository.save(new BillingProperty(
            BillingProperty.BillingPropertyName.ENTERPRISE_ACCOUNT_ARCHIVE_STORAGE_VOLUME_COST,
            Long.toString(4000L)
        ));
        billingPropertyRepository.save(new BillingProperty(
            BillingProperty.BillingPropertyName.ENTERPRISE_ACCOUNT_ARCHIVE_STORAGE_VOLUME_SIZE,
            Long.toString(536870912000L)
        ));
    }

    @Override
    public long createInstrument(long creator, long labId, String studyTypeName, String vendorName) {
        final InstrumentStudyType studyType = instrumentStudyTypeRepository.findByName(studyTypeName);
        final Vendor vendor = vendorRepository.findByName(vendorName);
        final InstrumentModel model =
            instrumentModelRepository.findByStudyTypeAndVendor(studyType.getId(), vendor.getId()).get(0);
        return instrumentManagement.createDefaultInstrument(creator, labId, model.getId());
    }

    @Override
    public long createInstrumentWithModel(long creator, long labId, String vendorName, String modelName) {
        final InstrumentModel model = instrumentModelRepository.findByNameAndVendorName(modelName, vendorName);
        return instrumentManagement.createDefaultInstrument(creator, labId, model.getId(), "Default " + modelName);
    }
}
