package com.infoclinika.mssharing.model.internal.write.ngs.api;

import com.infoclinika.mssharing.model.internal.write.ngs.api.dto.NgsExperimentTemplateData;

/**
 * @author timofei.kasianov 8/3/18
 */
public interface NgsExperimentTemplateParser {

    NgsExperimentTemplateData parse(byte[] ngsExperimentTemplate);

}
