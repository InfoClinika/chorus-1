package com.infoclinika.mssharing.model.internal.read;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.infoclinika.mssharing.model.internal.entity.Instrument;
import com.infoclinika.mssharing.model.internal.helper.InstrumentsDefaults;
import com.infoclinika.mssharing.model.read.InstrumentLine;
import com.infoclinika.mssharing.model.read.InstrumentReader;
import com.infoclinika.mssharing.platform.model.common.items.InstrumentItem;
import com.infoclinika.mssharing.platform.model.common.items.VendorItem;
import com.infoclinika.mssharing.platform.model.helper.read.ResultBuilder;
import com.infoclinika.mssharing.platform.model.impl.read.DefaultAccessedInstrumentReader;
import com.infoclinika.mssharing.platform.repository.InstrumentRepositoryTemplate.AccessedInstrument;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.SortedSet;

/**
 * @author : Alexander Serebriyan
 */
@Component
@Transactional(readOnly = true)
public class InstrumentReaderImpl extends DefaultAccessedInstrumentReader<Instrument, InstrumentLine> implements InstrumentReader {

    @Override
    public InstrumentLine transform(AccessedInstrument<Instrument> accessed) {
        return new InstrumentLine(instrumentReaderHelper.getDefaultTransformer().apply(accessed));
    }

    @Override
    public SortedSet<? extends InstrumentItem> readInstrumentItemsWhereUserIsOperator(long actor) {
        //noinspection unchecked
        return instrumentReaderHelper.readOperatedInstruments(actor)
                .transform(new Function<AccessedInstrument<Instrument>, InstrumentItem>() {
                    @Override
                    public InstrumentItem apply(AccessedInstrument<Instrument> input) {
                        return transformers.instrumentItemTransformer().apply(input.instrument);
                    }
                })
                .toSortedSet(transformers.instrumentItemComparator());
    }

    @Override
    public List<InstrumentLine> findByInstrumentModel(long actor, long instrumentModel) {
        final FluentIterable<AccessedInstrument<Instrument>> entities = instrumentReaderHelper.readOperatedInstruments(actor).getEntities();
        return entities
                .filter(input -> input.instrument.getModel().getId().equals(instrumentModel))
                .transform(input -> new InstrumentLine(instrumentReaderHelper.getDefaultTransformer().apply(input)))
                .toList();
    }

    @Override
    public Optional<InstrumentLine> readDefaultInstrument(long actor, long lab, long instrumentModel) {
        final ResultBuilder<AccessedInstrument<Instrument>, InstrumentLine> instrumentsResult =
                instrumentReaderHelper.readInstrumentLinesByLabModelAndName(actor, lab, instrumentModel, InstrumentsDefaults.DEFAULT_INSTRUMENT_NAME);
        final FluentIterable<InstrumentLine> instruments = instrumentsResult.transform();
        return instruments.size() > 0 ?
                Optional.of(instruments.get(0)) :
                Optional.empty();

    }

}
