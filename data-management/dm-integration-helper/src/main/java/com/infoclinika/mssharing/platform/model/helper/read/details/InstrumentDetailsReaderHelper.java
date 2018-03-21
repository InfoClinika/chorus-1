package com.infoclinika.mssharing.platform.model.helper.read.details;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.InstrumentTemplate;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.model.EntityFactories;
import com.infoclinika.mssharing.platform.model.helper.read.AbstractReaderHelper;
import com.infoclinika.mssharing.platform.model.helper.read.SingleResultBuilder;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.InstrumentAccess;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.InstrumentItemTemplate;
import com.infoclinika.mssharing.platform.repository.InstrumentRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.InstrumentRepositoryTemplate.AccessedInstrument;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static com.google.common.collect.FluentIterable.from;
import static com.infoclinika.mssharing.platform.model.helper.read.SingleResultBuilder.builder;

/**
 * @author Herman Zamula
 */
@Component
@Scope(value = "prototype")
public class InstrumentDetailsReaderHelper<INSTRUMENT extends InstrumentTemplate, ITEM extends InstrumentItemTemplate>
        extends AbstractReaderHelper<AccessedInstrument<INSTRUMENT>, ITEM, InstrumentItemTemplate> {

    @Inject
    private InstrumentRepositoryTemplate<INSTRUMENT> instrumentRepository;
    @Inject
    private DetailsTransformersTemplate detailsTransformers;
    @Inject
    private EntityFactories entityFactories;

    @Override
    public Function<AccessedInstrument<INSTRUMENT>, InstrumentItemTemplate> getDefaultTransformer() {
        return new Function<AccessedInstrument<INSTRUMENT>, InstrumentItemTemplate>() {
            @Override
            public InstrumentItemTemplate apply(AccessedInstrument<INSTRUMENT> accessedInstrument) {

                final INSTRUMENT instrument = accessedInstrument.instrument;

                final String vendor = instrument.getModel().getVendor().getName();
                final String studyType = instrument.getModel().getStudyType().getName();
                final String type = instrument.getModel().getType().getName();

                final UserTemplate user = entityFactories.userFromId.apply(accessedInstrument.accessedUser);
                //noinspection unchecked
                final InstrumentAccess access = instrument.isOperator(user) ? InstrumentAccess.OPERATOR
                        : instrument.isPending(user) ? InstrumentAccess.PENDING
                        : InstrumentAccess.NO_ACCESS;

                //noinspection unchecked
                return new InstrumentItemTemplate(
                        instrument.getId(),
                        instrument.getName(),
                        vendor,
                        instrument.getModel().getName(),
                        instrument.getModel().getId(),
                        instrument.getSerialNumber(),
                        instrument.getCreator().getEmail(),
                        instrument.getPeripherals(),
                        from(instrument.getOperators())
                                .transform(detailsTransformers.sharedPersonTransformer())
                                .toSortedSet(detailsTransformers.namedItemComparator()),
                        detailsTransformers.labItemTransformer().apply(instrument.getLab()),
                        type,
                        access, studyType);
            }
        };
    }

    public SingleResultBuilder<AccessedInstrument<INSTRUMENT>, ITEM> readInstrument(long actor, long id) {
        final AccessedInstrument<INSTRUMENT> instrument = instrumentRepository.findOneAccessed(actor, id);
        return builder(instrument, activeTransformer);
    }

}
