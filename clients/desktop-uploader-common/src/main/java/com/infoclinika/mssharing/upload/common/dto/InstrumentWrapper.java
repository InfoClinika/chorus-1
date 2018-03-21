package com.infoclinika.mssharing.upload.common.dto;

import com.infoclinika.mssharing.dto.response.InstrumentDTO;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author timofey.kasyanov
 *         date:   28.01.14
 */
public class InstrumentWrapper {
    private final InstrumentDTO instrument;

    public InstrumentWrapper(InstrumentDTO instrument) {
        checkNotNull(instrument);
        this.instrument = instrument;
    }

    public InstrumentDTO getInstrument() {
        return instrument;
    }

    @Override
    public String toString(){
        return instrument.getName();
    }

}
