package com.infoclinika.mssharing.platform.model.impl.read.adapters;

import com.infoclinika.mssharing.platform.entity.InstrumentModel;
import com.infoclinika.mssharing.platform.model.impl.read.DefaultInstrumentModelsReader;
import com.infoclinika.mssharing.platform.model.read.InstrumentModelReaderTemplate.InstrumentModelLineTemplate;
import org.springframework.stereotype.Component;

/**
 * @author timofei.kasianov 12/8/16
 */
@Component
public class DefaultInstrumentModelsReaderAdapter
    extends DefaultInstrumentModelsReader<InstrumentModel, InstrumentModelLineTemplate> {
    @Override
    public InstrumentModelLineTemplate transform(InstrumentModel instrumentModel) {
        return instrumentModelReaderHelper.getDefaultTransformer().apply(instrumentModel);
    }
}
