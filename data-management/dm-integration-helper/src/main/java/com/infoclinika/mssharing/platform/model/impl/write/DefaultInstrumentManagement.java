package com.infoclinika.mssharing.platform.model.impl.write;

import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.infoclinika.mssharing.platform.entity.InstrumentCreationRequestTemplate;
import com.infoclinika.mssharing.platform.entity.InstrumentTemplate;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.NotifierTemplate;
import com.infoclinika.mssharing.platform.model.RuleValidator;
import com.infoclinika.mssharing.platform.model.helper.write.InstrumentManager;
import com.infoclinika.mssharing.platform.model.write.InstrumentManagementTemplate;
import com.infoclinika.mssharing.platform.repository.InstrumentCreationRequestRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.InstrumentRepositoryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.FluentIterable.from;
import static com.infoclinika.mssharing.platform.model.impl.ValidatorPreconditions.checkAccess;

/**
 * @author : Alexander Serebriyan
 */
@Transactional
@Component
public class DefaultInstrumentManagement<
        INSTRUMENT extends InstrumentTemplate,
        INSTRUMENT_DETAILS extends InstrumentManagementTemplate.InstrumentDetailsTemplate,
        INSTRUMENT_CREATION_REQUEST extends InstrumentCreationRequestTemplate>
        implements InstrumentManagementTemplate<INSTRUMENT_DETAILS> {

    @Inject
    protected InstrumentManager<INSTRUMENT, INSTRUMENT_CREATION_REQUEST> managementHelper;
    @Inject
    protected InstrumentRepositoryTemplate<INSTRUMENT> instrumentRepository;
    @Inject
    protected InstrumentCreationRequestRepositoryTemplate<INSTRUMENT_CREATION_REQUEST> instrumentCreationRequestRepository;
    @Inject
    protected NotifierTemplate notifier;
    @Inject
    private RuleValidator ruleValidator;

    @Override
    public long createInstrument(long creator, long labId, long model, INSTRUMENT_DETAILS instrumentDetails) {
        beforeCreateInstrument(creator, labId, instrumentDetails);
        final INSTRUMENT instrument = onCreateInstrument(creator, labId, model, instrumentDetails);
        return instrument.getId();
    }

    protected INSTRUMENT onCreateInstrument(long creator, long labId, long model, INSTRUMENT_DETAILS instrumentDetails) {
        return managementHelper.createInstrument(creator, labId, model,
                instrumentDetails, Functions.<INSTRUMENT>identity());
    }

    protected void beforeCreateInstrument(long creator, long labId, INSTRUMENT_DETAILS instrumentDetails) {
        if (!ruleValidator.canUserCreateInstrumentInLab(creator, labId)) {
            throw new AccessDenied("Only lab head and admins can create instrument in the laboratory");
        }
        if (!ruleValidator.canUserCreateInstrument(creator)) {
            throw new AccessDenied("User isn't permitted to create experiment - laboratory is not specified");
        }
        if (!ruleValidator.canInstrumentBeCreated(labId, instrumentDetails.name, instrumentDetails.serialNumber)) {
            throw new AccessDenied("Couldn't create instrument");
        }
    }

    @Override
    public void editInstrument(long actor, long instrumentId, INSTRUMENT_DETAILS details) {
        beforeEditInstrument(actor, instrumentId, details);
        onEditInstrument(instrumentId, details);
    }

    protected void onEditInstrument(long instrumentId, INSTRUMENT_DETAILS details) {
        managementHelper.updateInstrument(managementHelper.findInstrument(instrumentId), details, Functions.<INSTRUMENT>identity());
    }

    protected void beforeEditInstrument(long actor, long instrumentId, INSTRUMENT_DETAILS details) {
        if (!ruleValidator.canInstrumentBeUpdated(instrumentId, details.name, details.serialNumber)) {
            throw new AccessDenied("Can't save instrument. Check Instrument Name or Serial Number");
        }
        if (!ruleValidator.canUserEditInstrument(actor, instrumentId)) {
            throw new AccessDenied("User isn't permitted to edit instrument");
        }
    }

    @Override
    public void deleteInstrument(long actor, long instrumentId) {
        beforeDeleteInstrument(actor, instrumentId);
        instrumentRepository.delete(instrumentId);
    }

    protected void beforeDeleteInstrument(long actor, long instrumentId) {
        if (!ruleValidator.canRemoveInstrument(actor, instrumentId)) {
            throw new AccessDenied("Cannot remove instrument");
        }
    }

    @Override
    public void setInstrumentOperators(final long actor, long instrumentId, List<Long> newOperators) {
        beforeSetInstrumentOperators(actor, instrumentId, newOperators);
        managementHelper.setOperators(actor, instrumentId, newOperators);
    }

    protected void beforeSetInstrumentOperators(final long actor, long instrumentId, List<Long> newOperators) {
        checkUserCanEditOperatorsList(actor, instrumentId);
        if (newOperators.size() == 0) {
            throw new IllegalStateException("Number of operators must be equals to not zero");
        }

        if (!from(newOperators).allMatch(new Predicate<Long>() {
            @Override
            public boolean apply(Long newOperator) {
                return ruleValidator.canShareInstrument(actor, newOperator);
            }
        })) {
            throw new AccessDenied("Can't share instrument");

        }
    }

    @Override
    public void addOperatorDirectly(long initiator, long instrumentId, long newOperator) {
        beforeAddOperatorDirectly(initiator, instrumentId, newOperator);
        managementHelper.addOperatorDirectly(initiator, instrumentId, newOperator);
    }

    protected void beforeAddOperatorDirectly(long initiator, long instrumentId, long newOperator) {

        checkAccess(ruleValidator.canEditOperatorsList(initiator, instrumentId),
                "Actor cant add more newOperators");

        checkAccess(ruleValidator.canShareInstrument(initiator, newOperator),
                "Actor can't share instrument to new operator");

    }

    @Override
    public void requestAccessToInstrument(long initiator, long instrumentId) {
        beforeRequestAccessToInstrument(initiator, instrumentId);
        managementHelper.requestAccessToInstrument(initiator, instrumentId);
    }

    protected void beforeRequestAccessToInstrument(long initiator, long instrumentId) {
        final INSTRUMENT instrument = managementHelper.findInstrument(instrumentId);
        Set<UserTemplate> operators = instrument.getOperators();
        if (!ruleValidator.canShareInstrument(operators.iterator().next().getId(), initiator)) {
            throw new AccessDenied("Can't share instrument");
        }
    }

    @Override
    public void refuseAccessToInstrument(long actor, long instrumentId, long initiatorId, String refuseComment) {
        beforeRefuseAccessToInstrument(actor, instrumentId);
        managementHelper.refuseAccessToInstrument(actor, instrumentId, initiatorId, refuseComment);
    }

    protected void beforeRefuseAccessToInstrument(long actor, long instrumentId) {
        checkUserCanEditOperatorsList(actor, instrumentId);
    }

    private void checkUserCanEditOperatorsList(long actor, long instrumentId) {
        if (!ruleValidator.canEditOperatorsList(actor, instrumentId)) {
            throw new AccessDenied("Actor cant add more newOperators");
        }
    }

    @Override
    public void approveAccessToInstrument(long actor, long instrumentId, long initiatorId) {
        beforeApproveAccessToInstrument(actor, instrumentId);
        managementHelper.approveAccess(actor, instrumentId, initiatorId);
    }

    protected void beforeApproveAccessToInstrument(long actor, long instrumentId) {
        checkUserCanEditOperatorsList(actor, instrumentId);
    }

    @Override
    public long approveInstrumentCreation(long actor, long requestId) {
        beforeApproveInstrumentCreation(actor, requestId);

        final INSTRUMENT instrument = onApproveInstrumentCreation(actor, requestId);

        return instrument.getId();
    }

    protected INSTRUMENT onApproveInstrumentCreation(long actor, long requestId) {
        return managementHelper.approveInstrumentCreation(actor, requestId, Functions.<INSTRUMENT>identity());
    }

    protected void beforeApproveInstrumentCreation(long actor, long requestId) {
        final INSTRUMENT_CREATION_REQUEST one = instrumentCreationRequestRepository.findOne(requestId);

        if (one == null) {
            notifier.staleOnNewInstrumentRequest(actor, requestId);
            throw new StaleInstrumentCreationRequestException(requestId);
        }

        if (!ruleValidator.canUserCreateInstrumentInLab(actor, one.getLab().getId())) {
            throw new AccessDenied("Only lab head and admins can create instrument");
        }
        if (!ruleValidator.canUserCreateInstrument(actor)) {
            throw new AccessDenied("User isn't permitted to create instrument - lab is not specified");
        }
        if (!ruleValidator.canInstrumentBeCreated(one.getLab().getId(), one.getName(), one.getSerialNumber())) {
            throw new AccessDenied("Couldn't create instrument");
        }
    }

    @Override
    public void refuseInstrumentCreation(long actor, long requestId, String refuseComment) {
        beforeRefuseInstrumentCreation(actor, requestId);
        managementHelper.refuseInstrumentCreation(actor, requestId, refuseComment);
    }

    protected void beforeRefuseInstrumentCreation(long actor, long requestId) {
        final INSTRUMENT_CREATION_REQUEST one = instrumentCreationRequestRepository.findOne(requestId);

        if (one == null) {
            notifier.staleOnNewInstrumentRequest(actor, requestId);
            throw new StaleInstrumentCreationRequestException(requestId);
        }

        if (!ruleValidator.canUserCreateInstrumentInLab(actor, one.getLab().getId())) {
            throw new AccessDenied("Only lab head and admins can create instrument");
        }
    }

    @Override
    public Optional<Long> newInstrumentRequest(long creator, long labId, long model, INSTRUMENT_DETAILS instrumentDetails, List<Long> operators) {
        beforeNewInstrumentRequest(creator, labId, instrumentDetails);

        final INSTRUMENT_CREATION_REQUEST savedRequest = onNewInstrumentRequest(creator, labId, model, instrumentDetails, operators);

        return Optional.of(savedRequest.getId());
    }

    protected INSTRUMENT_CREATION_REQUEST onNewInstrumentRequest(long creator, long labId, long model, INSTRUMENT_DETAILS instrumentDetails, List<Long> operators) {
        return managementHelper.newCreationRequest(creator,
                labId, model, instrumentDetails, operators,
                Functions.<INSTRUMENT_CREATION_REQUEST>identity());
    }

    protected void beforeNewInstrumentRequest(long creator, long labId, INSTRUMENT_DETAILS instrumentDetails) {
        if (!ruleValidator.canUserCreateInstrument(creator)) {
            throw new AccessDenied("User isn't permitted to create instrument - lab is not specified");
        }
        if (!ruleValidator.canInstrumentBeCreated(labId, instrumentDetails.name, instrumentDetails.serialNumber)) {
            throw new AccessDenied("Couldn't create instrument");
        }
    }

    @Override
    public Optional<Long> updateNewInstrumentRequest(long actor, long request, long model, INSTRUMENT_DETAILS details, List<Long> operators) {
        final INSTRUMENT_CREATION_REQUEST instrumentCreationRequest = checkNotNull(
                instrumentCreationRequestRepository.findOne(request),
                "Cannot find request by id: " + request
        );

        beforeUpdateNewInstrumentRequest(actor, details, instrumentCreationRequest);
        final INSTRUMENT_CREATION_REQUEST updatedRequest = onUpdateNewInstrumentRequest(model, details, operators, instrumentCreationRequest);

        return Optional.of(updatedRequest.getId());
    }

    protected INSTRUMENT_CREATION_REQUEST onUpdateNewInstrumentRequest(long model, INSTRUMENT_DETAILS details, List<Long> operators, INSTRUMENT_CREATION_REQUEST instrumentCreationRequest) {
        return managementHelper.updateInstrumentRequest(instrumentCreationRequest, model, details, operators, Functions.<INSTRUMENT_CREATION_REQUEST>identity());
    }

    protected void beforeUpdateNewInstrumentRequest(long actor, INSTRUMENT_DETAILS details, INSTRUMENT_CREATION_REQUEST one) {
        if (!ruleValidator.canUserCreateInstrumentInLab(actor, one.getLab().getId())) {
            throw new AccessDenied("Only lab head and admins can create instrument");
        }
        if (!ruleValidator.canUserCreateInstrument(actor)) {
            throw new AccessDenied("User isn't permitted to create experiment - laboratory is not specified");
        }
        if (!ruleValidator.canInstrumentBeCreated(one.getLab().getId(), details.name, details.serialNumber)) {
            throw new AccessDenied("Couldn't create instrument");
        }
    }
}
