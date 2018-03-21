package com.infoclinika.mssharing.model.helper;

import com.google.common.base.Objects;

import java.util.List;

/**
 * @author andrii.loboda
 */
public class ExperimentSampleItem {
    public String name;
    public ExperimentSampleTypeItem type;
    public List<String> factorValues;

    public ExperimentSampleItem(String name, ExperimentSampleTypeItem type, List<String> factorValues) {
        this.name = name;
        this.type = type;
        this.factorValues = factorValues;
    }

    public ExperimentSampleItem() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExperimentSampleItem that = (ExperimentSampleItem) o;
        return Objects.equal(name, that.name) &&
                Objects.equal(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, type);
    }
}
