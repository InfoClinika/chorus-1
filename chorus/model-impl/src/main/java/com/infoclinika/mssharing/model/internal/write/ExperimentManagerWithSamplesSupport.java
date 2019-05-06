package com.infoclinika.mssharing.model.internal.write;

/**
 * @author andrii.loboda
 */
public interface ExperimentManagerWithSamplesSupport<EXPERIMENT_INFO, EXPERIMENT> {
    void updateExperimentFilesWithFactorsAndSamples(EXPERIMENT_INFO info, EXPERIMENT experiment);
}
