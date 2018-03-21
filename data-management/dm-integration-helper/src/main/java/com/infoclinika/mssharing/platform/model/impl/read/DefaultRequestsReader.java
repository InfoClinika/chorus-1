package com.infoclinika.mssharing.platform.model.impl.read;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.platform.entity.*;
import com.infoclinika.mssharing.platform.model.AccessDenied;
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
import java.util.Set;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.find;

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


    private final Comparator<Comparable> comparator = new Comparator<Comparable>() {
        @Override
        public int compare(Comparable o1, Comparable o2) {
            return o2.compareTo(o1);
        }
    };
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
    protected InstrumentCreationRequestHelper<INSTRUMENT_CREATION_REQUEST, InstrumentCreationRequestInfo> instrumentCreationRequestHelper;
    private Function<INSTRUMENT, Iterable<InstrumentRequest>> instrumentRequestTransformer = new Function<INSTRUMENT, Iterable<InstrumentRequest>>() {
        @Override
        public Iterable<InstrumentRequest> apply(final INSTRUMENT instrument) {
            return from(instrument.getPending())
                    .transform(new Function<PendingOperator, InstrumentRequest>() {
                        @Override
                        public InstrumentRequest apply(PendingOperator pending) {
                            return new InstrumentRequest(pending.getUser().getFullName(), pending.getUser().getId(), instrument.getName(), pending.getStartPending(), instrument.getId());
                        }
                    });
        }
    };

    @Override
    public ImmutableSortedSet<LabRequest> myLabsInbox(long actor) {

        if (!ruleValidator.userCanSeeLabRequests(actor))
            return ImmutableSortedSet.of();

        return labCreationRequestHelper.readInbox()
                .transform()
                .toSortedSet(comparator);
    }

    @Override
    public ImmutableSortedSet<InstrumentRequest> myInstrumentInbox(long actor) {

        final UserTemplate<?> user = entityFactories.userFromId.apply(actor);

        return from(instrumentRepository.findWhereOperatorIs(user))
                .transformAndConcat(instrumentRequestTransformer)
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

    @Override
    public InstrumentRequestDetails myInstrumentInboxDetails(long actor, long instrument, final long requester) {

        onBeforeReadInstrumentInboxDetails(actor, instrument);

        final INSTRUMENT instrumentEntity = instrumentRepository.findOne(instrument);
        Set<PendingOperator> pendingOperators = instrumentEntity.getPending();
        final PendingOperator pending = find(pendingOperators, new Predicate<PendingOperator>() {
            @Override
            public boolean apply(PendingOperator pendingOperator) {
                return pendingOperator.getUser().getId().equals(requester);
            }
        });

        return new InstrumentRequestDetails(pending.getUser().getFullName(), instrumentEntity.getName(), pending.getStartPending(),
                instrumentEntity.getModel().getVendor().getName(), instrumentEntity.getModel().getVendor().getName(), instrumentEntity.getModel().getName(),
                instrumentEntity.getSerialNumber(), "", instrumentEntity.getPeripherals(), instrumentEntity.getLab().getName());


    }

    protected void onBeforeReadInstrumentInboxDetails(long actor, long instrument) {
        if (!ruleValidator.canReadInstrumentInboxDetails(actor, instrument)) {
            throw new AccessDenied("User has no rights to read instrument inbox details");
        }
    }
}
