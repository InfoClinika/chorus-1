package com.infoclinika.mssharing.platform.model.helper.write;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.infoclinika.mssharing.platform.entity.ProjectSharingRequestTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;
import com.infoclinika.mssharing.platform.model.EntityFactories;
import com.infoclinika.mssharing.platform.model.NotifierTemplate;
import com.infoclinika.mssharing.platform.model.RequestsTemplate;
import com.infoclinika.mssharing.platform.repository.ExperimentRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.ProjectSharingRequestRepositoryTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Date;

/**
 * @author Herman Zamula
 */
@Component
public class ProjectSharingRequestManager<T extends ProjectSharingRequestTemplate> {

    @Inject
    private ProjectSharingRequestRepositoryTemplate<T> projectSharingRequestRepository;
    @Inject
    private RequestsTemplate requests;
    @Inject
    private ExperimentRepositoryTemplate<ExperimentTemplate> experimentRepository;
    @Inject
    private EntityFactories factories;
    @Inject
    private NotifierTemplate notifier;

    public T findOrCreate(final long requesterId, long experimentId, final String downloadExperimentLink) {


        final Optional<T> existingRequest = Optional.fromNullable(
            projectSharingRequestRepository.findByRequesterAndExperimentLink(requesterId, downloadExperimentLink));

        if (existingRequest.isPresent()) {
            return existingRequest.get();
        }

        final ExperimentTemplate experiment = experimentRepository.findOne(experimentId);
        final ProjectTemplate project = experiment.getProject();

        Optional<T> projectSharingRequest = Optional
            .fromNullable(projectSharingRequestRepository.findByRequesterAndProject(requesterId, project.getId()));
        //noinspection unchecked
        final T requestTemplate = projectSharingRequest.or((Supplier<T>) factories.projectSharingRequest);

        requestTemplate.setProjectId(project.getId());
        requestTemplate.setRequesterId(requesterId);
        requestTemplate.setExperimentId(experimentId);
        requestTemplate.setRequestDate(new Date());
        requestTemplate.getDownloadExperimentLinks().add(downloadExperimentLink);

        projectSharingRequestRepository.save(requestTemplate);

        requests.addOutboxItem(
            requesterId,
            experiment.getProject().getCreator().getFullName(),
            "You attempted to download an experiment and requested an access to the parent project \"" +
                experiment.getProject().getName() + "\"",
            new Date()
        );

        if (!projectSharingRequest.isPresent()) {
            notifier.sendProjectSharingRequestNotification(
                experiment.getProject().getCreator().getId(),
                requesterId,
                experiment.getProject().getId(), experimentId
            );
        }

        return requestTemplate;
    }

}
