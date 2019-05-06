package com.infoclinika.mssharing.model.internal.write.ngs.api.dto;

import java.util.LinkedList;
import java.util.List;

/**
 * @author timofei.kasianov 8/2/18
 */
public class NgsExperimentTemplateData {

    private final List<NgsExperimentTemplateSampleData> samples = new LinkedList<>();

    public List<NgsExperimentTemplateSampleData> getSamples() {
        return samples;
    }
}
