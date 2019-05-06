package com.infoclinika.mssharing.platform.model.helper.write;

import com.infoclinika.mssharing.platform.entity.InboxMessageTemplate;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.model.EntityFactories;
import com.infoclinika.mssharing.platform.repository.InboxMessageRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.UserRepositoryTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author Herman Zamula
 */

@Component
public class InboxNotifierManager<ENTITY extends InboxMessageTemplate> {
    @Inject
    private UserRepositoryTemplate<UserTemplate> userRepository;

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

    public List<Long> getLabMemberIds(long labId) {
        return userRepository.findAllUsersByLab(labId)
            .stream()
            .map(UserTemplate::getId)
            .collect(toList());
    }
}
