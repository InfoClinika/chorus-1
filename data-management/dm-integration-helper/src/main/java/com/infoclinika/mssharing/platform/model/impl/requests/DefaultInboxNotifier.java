package com.infoclinika.mssharing.platform.model.impl.requests;

import com.infoclinika.mssharing.platform.entity.InboxMessageTemplate;
import com.infoclinika.mssharing.platform.model.InboxNotifierTemplate;
import com.infoclinika.mssharing.platform.model.helper.write.InboxNotifierManager;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Pavel Kaplin
 */
@Primary
@Service
public class DefaultInboxNotifier implements InboxNotifierTemplate {

    @Inject
    protected InboxNotifierManager<InboxMessageTemplate> notifierHelper;

    @Override
    public void notify(long from, long to, String message) {
        notifierHelper.notify(from, to, message);
    }

}
