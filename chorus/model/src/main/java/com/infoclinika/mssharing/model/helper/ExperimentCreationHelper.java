/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.helper;

import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;
import com.infoclinika.mssharing.platform.model.common.items.FileItem;
import com.infoclinika.mssharing.platform.model.helper.ExperimentCreationHelperTemplate;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate.Restriction;
import org.springframework.transaction.annotation.Transactional;

/**
 * Class contains useful methods to get data needed on instrument creation.
 *
 * @author Stanislav Kurilin
 */
@Transactional(readOnly = true)
public interface ExperimentCreationHelper extends ExperimentCreationHelperTemplate {

    ImmutableSet<ExperimentLabelTypeItem> experimentLabelTypes();

    ImmutableSet<ExperimentLabelItem> experimentLabels();

    ImmutableSet<ExperimentLabelItem> experimentLabels(long experimentLabelType);

    boolean hasFilesByModel(final long actor, final long specie, final long model, final Long lab);

    PagedItem<FileItem> availableFilesByInstrumentModel(long actor, long specie, long model, Long lab, PagedItemInfo pagedItemInfo);

    boolean hasFilesByInstrument(final long actor, final long specie, final long instrument);

    PagedItem<FileItem> availableFilesByInstrument(long actor, long specie, long instrument, PagedItemInfo pagedItemInfo);

    Restriction getRestrictionForInstrument(long instrumentId);

    class ExperimentLabelTypeItem {
        public final long id;
        public final String name;
        public final int maxSamples;

        public ExperimentLabelTypeItem(long id, String name, int maxSamples) {
            this.id = id;
            this.name = name;
            this.maxSamples = maxSamples;
        }
    }

    class ExperimentLabelItem {
        public final long id;
        public final String name;
        public final long type;
        public final String aminoAcid;

        public ExperimentLabelItem(long id, String name, long type, String aminoAcid) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.aminoAcid = aminoAcid;
        }
    }
}
