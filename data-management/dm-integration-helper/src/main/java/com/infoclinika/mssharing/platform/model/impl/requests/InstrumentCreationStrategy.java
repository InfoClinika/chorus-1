package com.infoclinika.mssharing.platform.model.impl.requests;

import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.platform.model.RequestsTemplate.InboxItem;
import com.infoclinika.mssharing.platform.model.read.RequestsReaderTemplate;
import com.infoclinika.mssharing.platform.model.write.InstrumentManagementTemplate;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Collection;

import static com.google.common.collect.Collections2.transform;


/**
 * @author timofey.kasyanov
 *     date: 12.05.2014
 */
@Service
class InstrumentCreationStrategy extends Strategy {

    @Inject
    private RequestsReaderTemplate requestsReader;

    @Inject
    private InstrumentManagementTemplate instrumentManagement;

    @Override
    public Collection<InboxItem> getInboxItems(long actor) {

        final ImmutableSortedSet<RequestsReaderTemplate.InstrumentCreationRequestInfo> requests
            = requestsReader.myInstrumentCreationInbox(actor);

        return transform(requests, input -> new InboxItem(
            buildGlobalId(input.creationRequestId),
            input.contactEmail,
            "Requested creation of " + input.instrumentName + " instrument",
            input.sent,
            InboxItem.Actions.APPROVE_REFUSE
        ));

    }

    @Override
    public void approve(long actor, String request) {
        final String internalId = getInternalId(request);
        final long requestId = Long.parseLong(internalId);
        //noinspection unchecked
        instrumentManagement.approveInstrumentCreation(actor, requestId);
    }

    @Override
    public void refuse(long actor, String request, String comment) {
        final String internalId = getInternalId(request);
        final long requestId = Long.parseLong(internalId);
        instrumentManagement.refuseInstrumentCreation(actor, requestId, comment);
    }

    @Override
    public void remove(long actor, String request) {
        throw new IllegalStateException("Not applicable operation");
    }
}
