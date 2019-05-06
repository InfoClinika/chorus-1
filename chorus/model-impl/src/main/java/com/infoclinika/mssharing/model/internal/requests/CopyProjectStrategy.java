package com.infoclinika.mssharing.model.internal.requests;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.model.read.RequestsReader;
import com.infoclinika.mssharing.model.write.StudyManagement;
import com.infoclinika.mssharing.platform.model.RequestsTemplate.InboxItem;
import com.infoclinika.mssharing.platform.model.impl.requests.Strategy;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Collection;

import static com.google.common.collect.Collections2.transform;

/**
 * @author Herman Zamula
 */
@Service
public class CopyProjectStrategy extends Strategy {

    @Inject
    private RequestsReader requestsReader;

    @Lazy
    @Inject
    private StudyManagement studyManagement;

    @Override
    public Collection<InboxItem> getInboxItems(long actor) {
        final ImmutableSortedSet<RequestsReader.ProjectCopyRequest> requests = requestsReader.myCopyProjectInbox(actor);
        return transform(requests, new Function<RequestsReader.ProjectCopyRequest, InboxItem>() {
            @Override
            public InboxItem apply(RequestsReader.ProjectCopyRequest input) {
                return new InboxItem(
                    buildGlobalId(input.project),
                    input.fullName,
                    String.format("%s has passed a copy of \"%s\" project to You", input.fullName, input.projectName),
                    input.dateSent,
                    InboxItem.Actions.APPROVE_REFUSE
                );
            }
        });
    }

    @Override
    public void approve(long actor, String request) {
        String internalId = getInternalId(request);
        String[] split = internalId.split(",");
        long project = Long.parseLong(split[0]);
        long billLab = Long.parseLong(split[1]);
        studyManagement.approveCopyProjectRequest(actor, project, billLab);
    }

    @Override
    public void refuse(long actor, String request, String comment) {
        String internalId = getInternalId(request);
        String[] split = internalId.split(",");
        long project = Long.parseLong(split[0]);
        studyManagement.refuseCopyProjectRequest(actor, project);
    }

    @Override
    public void remove(long actor, String request) {
        throw new IllegalStateException("Unsupported Operation: Remove.");
    }
}
