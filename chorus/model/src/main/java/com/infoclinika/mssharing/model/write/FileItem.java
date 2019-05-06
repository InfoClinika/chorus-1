package com.infoclinika.mssharing.model.write;

import com.google.common.base.MoreObjects;
import com.infoclinika.mssharing.model.helper.ExperimentPreparedSampleItem;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author Herman Zamula
 */
public class FileItem extends ExperimentManagementTemplate.FileItemTemplate {
    public String name;
    public final int fractionNumber;
    public final Integer pairedEnd;
    public final ExperimentPreparedSampleItem preparedSample;

    public FileItem(long id, boolean copy, int fractionNumber, ExperimentPreparedSampleItem preparedSample) {
        this(id, copy, fractionNumber, null, preparedSample);
    }

    public FileItem(long id, boolean copy, int fractionNumber, Integer pairedEnd,
                    ExperimentPreparedSampleItem preparedSample) {
        super(id, newArrayList(), newArrayList(), copy);
        this.fractionNumber = fractionNumber;
        this.preparedSample = preparedSample;
        this.pairedEnd = pairedEnd;
    }

    public FileItem(long id, String name, boolean copy, int fractionNumber, Integer pairedEnd,
                    ExperimentPreparedSampleItem preparedSample) {
        this(id, copy, fractionNumber, pairedEnd, preparedSample);

        this.name = name;
    }

    public FileItem(long id, ExperimentPreparedSampleItem preparedSample) {
        super(id, newArrayList(), newArrayList(), false);
        this.fractionNumber = 0;
        this.pairedEnd = null;
        this.preparedSample = preparedSample;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("id", this.id)
            .toString();
    }
}
