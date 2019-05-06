package com.infoclinika.mssharing.platform.model.impl.requests;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.platform.model.read.RequestsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.RequestsReaderTemplate.LabRequest;
import com.infoclinika.mssharing.platform.model.write.LabManagementTemplate;
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
class LabCreationStrategy extends Strategy {

    @Inject
    private RequestsReaderTemplate requestsReader;

    @Inject
    private LabManagementTemplate labManagement;

    @Override
    public Collection<InboxItem> getInboxItems(long actor) {
        ImmutableSortedSet<LabRequest> labRequests = requestsReader.myLabsInbox(actor);
        return transform(labRequests, input -> new InboxItem(buildGlobalId(input.labRequest), input.contactEmail,
            "Requested creation of " + input.labName + " laboratory",
            input.sent, APPROVE_REFUSE
        ));
    }

    private long getRequestId(String request) {
        return Long.parseLong(getInternalId(request));
    }

    @Override
    public void approve(long actor, String request) {
        labManagement.confirmLabCreation(actor, getRequestId(request));
    }

    @Override
    public void refuse(long actor, String request, String comment) {
        labManagement.rejectLabCreation(actor, getRequestId(request), comment);
    }

    @Override
    public void remove(long actor, String request) {
        throw new IllegalStateException("Not applicable operation");
    }
}
