package com.infoclinika.mssharing.platform.model.impl.write.sharing;

import com.infoclinika.mssharing.platform.model.write.SharingManagementTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author : Alexander Serebriyan
 */

@Service
public class ProjectAccessChangedEventPublisher implements ApplicationEventPublisherAware {

    private ApplicationEventPublisher publisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }


    public void publish(long projectId, Map<Long, SharingManagementTemplate.Access> sharedTo) {
        final ProjectAccessChangedEvent projectAccessChangedEvent = new ProjectAccessChangedEvent(projectId, sharedTo);
        publisher.publishEvent(projectAccessChangedEvent);

    }
}
