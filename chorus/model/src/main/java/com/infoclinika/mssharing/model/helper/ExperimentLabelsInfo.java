package com.infoclinika.mssharing.model.helper;

import com.google.common.base.MoreObjects;

import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;

/**
 * @author andrii.loboda
 */
public class ExperimentLabelsInfo {
    public List<Long> lightLabels = newLinkedList();
    public List<Long> mediumLabels = newLinkedList();
    public List<Long> heavyLabels = newLinkedList();
    public List<Long> specialLabels = newLinkedList();

    public ExperimentLabelsInfo(List<Long> lightLabels, List<Long> mediumLabels, List<Long> heavyLabels) {
        this.lightLabels.addAll(lightLabels);
        this.mediumLabels.addAll(mediumLabels);
        this.heavyLabels.addAll(heavyLabels);
    }

    public ExperimentLabelsInfo(List<Long> lightLabels, List<Long> mediumLabels, List<Long> heavyLabels,
                                List<Long> specialLabels) {
        this.lightLabels.addAll(lightLabels);
        this.mediumLabels.addAll(mediumLabels);
        this.heavyLabels.addAll(heavyLabels);
        this.specialLabels.addAll(specialLabels);
    }

    /*automatically generated getters, setters, default package constructor*/
    public ExperimentLabelsInfo() {
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("lightLabels", lightLabels)
            .add("mediumLabels", mediumLabels)
            .add("heavyLabels", heavyLabels)
            .add("specialLabels", specialLabels)
            .toString();
    }
}
