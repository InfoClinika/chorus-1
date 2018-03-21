package com.infoclinika.mssharing.platform.model.impl.read;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.platform.entity.InstrumentTemplate;
import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import com.infoclinika.mssharing.platform.model.TransformersTemplate;
import com.infoclinika.mssharing.platform.model.common.items.InstrumentItem;
import com.infoclinika.mssharing.platform.model.helper.read.InstrumentReaderHelper;
import com.infoclinika.mssharing.platform.model.impl.DefaultTransformingTemplate;
import com.infoclinika.mssharing.platform.model.read.InstrumentReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.InstrumentReaderTemplate.InstrumentLineTemplate;
import com.infoclinika.mssharing.platform.repository.InstrumentRepositoryTemplate.AccessedInstrument;
import com.infoclinika.mssharing.platform.repository.UserRepositoryTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Set;
import java.util.SortedSet;

import static com.google.common.collect.FluentIterable.from;
import static com.infoclinika.mssharing.platform.model.helper.PagedItemsTransformerTemplate.toPageRequest;
import static com.infoclinika.mssharing.platform.model.helper.read.PagedResultBuilder.builder;
import static com.infoclinika.mssharing.platform.model.impl.ValidatorPreconditions.checkPresence;

/**
 * @author : Alexander Serebriyan, Herman Zamula
 */
@Transactional(readOnly = true)
public abstract class DefaultAccessedInstrumentReader<INSTRUMENT extends InstrumentTemplate, INSTRUMENT_LINE extends InstrumentLineTemplate> implements InstrumentReaderTemplate<INSTRUMENT_LINE>,
        DefaultTransformingTemplate<AccessedInstrument<INSTRUMENT>, INSTRUMENT_LINE> {

    @Inject
    protected InstrumentReaderHelper<INSTRUMENT, INSTRUMENT_LINE> instrumentReaderHelper;
    @Inject
    protected TransformersTemplate transformers;
    @Inject
    private UserRepositoryTemplate userRepositoryTemplate;

    @PostConstruct
    private void setup() {
        instrumentReaderHelper.setTransformer(new Function<AccessedInstrument<INSTRUMENT>, INSTRUMENT_LINE>() {
            @Override
            public INSTRUMENT_LINE apply(AccessedInstrument<INSTRUMENT> input) {
                return transform(input);
            }
        });
    }

    @Override
    public Set<INSTRUMENT_LINE> readInstruments(long actor) {
        beforeReadInstruments(actor);
        ImmutableSet<AccessedInstrument<INSTRUMENT>> instrumentLineTemplates = onReadInstruments(actor);
        return afterReadInstruments(actor, instrumentLineTemplates);
    }


    protected Set<INSTRUMENT_LINE> afterReadInstruments(long actor, ImmutableSet<AccessedInstrument<INSTRUMENT>> instrumentTemplates) {
        return from(instrumentTemplates).transform(instrumentReaderHelper.getTransformer()).toSet();
    }

    protected ImmutableSet<AccessedInstrument<INSTRUMENT>> onReadInstruments(long actor) {
        return instrumentReaderHelper.readAvailableInstruments(actor).getEntities().toSet();
    }

    protected void beforeReadInstruments(long actor) {
        //noinspection unchecked
        checkPresence(userRepositoryTemplate.findOne(actor));
    }

    @Override
    public Set<INSTRUMENT_LINE> readInstrumentsByLab(long actor, long lab) {
        checkPresence(actor);

        ImmutableSet<AccessedInstrument<INSTRUMENT>> instruments = onReadInstrumentsByLab(actor, lab);

        return afterReadInstrumentsByLab(actor, instruments);
    }

    protected Set<INSTRUMENT_LINE> afterReadInstrumentsByLab(long actor, ImmutableSet<AccessedInstrument<INSTRUMENT>> instruments) {
        return from(instruments).transform(instrumentReaderHelper.getTransformer()).toSet();
    }

    protected ImmutableSet<AccessedInstrument<INSTRUMENT>> onReadInstrumentsByLab(long actor, long lab) {
        return instrumentReaderHelper.readInstrumentLinesByLab(actor, lab).getEntities().toSet();
    }

    @Override
    public PagedItem<INSTRUMENT_LINE> readInstruments(long actor, PagedItemInfo pagedItemInfo) {
        beforeReadInstruments(actor);
        final PageRequest pageRequest = toPageRequest(InstrumentTemplate.class, pagedItemInfo);
        PagedItem<AccessedInstrument<INSTRUMENT>> page = instrumentReaderHelper.readInstruments(actor, pageRequest, pagedItemInfo.toFilterQuery()).getPagedItem();
        return afterReadInstruments(actor, page);
    }

    protected PagedItem<INSTRUMENT_LINE> afterReadInstruments(long actor, PagedItem<AccessedInstrument<INSTRUMENT>> pagedItem) {
        return builder(pagedItem, instrumentReaderHelper.getTransformer()).transform();
    }

    @Override
    public PagedItem<INSTRUMENT_LINE> readInstrumentsByLab(long actor, long lab, PagedItemInfo pagedItemInfo) {
        beforeReadInstrumentsByLab(actor);
        final PageRequest pageRequest = toPageRequest(InstrumentTemplate.class, pagedItemInfo);
        PagedItem<AccessedInstrument<INSTRUMENT>> page = instrumentReaderHelper.readInstrumentsByLab(actor, lab, pageRequest, pagedItemInfo.toFilterQuery()).getPagedItem();
        return afterReadInstrumentsByLab(actor, lab, page);
    }

    @Override
    public SortedSet<InstrumentItem> readInstrumentsWhereUserIsOperator(long actor) {

        //noinspection unchecked
        return instrumentReaderHelper.readOperatedInstruments(actor)
                .transform(new Function<AccessedInstrument<INSTRUMENT>, InstrumentItem>() {
                    @Override
                    public InstrumentItem apply(AccessedInstrument<INSTRUMENT> input) {
                        return transformers.instrumentItemTransformer().apply(input.instrument);
                    }
                })
                .toSortedSet(transformers.instrumentItemComparator());
    }

    private void beforeReadInstrumentsByLab(long actor) {
        //noinspection unchecked
        checkPresence(userRepositoryTemplate.findOne(actor));
    }

    protected PagedItem<INSTRUMENT_LINE> afterReadInstrumentsByLab(long actor, long lab, PagedItem<AccessedInstrument<INSTRUMENT>> pagedItem) {
        return builder(pagedItem, instrumentReaderHelper.getTransformer()).transform();
    }
}
