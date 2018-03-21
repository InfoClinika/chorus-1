package com.infoclinika.mssharing.model.write;

import com.google.common.base.MoreObjects;
import com.infoclinika.mssharing.model.helper.ExperimentPreparedSampleItem;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author Herman Zamula
 */
public class FileItem extends ExperimentManagementTemplate.FileItemTemplate {

    public final int fractionNumber;
    public final ExperimentPreparedSampleItem preparedSample;

    public FileItem(long id, boolean copy, int fractionNumber, ExperimentPreparedSampleItem preparedSample) {
        super(id, newArrayList(), newArrayList(), copy);
        this.fractionNumber = fractionNumber;
        this.preparedSample = preparedSample;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", this.id)
                .toString();
    }
}
