package com.infoclinika.mssharing.platform.model.impl.read.adapters;

import com.infoclinika.mssharing.platform.entity.InstrumentCreationRequestTemplate;
import com.infoclinika.mssharing.platform.entity.LabCreationRequestTemplate;
import com.infoclinika.mssharing.platform.model.impl.read.DefaultRequestsDetailsReader;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.LabItemTemplate;
import com.infoclinika.mssharing.platform.model.read.RequestsDetailsReaderTemplate.InstrumentCreationItemTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Herman Zamula
 */
@Component
public class DefaultRequestsDetailsReaderAdapter

        extends DefaultRequestsDetailsReader<InstrumentCreationRequestTemplate, LabCreationRequestTemplate, InstrumentCreationItemTemplate, LabItemTemplate> {

    @Override
    protected InstrumentCreationItemTemplate transformInstrumentCreationRequest(InstrumentCreationRequestTemplate instrumentCreationRequest) {
        return instrumentRequestDetailsHelper.getDefaultTransformer().apply(instrumentCreationRequest);
    }

    @Override
    protected LabItemTemplate transformLabCreationRequest(LabCreationRequestTemplate labCreationRequest) {
        return labRequestDetailsHelper.getDefaultTransformer().apply(labCreationRequest);
    }
}
