package com.infoclinika.mssharing.platform.model.impl.read.adapters;

import com.infoclinika.mssharing.platform.entity.LabTemplate;
import com.infoclinika.mssharing.platform.model.impl.read.DefaultLabReader;
import com.infoclinika.mssharing.platform.model.read.LabReaderTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Herman Zamula
 */
@Component
public class DefaultLabReaderAdapter extends DefaultLabReader<LabTemplate, LabReaderTemplate.LabLineTemplate> {

    @Override
    public LabReaderTemplate.LabLineTemplate transform(LabTemplate labTemplate) {
        return labReaderHelper.getDefaultTransformer().apply(labTemplate);
    }
}
