package com.infoclinika.mssharing.model.read.dto.details;

import java.util.List;

/**
 * @author andrii.loboda
 */
public class ExperimentLabelsItem {
    public final List<Long> lightLabels;
    public final List<Long> mediumLabels;
    public final List<Long> heavyLabels;
    public final List<Long> specialLabels;

    public ExperimentLabelsItem(
            List<Long> lightLabels,
            List<Long> mediumLabels,
            List<Long> heavyLabels,
            List<Long> specialLabels
    ) {
        this.lightLabels = lightLabels;
        this.mediumLabels = mediumLabels;
        this.heavyLabels = heavyLabels;
        this.specialLabels = specialLabels;
    }
}
