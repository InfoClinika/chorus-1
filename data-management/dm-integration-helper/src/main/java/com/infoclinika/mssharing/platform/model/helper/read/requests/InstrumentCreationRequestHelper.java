package com.infoclinika.mssharing.platform.model.helper.read.requests;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.InstrumentCreationRequestTemplate;
import com.infoclinika.mssharing.platform.model.helper.read.AbstractReaderHelper;
import com.infoclinika.mssharing.platform.model.helper.read.ResultBuilder;
import com.infoclinika.mssharing.platform.model.read.RequestsReaderTemplate.InstrumentCreationRequestInfo;
import com.infoclinika.mssharing.platform.repository.InstrumentCreationRequestRepositoryTemplate;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static com.infoclinika.mssharing.platform.model.helper.read.ResultBuilder.builder;

/**
 * @author Herman Zamula
 */
@Component
@Scope(value = "prototype")
public class InstrumentCreationRequestHelper
        <ENTITY extends InstrumentCreationRequestTemplate, REQUEST extends InstrumentCreationRequestInfo>
        extends AbstractReaderHelper<ENTITY, REQUEST, InstrumentCreationRequestInfo> {

    @Inject
    private InstrumentCreationRequestRepositoryTemplate<ENTITY> instrumentCreationRequestRepository;

    @Override
    public Function<ENTITY, InstrumentCreationRequestInfo> getDefaultTransformer() {
        return new Function<ENTITY, InstrumentCreationRequestInfo>() {
            @Override
            public InstrumentCreationRequestInfo apply(ENTITY input) {
                return new InstrumentCreationRequestInfo(
                        input.getRequester().getEmail(),
                        input.getName(),
                        input.getRequestDate(),
                        input.getId()
                );
            }
        };
    }

    public ResultBuilder<ENTITY, REQUEST> readRequests(long actor) {
        return builder(instrumentCreationRequestRepository.findHeadId(actor), activeTransformer);
    }
}
