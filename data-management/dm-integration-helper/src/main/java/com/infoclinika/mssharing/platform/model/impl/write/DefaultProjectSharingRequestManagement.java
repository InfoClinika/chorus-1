package com.infoclinika.mssharing.platform.model.impl.write;

import com.infoclinika.mssharing.platform.entity.ProjectSharingRequestTemplate;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.model.InboxNotifierTemplate;
import com.infoclinika.mssharing.platform.model.NotifierTemplate;
import com.infoclinika.mssharing.platform.model.helper.write.ProjectSharingRequestManager;
import com.infoclinika.mssharing.platform.model.helper.write.SharingManager;
import com.infoclinika.mssharing.platform.model.write.ProjectSharingRequestManagement;
import com.infoclinika.mssharing.platform.model.write.SharingManagementTemplate;
import com.infoclinika.mssharing.platform.repository.ProjectRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.ProjectSharingRequestRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.UserRepositoryTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static com.infoclinika.mssharing.platform.model.impl.ValidatorPreconditions.checkPresence;

/**
 * @author Herman Zamula
 */
@Component
@Transactional
public class DefaultProjectSharingRequestManagement implements ProjectSharingRequestManagement {

    @Inject
    private ProjectSharingRequestManager<ProjectSharingRequestTemplate> sharingRequestManagementHelper;
    @Inject
    private UserRepositoryTemplate<?> userRepository;
    @Inject
    private SharingManager sharingManager;
    @Inject
    private ProjectSharingRequestRepositoryTemplate<ProjectSharingRequestTemplate> sharingRequestRepository;
    @Inject
    private InboxNotifierTemplate inboxNotifier;
    @Inject
    private NotifierTemplate notifier;
    @Inject
    private ProjectRepositoryTemplate<?> projectRepository;

    @Override
    public long newProjectSharingRequest(long actor, long experimentId, String downloadExperimentLink) {
        return sharingRequestManagementHelper.findOrCreate(actor, experimentId, downloadExperimentLink).getId();
    }

    @Override
    public void approveSharingProject(long actor, long project, long requester) {

        final UserTemplate projectCreator = checkPresence(userRepository.findOne(actor));
        final Map<Long, SharingManagementTemplate.Access> colleagues = newHashMap();
        colleagues.put(requester, SharingManagementTemplate.Access.READ);

        sharingManager.updateSharingPolicy(projectCreator.getId(), project, colleagues, new HashMap<Long, SharingManagementTemplate.Access>(), false);

        final ProjectSharingRequestTemplate resolvedRequest = sharingRequestRepository.findByRequesterAndProject(requester, project);

        final String projectName = projectRepository.findOne(resolvedRequest.getProjectId()).getName();
        inboxNotifier.notify(projectCreator.getId(), requester,
                "Your request for accessing the experiment data has been approved. " +
                        "Now the \"" + projectName + "\" project is shared to you."
        );

        notifier.projectSharingApproved(requester, projectName, resolvedRequest.getDownloadExperimentLinks());

        sharingRequestRepository.delete(resolvedRequest);

    }

    @Override
    public void refuseSharingProject(long actor, long project, long requester, String refuseComment) {
        final UserTemplate projectCreator = checkPresence(userRepository.findOne(actor));

        final ProjectSharingRequestTemplate resolvedRequest = sharingRequestRepository.findByRequesterAndProject(requester, project);
        final String projectName = projectRepository.findOne(resolvedRequest.getProjectId()).getName();
        notifier.projectSharingRejected(requester, projectName, refuseComment);

        inboxNotifier.notify(projectCreator.getId(), requester,
                "Unfortunately your request to access the experiment in the private project \"" + projectName + "\" has been rejected. " +
                        "Rejection comment: " + refuseComment
        );

        sharingRequestRepository.delete(resolvedRequest);

    }
}
