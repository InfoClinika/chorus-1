package com.infoclinika.mssharing.platform.model.impl.requests;

import com.infoclinika.mssharing.platform.entity.InboxMessageTemplate;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.repository.InboxMessageRepositoryTemplate;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Collections2.transform;
import static com.infoclinika.mssharing.platform.model.RequestsTemplate.InboxItem;

/**
 * @author Pavel Kaplin
 */
@Service
class NotificationsStrategy extends Strategy {

    @Inject
    private InboxMessageRepositoryTemplate<InboxMessageTemplate> messageRepository;

    @Override
    public Collection<InboxItem> getInboxItems(long actor) {
        List<InboxMessageTemplate> inboxMessages = messageRepository.findByTo(actor);
        return transform(inboxMessages, input -> new InboxItem(
            buildGlobalId(input.getId()),
            input.getFrom().getFullName(),
            input.getMessage(),
            input.getDate(),
            InboxItem.Actions.OK
        ));
    }

    @Override
    public void approve(long actor, String request) {
        throw new IllegalStateException("Not applicable operation");
    }

    @Override
    public void refuse(long actor, String request, String comment) {
        throw new IllegalStateException("Not applicable operation");
    }

    @Override
    public void remove(long actor, String request) {
        InboxMessageTemplate message = messageRepository.findOne(Long.valueOf(getInternalId(request)));
        if (message.getTo().getId() != actor) {
            throw new AccessDenied("Could not remove inbox item owned by other user");
        }
        messageRepository.delete(message);
    }
}
