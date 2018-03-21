package com.infoclinika.mssharing.model.read;

import com.google.common.collect.ImmutableList;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.AnnotationItem;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.ConditionItem;

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

        public ExperimentShortSampleItem(long id, String name, String type, ConditionItem condition) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.condition = condition;
        }
    }
}
