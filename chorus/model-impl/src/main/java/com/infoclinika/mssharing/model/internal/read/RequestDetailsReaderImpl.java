package com.infoclinika.mssharing.model.internal.read;

import com.infoclinika.mssharing.model.internal.entity.InstrumentCreationRequest;
import com.infoclinika.mssharing.model.read.DetailsReader.InstrumentCreationItem;
import com.infoclinika.mssharing.platform.entity.LabCreationRequestTemplate;
import com.infoclinika.mssharing.platform.model.impl.read.DefaultRequestsDetailsReader;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.LabItemTemplate;
import com.infoclinika.mssharing.platform.model.read.RequestsDetailsReaderTemplate;
import org.springframework.stereotype.Component;

import static com.google.common.collect.FluentIterable.from;
import static com.infoclinika.mssharing.model.internal.read.Transformers.LOCK_MZ_ITEM_FUNCTION;

/**
 * @author Herman Zamula
 */
@Component
public class RequestDetailsReaderImpl extends DefaultRequestsDetailsReader<InstrumentCreationRequest, LabCreationRequestTemplate, InstrumentCreationItem, LabItemTemplate>

        implements RequestsDetailsReaderTemplate<InstrumentCreationItem, LabItemTemplate> {

    @Override
    protected InstrumentCreationItem transformInstrumentCreationRequest(InstrumentCreationRequest instrumentCreationRequest) {

        final InstrumentCreationItemTemplate template = instrumentRequestDetailsHelper.getDefaultTransformer().apply(instrumentCreationRequest);

        return new InstrumentCreationItem(
                template,
                instrumentCreationRequest.getHplc(),
                from(instrumentCreationRequest.getLockMasses()).transform(LOCK_MZ_ITEM_FUNCTION).toList()
        );

    }

    @Override
    protected LabItemTemplate transformLabCreationRequest(LabCreationRequestTemplate labCreationRequest) {
        return labRequestDetailsHelper.getDefaultTransformer().apply(labCreationRequest);
    }
}
