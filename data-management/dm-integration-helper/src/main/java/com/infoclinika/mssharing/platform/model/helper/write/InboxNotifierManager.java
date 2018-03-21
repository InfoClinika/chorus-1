package com.infoclinika.mssharing.platform.model.helper.write;

import com.infoclinika.mssharing.platform.entity.InboxMessageTemplate;
import com.infoclinika.mssharing.platform.model.EntityFactories;
import com.infoclinika.mssharing.platform.repository.InboxMessageRepositoryTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Date;

/**
 * @author Herman Zamula
 */

@Component
public class InboxNotifierManager<ENTITY extends InboxMessageTemplate> {

    @Inject
    private InboxMessageRepositoryTemplate<ENTITY> messageRepository;
    @Inject
    private EntityFactories factories;


    @SuppressWarnings("unchecked")
    public ENTITY notify(long from, long to, String message) {
        final ENTITY template = (ENTITY) factories.inboxMessage.get();
        template.setFrom(factories.userFromId.apply(from));
        template.setTo(factories.userFromId.apply(to));
        template.setDate(new Date());
        template.setMessage(message);
        return messageRepository.save(template);
    }
}
