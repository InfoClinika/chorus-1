package com.infoclinika.mssharing.platform.model.impl.write.sharing;

import com.infoclinika.mssharing.platform.model.write.SharingManagementTemplate;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

/**
 * @author : Alexander Serebriyan
 */
public class ProjectAccessChangedEvent extends ApplicationEvent {

    private final long projectId;
    private final Map<Long, SharingManagementTemplate.Access> sharedTo;

    public ProjectAccessChangedEvent(long projectId, Map<Long, SharingManagementTemplate.Access> sharedTo) {
        super(projectId);
        this.projectId = projectId;
        this.sharedTo = sharedTo;
    }

    public long getProjectId() {
        return projectId;
    }

    public Map<Long, SharingManagementTemplate.Access> getSharedTo() {
        return sharedTo;
    }
}
