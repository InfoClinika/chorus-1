package com.infoclinika.mssharing.model.write;

/**
 * Experiment Category specifies to which kingdom of experiments it refers to.
 * Different rules for experiments with different categories could be applied.
 *
 * @author Andrii Loboda
 */
public enum ExperimentCategory {
    /**
     * Proteomics category implies that experiment is made based on mass-spectrometric data
     * and it serves to study proteomes and their functions.
     */
    PROTEOMICS,
    /*Experiments with this category work with genes, not proteomes.
     * */
    MICROARRAY,
    /*Experiments with this category work with genes
     * */
    NGS
}
