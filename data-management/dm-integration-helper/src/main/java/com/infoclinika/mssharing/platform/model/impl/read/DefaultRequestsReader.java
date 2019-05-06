package com.infoclinika.mssharing.platform.model.impl.read;

import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.platform.entity.*;
import com.infoclinika.mssharing.platform.model.EntityFactories;
import com.infoclinika.mssharing.platform.model.RuleValidator;
import com.infoclinika.mssharing.platform.model.helper.read.requests.InstrumentCreationRequestHelper;
import com.infoclinika.mssharing.platform.model.helper.read.requests.LabCreationRequestHelper;
import com.infoclinika.mssharing.platform.model.helper.read.requests.LabMembershipRequestHelper;
import com.infoclinika.mssharing.platform.model.helper.read.requests.ProjectSharingInboxHelper;
import com.infoclinika.mssharing.platform.model.read.RequestsReaderTemplate;
import com.infoclinika.mssharing.platform.repository.InstrumentRepositoryTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Comparator;

/**
 * @author Herman Zamula
 */
@SuppressWarnings("unchecked")
@Transactional(readOnly = true)
public abstract class DefaultRequestsReader<INSTRUMENT extends InstrumentTemplate,
    PROJECT_REQUEST extends ProjectSharingRequestTemplate,
    MEMBERSHIP_REQUEST extends UserLabMembershipRequestTemplate,
    LAB_CREATION_REQUEST extends LabCreationRequestTemplate,
    INSTRUMENT_CREATION_REQUEST extends InstrumentCreationRequestTemplate>

    implements RequestsReaderTemplate {


    private final Comparator<Comparable> comparator = Comparator.reverseOrder();
    @Inject
    protected InstrumentRepositoryTemplate<INSTRUMENT> instrumentRepository;
    @Inject
    protected RuleValidator ruleValidator;
    @Inject
    protected EntityFactories entityFactories;
    @Inject
    protected ProjectSharingInboxHelper<PROJECT_REQUEST, ProjectSharingInfo> projectSharingInboxHelper;
    @Inject
    protected LabMembershipRequestHelper<MEMBERSHIP_REQUEST, LabMembershipRequest> labMembershipRequestHelper;
    @Inject
    protected LabCreationRequestHelper<LAB_CREATION_REQUEST, LabRequest> labCreationRequestHelper;
    @Inject
    protected InstrumentCreationRequestHelper<INSTRUMENT_CREATION_REQUEST, InstrumentCreationRequestInfo>
        instrumentCreationRequestHelper;

    @Override
    public ImmutableSortedSet<LabRequest> myLabsInbox(long actor) {

        if (!ruleValidator.userCanSeeLabRequests(actor)) {
            return ImmutableSortedSet.of();
        }

        return labCreationRequestHelper.readInbox()
            .transform()
            .toSortedSet(comparator);
    }

    @Override
    public ImmutableSortedSet<InstrumentCreationRequestInfo> myInstrumentCreationInbox(final long actor) {

        return instrumentCreationRequestHelper.readRequests(actor)
            .transform()
            .toSortedSet(comparator);
    }

    @Override
    public ImmutableSortedSet<LabMembershipRequest> myLabMembershipInbox(long actor) {

        return labMembershipRequestHelper.readInbox(actor)
            .transform()
            .toSortedSet(comparator);

    }

    @Override
    public ImmutableSortedSet<ProjectSharingInfo> myProjectSharingInbox(long actor) {

        return projectSharingInboxHelper.readProject(actor)
            .transform()
            .toSortedSet(comparator);

    }

    @Override
    public ImmutableSortedSet<LabMembershipRequest> myLabMembershipOutbox(long actor) {

        return labMembershipRequestHelper.readOutbox(actor)
            .transform()
            .toSortedSet(comparator);

    }
}
