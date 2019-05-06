package com.infoclinika.mssharing.platform.model.impl.requests;

import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.platform.model.RequestsTemplate.InboxItem;
import com.infoclinika.mssharing.platform.model.read.RequestsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.RequestsReaderTemplate.ProjectSharingInfo;
import com.infoclinika.mssharing.platform.model.write.ProjectSharingRequestManagement;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Collection;

import static com.google.common.collect.Collections2.transform;

/**
 * @author Nikita Matrosov
 */
@Service
class ProjectSharingStrategy extends Strategy {

    @Inject
    private RequestsReaderTemplate requestsReader;

    @Inject
    private ProjectSharingRequestManagement projectSharingRequestManagement;

    @Override
    public void approve(long actor, String request) {
        String internalId = getInternalId(request);
        String[] split = internalId.split(",");
        long project = Long.parseLong(split[0]);
        long requester = Long.parseLong(split[1]);
        projectSharingRequestManagement.approveSharingProject(actor, project, requester);
    }

    @Override
    public void refuse(long actor, String request, String comment) {
        String internalId = getInternalId(request);
        String[] split = internalId.split(",");
        long project = Long.parseLong(split[0]);
        long requester = Long.parseLong(split[1]);
        projectSharingRequestManagement.refuseSharingProject(actor, project, requester, comment);
    }

    @Override
    public void remove(long actor, String request) {
        throw new IllegalStateException("Not applicable operation");
    }

    @Override
    public Collection<InboxItem> getInboxItems(long actor) {
        ImmutableSortedSet<ProjectSharingInfo> instrumentRequests = requestsReader.myProjectSharingInbox(actor);
        return transform(instrumentRequests, input -> {
            String id = input.project + "," + input.requester;
            return new InboxItem(
                buildGlobalId(id),
                input.requesterName,
                "User " + input.requesterName + " has attempted to download an experiment " +
                    "and requested an access to the parent project \"" + input.projectName + "\"",
                input.sent,
                InboxItem.Actions.APPROVE_REFUSE
            );
        });
    }
}

