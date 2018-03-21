package com.infoclinika.mssharing.model.helper;

import com.google.common.base.Objects;

import java.util.Set;

/**
 * @author andrii.loboda
 */
public class ExperimentPreparedSampleItem {
    public String name;
    public Set<ExperimentSampleItem> samples;

    public ExperimentPreparedSampleItem(String name, Set<ExperimentSampleItem> samples) {
        this.name = name;
        this.samples = samples;
    }

    public ExperimentPreparedSampleItem() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExperimentPreparedSampleItem that = (ExperimentPreparedSampleItem) o;
        return Objects.equal(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
