package com.infoclinika.mssharing.platform.model.helper.read.details;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.InstrumentCreationRequestTemplate;
import com.infoclinika.mssharing.platform.model.helper.read.AbstractReaderHelper;
import com.infoclinika.mssharing.platform.model.helper.read.SingleResultBuilder;
import com.infoclinika.mssharing.platform.model.read.RequestsDetailsReaderTemplate.InstrumentCreationItemTemplate;
import com.infoclinika.mssharing.platform.repository.InstrumentCreationRequestRepositoryTemplate;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static com.infoclinika.mssharing.platform.model.helper.read.SingleResultBuilder.builder;

/**
 * @author Herman Zamula
 */
@Component
@Scope("prototype")
public class InstrumentRequestDetailsReaderHelper<INSTRUMENT_CREATION_ENTITY extends InstrumentCreationRequestTemplate,
        INSTRUMENT_CREATION extends InstrumentCreationItemTemplate>
        extends AbstractReaderHelper<INSTRUMENT_CREATION_ENTITY, INSTRUMENT_CREATION, InstrumentCreationItemTemplate> {

    @Inject
    private DetailsTransformers detailsTransformers;
    @Inject
    private InstrumentCreationRequestRepositoryTemplate<INSTRUMENT_CREATION_ENTITY> instrumentCreationRequestRepository;

    @Override
    public Function<INSTRUMENT_CREATION_ENTITY, InstrumentCreationItemTemplate> getDefaultTransformer() {

        return new Function<INSTRUMENT_CREATION_ENTITY, InstrumentCreationItemTemplate>() {
            @Override
            public InstrumentCreationItemTemplate apply(INSTRUMENT_CREATION_ENTITY input) {

                //noinspection unchecked
                return new InstrumentCreationItemTemplate(
                        input.getId(),
                        input.getName(),
                        input.getSerialNumber(),
                        input.getPeripherals(),
                        input.getLab().getName(),
                        input.getLab().getId(),
                        input.getModel().getId(),
                        input.getModel().getVendor().getId(),
                        detailsTransformers.userItemTransformer().apply(input.getRequester()),
                        input.getRequestDate(),
                        transform(
                                newArrayList(input.getOperators()),
                                detailsTransformers.userItemTransformer()
                        )
                );
            }
        };
    }

    public SingleResultBuilder<INSTRUMENT_CREATION_ENTITY, INSTRUMENT_CREATION> readRequest(long request) {
        return builder(instrumentCreationRequestRepository.findOne(request), activeTransformer);
    }
}
