package com.infoclinika.mssharing.platform.model.helper.write;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.*;
import com.infoclinika.mssharing.platform.model.EntityFactories;
import com.infoclinika.mssharing.platform.model.InboxNotifierTemplate;
import com.infoclinika.mssharing.platform.model.NotifierTemplate;
import com.infoclinika.mssharing.platform.model.RequestsTemplate;
import com.infoclinika.mssharing.platform.repository.*;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Date;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.infoclinika.mssharing.platform.model.write.InstrumentManagementTemplate.InstrumentDetailsTemplate;

/**
 * @author Herman Zamula
 */
@Component
@SuppressWarnings("unchecked")
public class InstrumentManager<INSTRUMENT extends InstrumentTemplate,
    INSTRUMENT_CREATION_REQUEST extends InstrumentCreationRequestTemplate> {

    @Inject
    private InstrumentRepositoryTemplate<INSTRUMENT> instrumentRepository;
    @Inject
    private LabRepositoryTemplate<LabTemplate> labRepository;
    @Inject
    private UserRepositoryTemplate<UserTemplate> userRepository;
    @Inject
    private InstrumentCreationRequestRepositoryTemplate<INSTRUMENT_CREATION_REQUEST>
        instrumentCreationRequestRepository;
    @Inject
    private InboxNotifierTemplate inboxNotifier;
    @Inject
    private RequestsTemplate requests;
    @Inject
    private NotifierTemplate notifier;
    @Inject
    private EntityFactories factories;

    private INSTRUMENT createInstrument(INSTRUMENT_CREATION_REQUEST request,
                                        Function<INSTRUMENT, INSTRUMENT> setPropsFn) {

        final INSTRUMENT entity = (INSTRUMENT) factories.instrument.get();
        entity.setCreator(request.getRequester());
        entity.setLab(request.getLab());
        entity.setModel(request.getModel());
        entity.setName(request.getName());
        entity.setSerialNumber(request.getSerialNumber());
        entity.setPeripherals(request.getPeripherals());

        return saveInstrument(Objects.requireNonNull(setPropsFn.apply(entity)));
    }

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
    public INSTRUMENT createInstrument(long creator, long labId, long model,
                                       InstrumentDetailsTemplate instrumentDetails,
                                       Function<INSTRUMENT, INSTRUMENT> setPropertiesFn) {

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
        final INSTRUMENT instrument = saveInstrument(Objects.requireNonNull(setPropertiesFn.apply(template)));

        return instrument;
    }

    private INSTRUMENT saveInstrument(INSTRUMENT template) {
        template.setLastModification(new Date());
        return instrumentRepository.save(template);
    }

    /**
     * Creates INSTRUMENT_CREATION_REQUEST entity
     *
     * @param creator    requester id
     * @param labId      laboratory for instrument
     * @param model      instrument model
     * @param details    instrument properties
     * @param setPropsFn use for set additional properties of custom INSTRUMENT_CREATION_REQUEST implementation.
     *                   Called after set all common properties and before saving
     * @return saved INSTRUMENT_CREATION_REQUEST entity
     */
    public INSTRUMENT_CREATION_REQUEST newCreationRequest(long creator, long labId, long model,
                                                          InstrumentDetailsTemplate details,
                                                          Function<INSTRUMENT_CREATION_REQUEST,
                                                              INSTRUMENT_CREATION_REQUEST> setPropsFn) {

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

        final INSTRUMENT_CREATION_REQUEST request = saveInstrumentRequest(setPropsFn.apply(template));

        notify(creator, lab, request);

        return request;
    }

    private void notify(long creator, LabTemplate lab, INSTRUMENT_CREATION_REQUEST request) {
        final UserTemplate labHead = lab.getHead();
        notifier.sendInstrumentCreationRequestNotification(labHead.getId(), request.getRequester().getEmail(),
            lab.getName(), request.getName()
        );
        requests.addOutboxItem(
            creator,
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
     * @param setPropsFn use for set additional properties of custom INSTRUMENT implementation.
     *                   Called after set all common properties and before saving
     * @return saved in repository INSTRUMENT_CREATION_REQUEST entity
     */
    public INSTRUMENT_CREATION_REQUEST updateInstrumentRequest(INSTRUMENT_CREATION_REQUEST request, long model,
                                                               InstrumentDetailsTemplate details,
                                                               Function<INSTRUMENT_CREATION_REQUEST,
                                                                   INSTRUMENT_CREATION_REQUEST> setPropsFn) {
        request.setName(details.name);
        request.setPeripherals(details.peripherals);
        request.setModel(new InstrumentModel(model));
        request.setSerialNumber(details.serialNumber);

        return saveInstrumentRequest(setPropsFn.apply(request));
    }

    private INSTRUMENT_CREATION_REQUEST saveInstrumentRequest(INSTRUMENT_CREATION_REQUEST request) {
        request.setLastModification(new Date());
        return instrumentCreationRequestRepository.save(request);
    }

    public INSTRUMENT updateInstrument(INSTRUMENT instrument, InstrumentDetailsTemplate details,
                                       Function<INSTRUMENT, INSTRUMENT> setPropsFn) {
        instrument.setName(details.name);
        instrument.setPeripherals(details.peripherals);
        instrument.setSerialNumber(details.serialNumber);
        return saveInstrument(setPropsFn.apply(instrument));
    }

    public INSTRUMENT approveInstrumentCreation(long actor, long request, Function<INSTRUMENT, INSTRUMENT> setPropsFn) {
        final INSTRUMENT_CREATION_REQUEST creationRequest = instrumentCreationRequestRepository.findOne(request);
        final INSTRUMENT instrument = createInstrument(creationRequest, setPropsFn);

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
