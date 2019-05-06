package com.infoclinika.mssharing.model.internal.write;

import com.infoclinika.mssharing.model.helper.ExperimentLabelsInfo;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author andrii.loboda
 */
@Transactional
public interface ExperimentLabelToExperimentManagement {
    /*
    * Persists experiment labels, removes old ones**/
    void persistExperimentLabels(long experiment, ExperimentLabelsInfo experimentLabels);

    void copyExperimentLabels(long experimentFrom, long experimentTo);

    void deleteExperimentLabels(long experiment);
}
