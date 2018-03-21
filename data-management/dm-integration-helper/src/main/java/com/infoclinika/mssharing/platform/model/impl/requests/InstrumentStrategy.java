package com.infoclinika.mssharing.platform.model.impl.requests;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.platform.model.helper.write.InstrumentManager;
import com.infoclinika.mssharing.platform.model.read.RequestsReaderTemplate;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Collection;

import static com.google.common.collect.Collections2.transform;
import static com.infoclinika.mssharing.platform.model.RequestsTemplate.InboxItem;
import static com.infoclinika.mssharing.platform.model.RequestsTemplate.InboxItem.Actions.APPROVE_REFUSE;

/**
 * @author Pavel Kaplin
 */
@Service
class InstrumentStrategy extends Strategy {

    @Inject
    private RequestsReaderTemplate requestsReader;

    @Inject
    private InstrumentManager instrumentManagement;

    @Override
    public void approve(long actor, String request) {
        String internalId = getInternalId(request);
        String[] split = internalId.split(",");
        long instrument = Long.parseLong(split[0]);
        long requester = Long.parseLong(split[1]);
        instrumentManagement.approveAccess(actor, instrument, requester);
    }

    @Override
    public void refuse(long actor, String request, String comment) {
        String internalId = getInternalId(request);
        String[] split = internalId.split(",");
        long instrument = Long.parseLong(split[0]);
        long requester = Long.parseLong(split[1]);
        instrumentManagement.refuseAccessToInstrument(actor, instrument, requester, comment);
    }

    @Override
    public void remove(long actor, String request) {
        throw new IllegalStateException("Not applicable operation");
    }

    @Override
    public Collection<InboxItem> getInboxItems(long actor) {
        ImmutableSortedSet<RequestsReaderTemplate.InstrumentRequest> instrumentRequests = requestsReader.myInstrumentInbox(actor);
        return transform(instrumentRequests, new Function<RequestsReaderTemplate.InstrumentRequest, InboxItem>() {
            @Override
            public InboxItem apply(RequestsReaderTemplate.InstrumentRequest input) {
                String id = input.instrument + "," + input.requester;
                return new InboxItem(buildGlobalId(id), input.requesterName, "Requested an access to " + input.instrumentName + " instrument.",
                        input.sent, APPROVE_REFUSE);
            }
        });

    }
}
