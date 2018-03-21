package com.infoclinika.mssharing.platform.model.impl.requests;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.platform.entity.OutboxMessageTemplate;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.EntityFactories;
import com.infoclinika.mssharing.platform.model.RequestsTemplate;
import com.infoclinika.mssharing.platform.repository.OutboxMessageRepositoryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Collections2.transform;

/**
 * @author Pavel Kaplin
 */
@Service
public class DefaultRequests implements RequestsTemplate {

    private static final Comparator<RequestsTemplate.InboxItem> INBOX_ITEMS_BY_DATE = new Comparator<RequestsTemplate.InboxItem>() {
        @Override
        public int compare(RequestsTemplate.InboxItem one, RequestsTemplate.InboxItem two) {
            int result = one.sent.compareTo(two.sent);
            if (result == 0) {
                return one.hashCode() - two.hashCode();
            }
            return result;
        }
    };
    private static final Comparator<RequestsTemplate.OutboxItem> OUTBOX_ITEMS_BY_DATE = new Comparator<RequestsTemplate.OutboxItem>() {
        @Override
        public int compare(RequestsTemplate.OutboxItem one, RequestsTemplate.OutboxItem two) {
            int result = one.sent.compareTo(two.sent);
            if (result == 0) {
                return one.hashCode() - two.hashCode();
            }
            return result;
        }
    };
    @Inject
    private Collection<Strategy> strategies;
    @Inject
    private OutboxMessageRepositoryTemplate<OutboxMessageTemplate> outboxMessageRepository;
    @Inject
    private EntityFactories entityFactories;

    @Override
    @Transactional(readOnly = true)
    public ImmutableSortedSet<RequestsTemplate.InboxItem> getInboxItems(long actor) {
        final ImmutableSortedSet.Builder<RequestsTemplate.InboxItem> builder = ImmutableSortedSet.orderedBy(INBOX_ITEMS_BY_DATE);

        for (Strategy strategy : strategies) {
            builder.addAll(strategy.getInboxItems(actor));
        }

        return builder.build();
    }

    @Override
    @Transactional
    public void removeInboxItem(long actor, String request) {
        getStrategy(request).remove(actor, request);
    }

    @Override
    @Transactional
    public void removeOutboxItem(long actor, String request) {
        OutboxMessageTemplate outbpxMessage = outboxMessageRepository.findOne(Long.parseLong(request));
        if (!outbpxMessage.getFrom().getId().equals(actor)) {
            throw new AccessDenied("Could not remove outbox item owned by other user");
        }
        outboxMessageRepository.delete(outbpxMessage);
    }

    @Override
    @Transactional
    public void approve(long actor, String request) {
        getStrategy(request).approve(actor, request);
    }

    private Strategy getStrategy(String request) {
        for (Strategy strategy : strategies) {
            if (strategy.canHandle(request)) {
                return strategy;
            }
        }
        throw new IllegalArgumentException("Could not find strategy for request id " + request);
    }

    @Override
    @Transactional
    public void refuse(long actor, String request, String comment) {
        getStrategy(request).refuse(actor, request, comment);
    }

    @Override
    @Transactional
    public void addOutboxItem(long from, String to, String message, Date date) {
        final OutboxMessageTemplate messageTemplate = entityFactories.outboxMessage.get();
        messageTemplate.setRecipient(to);
        messageTemplate.setMessage(message);
        messageTemplate.setDate(date);
        messageTemplate.setFrom(entityFactories.userFromId.apply(from));
        outboxMessageRepository.save(messageTemplate);
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableSortedSet<RequestsTemplate.OutboxItem> getOutboxItems(long actor) {
        List<OutboxMessageTemplate> messages = outboxMessageRepository.findByFrom(actor);
        ImmutableSortedSet.Builder<RequestsTemplate.OutboxItem> builder = ImmutableSortedSet.orderedBy(OUTBOX_ITEMS_BY_DATE);
        builder.addAll(transform(messages, new Function<OutboxMessageTemplate, RequestsTemplate.OutboxItem>() {
            @Override
            public RequestsTemplate.OutboxItem apply(OutboxMessageTemplate input) {
                return new RequestsTemplate.OutboxItem(input.getId().toString(), input.getRecipient(), input.getMessage(), input.getDate());
            }
        }));
        return builder.build();
    }

}
