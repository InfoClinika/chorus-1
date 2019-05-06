package com.infoclinika.mssharing.model.internal.write.ngs.api;

import com.infoclinika.mssharing.model.internal.write.ngs.api.dto.NgsExperimentTemplateData;
import com.infoclinika.mssharing.model.internal.write.ngs.api.dto.NgsExperimentTemplateValidationResult;

/**
 * @author timofei.kasianov 8/3/18
 */
public interface NgsExperimentTemplateDataValidator {

    NgsExperimentTemplateValidationResult validate(NgsExperimentTemplateData experimentTemplateData);

}
