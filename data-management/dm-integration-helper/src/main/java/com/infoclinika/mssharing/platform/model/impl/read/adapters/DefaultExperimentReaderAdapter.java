package com.infoclinika.mssharing.platform.model.impl.read.adapters;

import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import com.infoclinika.mssharing.platform.model.impl.read.DefaultExperimentReader;
import com.infoclinika.mssharing.platform.model.read.ExperimentReaderTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Herman Zamula
 */
@Component
public class DefaultExperimentReaderAdapter extends DefaultExperimentReader<ExperimentTemplate, ExperimentReaderTemplate.ExperimentLineTemplate> {
    @Override
    public ExperimentLineTemplate transform(ExperimentTemplate experimentTemplate) {
        return experimentReaderHelper.getDefaultTransformer().apply(experimentTemplate);
    }
}
