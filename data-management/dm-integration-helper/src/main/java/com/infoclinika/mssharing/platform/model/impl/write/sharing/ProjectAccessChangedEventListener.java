package com.infoclinika.mssharing.platform.model.impl.write.sharing;

import com.infoclinika.mssharing.platform.model.write.SharingManagementTemplate;
import org.springframework.context.ApplicationListener;

import javax.inject.Inject;

/**
 * @author : Alexander Serebriyan
 */
public class ProjectAccessChangedEventListener implements ApplicationListener<ProjectAccessChangedEvent> {

    @Inject
    private SharingManagementTemplate sharingManagement;

    @Override
    public void onApplicationEvent(ProjectAccessChangedEvent event) {
        sharingManagement.updateProjectAccessRecords(event.getProjectId(), event.getSharedTo());
    }
}
