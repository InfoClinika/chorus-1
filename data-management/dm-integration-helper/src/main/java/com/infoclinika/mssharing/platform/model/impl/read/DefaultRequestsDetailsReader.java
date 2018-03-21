package com.infoclinika.mssharing.platform.model.impl.read;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.InstrumentCreationRequestTemplate;
import com.infoclinika.mssharing.platform.entity.LabCreationRequestTemplate;
import com.infoclinika.mssharing.platform.model.RuleValidator;
import com.infoclinika.mssharing.platform.model.helper.read.SingleResultBuilder;
import com.infoclinika.mssharing.platform.model.helper.read.details.InstrumentRequestDetailsReaderHelper;
import com.infoclinika.mssharing.platform.model.helper.read.details.LabRequestDetailsReaderHelper;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.RequestsDetailsReaderTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import static com.infoclinika.mssharing.platform.model.impl.ValidatorPreconditions.checkAccess;

/**
 * @author Herman Zamula
 */
@Transactional(readOnly = true)
public abstract class DefaultRequestsDetailsReader<
        INSTRUMENT_CREATION_ENTITY extends InstrumentCreationRequestTemplate,
        LAB_CREATION_ENTITY extends LabCreationRequestTemplate,
        INSTRUMENT_CREATION extends RequestsDetailsReaderTemplate.InstrumentCreationItemTemplate,
        LAB_CREATION extends DetailsReaderTemplate.LabItemTemplate> implements RequestsDetailsReaderTemplate<INSTRUMENT_CREATION, LAB_CREATION> {

    @Inject
    protected InstrumentRequestDetailsReaderHelper<INSTRUMENT_CREATION_ENTITY, INSTRUMENT_CREATION> instrumentRequestDetailsHelper;
    @Inject
    protected RuleValidator ruleValidator;
    @Inject
    protected LabRequestDetailsReaderHelper<LAB_CREATION_ENTITY, LAB_CREATION> labRequestDetailsHelper;

    @PostConstruct
    public void setup() {
        instrumentRequestDetailsHelper.setTransformer(new Function<INSTRUMENT_CREATION_ENTITY, INSTRUMENT_CREATION>() {
            @Override
            public INSTRUMENT_CREATION apply(INSTRUMENT_CREATION_ENTITY input) {
                return transformInstrumentCreationRequest(input);
            }
        });
        labRequestDetailsHelper.setTransformer(new Function<LAB_CREATION_ENTITY, LAB_CREATION>() {
            @Override
            public LAB_CREATION apply(LAB_CREATION_ENTITY input) {
                return transformLabCreationRequest(input);
            }
        });
    }

    @Override
    public INSTRUMENT_CREATION readInstrumentCreation(long actor, long request) {

        beforeReadInstrumentCreationRequest(actor, request);
        final SingleResultBuilder<INSTRUMENT_CREATION_ENTITY, INSTRUMENT_CREATION> resultBuilder = instrumentRequestDetailsHelper.readRequest(request);
        return afterReadInstrumentCreationRequest(actor, resultBuilder);

    }

    protected void beforeReadInstrumentCreationRequest(long actor, long request) {

        checkAccess(ruleValidator.canReadInstrumentRequestDetails(actor, request),
                "User has no access on read instrument creation request. User=" + actor + ", Request=" + request);

    }

    private INSTRUMENT_CREATION afterReadInstrumentCreationRequest(long actor, SingleResultBuilder<INSTRUMENT_CREATION_ENTITY, INSTRUMENT_CREATION> resultBuilder) {
        return resultBuilder.transform();
    }

    @Override
    public LAB_CREATION readLabRequestDetails(long actor, long request) {

        beforeReadLabCreationRequest(actor);
        final SingleResultBuilder<LAB_CREATION_ENTITY, LAB_CREATION> resultBuilder = labRequestDetailsHelper.readRequest(request);
        return afterREadLabCreationRequest(actor, resultBuilder);

    }

    protected void beforeReadLabCreationRequest(long actor) {

        checkAccess(ruleValidator.canReadLabs(actor), "User should be admin to read lab request details");

    }

    private LAB_CREATION afterREadLabCreationRequest(long actor, SingleResultBuilder<LAB_CREATION_ENTITY, LAB_CREATION> resultBuilder) {
        return resultBuilder.transform();
    }

    protected abstract INSTRUMENT_CREATION transformInstrumentCreationRequest(INSTRUMENT_CREATION_ENTITY instrumentCreationRequest);

    protected abstract LAB_CREATION transformLabCreationRequest(LAB_CREATION_ENTITY labCreationRequest);

}
