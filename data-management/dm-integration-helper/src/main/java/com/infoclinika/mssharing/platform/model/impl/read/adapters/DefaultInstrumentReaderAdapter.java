package com.infoclinika.mssharing.platform.model.impl.read.adapters;

import com.infoclinika.mssharing.platform.entity.InstrumentTemplate;
import com.infoclinika.mssharing.platform.model.impl.read.DefaultAccessedInstrumentReader;
import com.infoclinika.mssharing.platform.model.read.InstrumentReaderTemplate.InstrumentLineTemplate;
import com.infoclinika.mssharing.platform.repository.InstrumentRepositoryTemplate.AccessedInstrument;
import org.springframework.stereotype.Component;

/**
 * @author Herman Zamula
 */
@Component
public class DefaultInstrumentReaderAdapter
    extends DefaultAccessedInstrumentReader<InstrumentTemplate, InstrumentLineTemplate> {
    @Override
    public InstrumentLineTemplate transform(AccessedInstrument<InstrumentTemplate> instrument) {
        return instrumentReaderHelper.getDefaultTransformer().apply(instrument);
    }
}
