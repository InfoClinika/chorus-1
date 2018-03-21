package com.infoclinika.mssharing.platform.model.testing.helper;

import com.infoclinika.mssharing.platform.model.read.LabReaderTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Herman Zamula
 */
@Component
class ReadServices {
    @Inject
    public LabReaderTemplate labReader;
}
