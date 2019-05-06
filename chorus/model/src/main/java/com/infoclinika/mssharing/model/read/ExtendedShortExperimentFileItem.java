package com.infoclinika.mssharing.model.read;

import com.google.common.collect.ImmutableList;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.AnnotationItem;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.ConditionItem;

import java.util.Collection;
import java.util.Objects;

/**
 * @author andrii.loboda
 */
public class ExtendedShortExperimentFileItem extends DetailsReaderTemplate.ShortExperimentFileItem {
    public final ImmutableList<ExperimentShortSampleItem> samples;

    public ExtendedShortExperimentFileItem(long id, String name, ImmutableList<ExperimentShortSampleItem> samples) {
        super(id, name, ImmutableList.<ConditionItem>of(), ImmutableList.<AnnotationItem>of());
        this.samples = samples;
    }

    public static class ExperimentShortSampleItem {
        public final long id;
        public final String name;
        public final String type;
        public final ConditionItem condition;
        public final Collection<AnnotationItem> annotations;

        public ExperimentShortSampleItem(long id, String name, String type, ConditionItem condition,
                                         Collection<AnnotationItem> annotations) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.condition = condition;
            this.annotations = annotations;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ExperimentShortSampleItem that = (ExperimentShortSampleItem) o;
            return id == that.id &&
                Objects.equals(name, that.name) &&
                Objects.equals(type, that.type) &&
                Objects.equals(condition, that.condition) &&
                Objects.equals(annotations, that.annotations);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, type, condition, annotations);
        }
    }
}
