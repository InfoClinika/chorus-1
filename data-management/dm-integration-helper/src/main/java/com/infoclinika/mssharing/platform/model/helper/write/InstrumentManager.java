package com.infoclinika.mssharing.platform.model.helper.write;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.infoclinika.mssharing.platform.entity.*;
import com.infoclinika.mssharing.platform.model.EntityFactories;
import com.infoclinika.mssharing.platform.model.InboxNotifierTemplate;
import com.infoclinika.mssharing.platform.model.NotifierTemplate;
import com.infoclinika.mssharing.platform.model.RequestsTemplate;
import com.infoclinika.mssharing.platform.repository.InstrumentCreationRequestRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.InstrumentRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.LabRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.UserRepositoryTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.and;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static com.google.common.collect.Sets.newHashSet;
import static com.infoclinika.mssharing.platform.model.write.InstrumentManagementTemplate.InstrumentDetailsTemplate;

/**
 * @author Herman Zamula
 */
@Component
@SuppressWarnings("unchecked")
public class InstrumentManager<INSTRUMENT extends InstrumentTemplate, INSTRUMENT_CREATION_REQUEST extends InstrumentCreationRequestTemplate> {

    @Inject
    private InstrumentRepositoryTemplate<INSTRUMENT> instrumentRepository;
    @Inject
    private LabRepositoryTemplate<LabTemplate> labRepository;
    @Inject
    private UserRepositoryTemplate<UserTemplate> userRepository;
    @Inject
    private InstrumentCreationRequestRepositoryTemplate<INSTRUMENT_CREATION_REQUEST> instrumentCreationRequestRepository;
    @Inject
    private InboxNotifierTemplate inboxNotifier;
    @Inject
    private RequestsTemplate requests;
    @Inject
    private NotifierTemplate notifier;
    @Inject
    private EntityFactories factories;


    /**
     * Creates INSTRUMENT entity
     *
     * @param creator           creator id
     * @param labId             laboratory (organization) of instrument
     * @param model             instrument model id
     * @param instrumentDetails instrument properties
     * @param setPropertiesFn   use for set additional properties of custom INSTRUMENT implementation.
     *                          Called after set all common properties and before saving
     * @return saved in repository INSTRUMENT entity
     */
    public INSTRUMENT createInstrument(long creator, long labId, long model, InstrumentDetailsTemplate instrumentDetails, Function<INSTRUMENT, INSTRUMENT> setPropertiesFn) {

        final LabTemplate lab = labRepository.findOne(labId);
        final INSTRUMENT template = (INSTRUMENT) factories.instrument.get();
        final UserTemplate userTemplate = factories.user.get();
        userTemplate.setId(creator);
        template.setCreator(userTemplate);
        template.setLab(lab);
        template.setModel(new InstrumentModel(model));
        template.setSerialNumber(instrumentDetails.serialNumber);
        template.setPeripherals(instrumentDetails.peripherals);
        template.setName(instrumentDetails.name);
        final INSTRUMENT instrument = saveInstrument(setPropertiesFn.apply(template));
        addOperatorDirectly(creator, instrument.getId(), lab.getHead().getId());
        return instrument;
    }

    private INSTRUMENT saveInstrument(INSTRUMENT template) {
        template.setLastModification(new Date());
        return instrumentRepository.save(template);
    }

    public INSTRUMENT addOperatorDirectly(Long creator, long instrumentId, Long newOperatorId) {
        final INSTRUMENT instrument = instrumentRepository.findOne(instrumentId);
        final UserTemplate newOperator = userRepository.findOne(newOperatorId);
        if (instrument.getOperators().contains(newOperator)) return instrument;
        instrument.removePending(newOperator);
        instrument.addOperator(newOperator);
        notifier.userWasAddedToOperators(creator, newOperator.getId(), instrumentId);
        return instrument;
    }


    /**
     * Creates INSTRUMENT_CREATION_REQUEST entity
     *
     * @param creator    requester id
     * @param labId      laboratory for instrument
     * @param model      instrument model
     * @param details    instrument properties
     * @param operators  ids of USERs that have access to instrument
     * @param setPropsFn use for set additional properties of custom INSTRUMENT_CREATION_REQUEST implementation.
     *                   Called after set all common properties and before saving
     * @return saved INSTRUMENT_CREATION_REQUEST entity
     */
    public INSTRUMENT_CREATION_REQUEST newCreationRequest(long creator, long labId, long model, InstrumentDetailsTemplate details, List<Long> operators, Function<INSTRUMENT_CREATION_REQUEST, INSTRUMENT_CREATION_REQUEST> setPropsFn) {

        final UserTemplate one = userRepository.findOne(creator);
        final InstrumentModel instrumentModel = new InstrumentModel(model);
        final LabTemplate lab = labRepository.findOne(labId);
        final UserTemplate labHead = lab.getHead();
        final INSTRUMENT_CREATION_REQUEST template = (INSTRUMENT_CREATION_REQUEST) factories.instrumentRequest.get();

        template.setName(details.name);
        template.setPeripherals(details.peripherals);
        template.setModel(instrumentModel);
        template.setSerialNumber(details.serialNumber);
        template.setRequestDate(new Date());
        template.setRequester(one);
        template.setLab(lab);
        template.getOperators().addAll(transform(operators, factories.userFromId));
        template.getOperators().addAll(transform(newArrayList(creator, labHead.getId()), factories.userFromId));

        final INSTRUMENT_CREATION_REQUEST request = saveInstrumentRequest(setPropsFn.apply(template));

        notify(creator, lab, request);

        return request;
    }

    private void notify(long creator, LabTemplate lab, INSTRUMENT_CREATION_REQUEST request) {
        final UserTemplate labHead = lab.getHead();
        notifier.sendInstrumentCreationRequestNotification(labHead.getId(), request.getRequester().getEmail(), lab.getName(), request.getName());
        requests.addOutboxItem(creator,
                "Lab head " + labHead.getFullName(),
                "Request to create " + request.getName() + " instrument in " + request.getLab().getName() + " laboratory",
                new Date()
        );
    }

    /**
     * Updates given INSTRUMENT_CREATION_REQUEST
     *
     * @param request    instrument request entity to update
     * @param model      instrument model id
     * @param details    instrument details
     * @param operators  ids of USERs that have access to instrument
     * @param setPropsFn use for set additional properties of custom INSTRUMENT implementation.
     *                   Called after set all common properties and before saving
     * @return saved in repository INSTRUMENT_CREATION_REQUEST entity
     */
    public INSTRUMENT_CREATION_REQUEST updateInstrumentRequest(INSTRUMENT_CREATION_REQUEST request, long model, InstrumentDetailsTemplate details, List<Long> operators, Function<INSTRUMENT_CREATION_REQUEST, INSTRUMENT_CREATION_REQUEST> setPropsFn) {
        request.setName(details.name);
        request.setPeripherals(details.peripherals);
        request.setModel(new InstrumentModel(model));
        request.setSerialNumber(details.serialNumber);
        request.getOperators().clear();
        request.getOperators().addAll(Lists.transform(operators, factories.userFromId));

        return saveInstrumentRequest(setPropsFn.apply(request));
    }

    private INSTRUMENT_CREATION_REQUEST saveInstrumentRequest(INSTRUMENT_CREATION_REQUEST request) {
        request.setLastModification(new Date());
        return instrumentCreationRequestRepository.save(request);
    }

    public INSTRUMENT updateInstrument(INSTRUMENT instrument, InstrumentDetailsTemplate details, Function<INSTRUMENT, INSTRUMENT> setPropsFn) {
        instrument.setName(details.name);
        instrument.setPeripherals(details.peripherals);
        instrument.setSerialNumber(details.serialNumber);
        return saveInstrument(setPropsFn.apply(instrument));
    }

    public INSTRUMENT setOperators(Long actor, Long instrumentId, List<Long> newOperators) {
        final INSTRUMENT instrument = instrumentRepository.findOne(instrumentId);
        final UserTemplate labHead = instrument.getLab().getHead();
        final HashSet<UserTemplate> users = newHashSet(Iterables.transform(from(newOperators)
                        .filter(and(userNotActorFilter(actor), userNotLabHeadFilter(labHead.getId()))),
                factories.userFromId
        ));
        doAddNewOperators(actor, instrument, users);
        users.add(factories.userFromId.apply(actor));
        users.add(labHead);
        instrument.getOperators().clear();
        instrument.getOperators().addAll(users);
        return saveInstrument(instrument);
    }

    private void doAddNewOperators(Long actor, INSTRUMENT instrument, Set<UserTemplate> users) {
        for (UserTemplate newOperator : users) {
            if (instrument.isPending(newOperator)) {
                instrument.removePending(newOperator);
            }
            notifier.userWasAddedToOperators(actor, newOperator.getId(), instrument.getId());
        }
    }

    private Predicate<Long> userNotActorFilter(final long actor) {
        return new Predicate<Long>() {
            @Override
            public boolean apply(Long newOperator) {
                return actor != newOperator;
            }
        };
    }

    private Predicate<Long> userNotLabHeadFilter(final long labHead) {
        return new Predicate<Long>() {
            @Override
            public boolean apply(Long newOperator) {
                return labHead != newOperator;
            }
        };
    }

    public INSTRUMENT requestAccessToInstrument(long initiatorId, long instrumentId) {
        final INSTRUMENT instrument = instrumentRepository.findOne(instrumentId);
        final PendingOperator<?> pending = new PendingOperator<UserTemplate<?>>(new Date(), factories.userFromId.apply(initiatorId));
        requests.addOutboxItem(initiatorId, "Operators of " + instrument.getName(), "Requested an access to " + instrument.getName() + " instrument.", new Date());
        for (Object operatorObject : instrument.getOperators()) {
            UserTemplate operator = (UserTemplate) operatorObject;
            notifier.instrumentRequest(initiatorId, operator.getId(), instrument.getName());
        }
        instrument.addPending(pending);
        return instrumentRepository.save(instrument);
    }

    public INSTRUMENT refuseAccessToInstrument(long actor, long instrumentId, long requester, String refuseComment) {
        final INSTRUMENT instrument = instrumentRepository.findOne(instrumentId);
        final UserTemplate initiator = getUser(requester);
        if (instrument.isPending(initiator)) {
            instrument.removePending(initiator);
            notifier.instrumentRequestRefuse(actor, requester, instrumentId, refuseComment);
            inboxNotifier.notify(actor, requester, "Your request to access instrument " + instrument.getName() + " was refused: " + refuseComment);
            return instrument;
        }
        if (instrument.isOperator(initiator)) {
            notifier.staleOnInstrumentRequest(actor, instrumentId, requester);
        }
        return instrument;
    }

    public INSTRUMENT approveAccess(long actor, long instrumentId, long initiatorId) {
        final INSTRUMENT instrument = instrumentRepository.findOne(instrumentId);
        final UserTemplate initiator = getUser(initiatorId);
        if (instrument.isOperator(initiator)) return instrument;
        if (instrument.isPending(initiator)) {
            instrument.removePending(initiator);
            instrument.addOperator(initiator);
            notifier.instrumentRequestApproved(actor, initiatorId, instrumentId);
            inboxNotifier.notify(actor, initiatorId, "Your request to access instrument " + instrument.getName() + " was approved");
        } else {
            notifier.staleOnInstrumentRequest(actor, instrumentId, initiatorId);
        }
        return instrument;
    }

    private UserTemplate getUser(long initiatorId) {
        //noinspection unchecked
        return factories.userFromId.apply(initiatorId);
    }

    public INSTRUMENT approveInstrumentCreation(long actor, long request, Function<INSTRUMENT, INSTRUMENT> setPropsFn) {
        final INSTRUMENT_CREATION_REQUEST creationRequest = instrumentCreationRequestRepository.findOne(request);
        final INSTRUMENT instrument = createInstrument(creationRequest, setPropsFn);

        for (Object operatorObject : instrument.getOperators()) {
            UserTemplate operator = (UserTemplate) operatorObject;
            if (!operator.getId().equals(creationRequest.getRequester().getId())) {
                notifier.userWasAddedToOperators(actor, operator.getId(), instrument.getId());
            }
        }

        instrumentCreationRequestRepository.delete(creationRequest);

        inboxNotifier.notify(
                actor,
                creationRequest.getRequester().getId(),
                "Your request for creation " + instrument.getName() + " instrument was approved"
        );

        notifier.sendInstrumentCreationApprovedNotification(
                creationRequest.getRequester().getId(),
                creationRequest.getLab().getName(),
                creationRequest.getName()
        );

        return instrument;
    }

    public INSTRUMENT findInstrument(long instrumentId) {
        return checkNotNull(instrumentRepository.findOne(instrumentId), "Couldn't find such instrument");
    }


    private INSTRUMENT createInstrument(INSTRUMENT_CREATION_REQUEST request, Function<INSTRUMENT, INSTRUMENT> setPropsFn) {

        final INSTRUMENT entity = (INSTRUMENT) factories.instrument.get();
        entity.setCreator(request.getRequester());
        entity.setLab(request.getLab());
        entity.setModel(request.getModel());
        entity.setName(request.getName());
        entity.setSerialNumber(request.getSerialNumber());
        entity.setPeripherals(request.getPeripherals());

        entity.addOperator(request.getLab().getHead());
        entity.getOperators().addAll(request.getOperators());

        return saveInstrument(setPropsFn.apply(entity));
    }

    public void refuseInstrumentCreation(long actor, long requestId, String refuseComment) {

        final INSTRUMENT_CREATION_REQUEST request = instrumentCreationRequestRepository.findOne(requestId);
        notifier.sendInstrumentCreationRejectedNotification(
                request.getRequester().getId(),
                refuseComment,
                request.getLab().getName(),
                request.getName()
        );
        instrumentCreationRequestRepository.delete(request);

        inboxNotifier.notify(
                actor,
                request.getRequester().getId(),
                "Your request for creation " + request.getName() + "instrument was rejected: " + refuseComment
        );
    }
}
