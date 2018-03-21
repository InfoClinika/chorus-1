package com.infoclinika.mssharing.platform.model.impl.requests;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.platform.model.RequestsTemplate.InboxItem;
import com.infoclinika.mssharing.platform.model.read.RequestsReaderTemplate;
import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Collection;

import static com.google.common.collect.Collections2.transform;
import static com.infoclinika.mssharing.platform.model.read.RequestsReaderTemplate.LabMembershipRequest;

/**
 * @author Pavel Kaplin
 */
@Service
class LabMembershipStrategy extends Strategy {

    @Inject
    private RequestsReaderTemplate requestsReader;

    @Inject
    private UserManagementTemplate userManagement;

    @Override
    public Collection<InboxItem> getInboxItems(long actor) {
        ImmutableSortedSet<RequestsReaderTemplate.LabMembershipRequest> labMembershipRequests = requestsReader.myLabMembershipInbox(actor);
        return transform(labMembershipRequests, new Function<LabMembershipRequest, InboxItem>() {
            @Override
            public InboxItem apply(LabMembershipRequest input) {
                return new InboxItem(buildGlobalId(input.requestId),
                        input.requesterName,
                        "Requested a membership in " + input.labName + " lab.",
                        input.sent, InboxItem.Actions.APPROVE_REFUSE);
            }
        });
    }

    @Override
    public void approve(long actor, String request) {
        long requestId = getRequestId(request);
        userManagement.approveLabMembershipRequest(actor, requestId);
    }

    private long getRequestId(String request) {
        return Long.parseLong(getInternalId(request));
    }

    @Override
    public void refuse(long actor, String request, String comment) {
        userManagement.rejectLabMembershipRequest(actor, getRequestId(request), comment);
    }

    @Override
    public void remove(long actor, String request) {
        throw new IllegalStateException("Not applicable operation");
    }


}
