/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.internal.entity.restorable;


import com.infoclinika.mssharing.model.internal.entity.*;
import com.infoclinika.mssharing.model.write.AnalysisBounds;
import com.infoclinika.mssharing.model.write.ExperimentCategory;
import com.infoclinika.mssharing.platform.entity.ExperimentType;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentData;
import com.infoclinika.mssharing.platform.entity.restorable.InstrumentRestriction;
import com.infoclinika.mssharing.platform.entity.Species;

import javax.persistence.Entity;
import java.util.Date;
import java.util.List;

/**
 * @author Stanislav Kurilin, Elena Kurilina
 */
@Entity
//@Indexed
public class ActiveExperiment extends AbstractExperiment {

    public ActiveExperiment(
            User creator,
            AbstractProject project,
            Lab lab,
            String name,
            ExperimentData experiment,
            Date creation,
            InstrumentRestriction instrumentRestriction,
            ExperimentType experimentType,
            Species specie,
            AnalysisBounds bounds,
            List<LockMz> lockMasses,
            int mixedSamplesCount,
            int channelsCount,
            String labelType,
            String groupSpecificParametersType,
            double reporterMassTol,
            boolean filterByPIFEnabled,
            double minReporterPIF,
            double minBasePeakRatio,
            double minReporterFraction,
            ExperimentCategory experimentCategory,
            NgsRelatedData ngsRelatedData
    ) {
        super(
                creator,
                project,
                lab,
                name,
                experiment,
                creation,
                instrumentRestriction,
                experimentType,
                specie,
                bounds,
                lockMasses,
                mixedSamplesCount,
                channelsCount,
                labelType,
                groupSpecificParametersType,
                reporterMassTol,
                filterByPIFEnabled,
                minReporterPIF,
                minBasePeakRatio,
                minReporterFraction,
                experimentCategory,
                ngsRelatedData
        );
    }

    public ActiveExperiment() {
    }
}
