package com.infoclinika.mssharing.platform.model.helper.read.details;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.platform.entity.Sharing;
import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;
import com.infoclinika.mssharing.platform.model.helper.read.AbstractReaderHelper;
import com.infoclinika.mssharing.platform.model.helper.read.SingleResultBuilder;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.ProjectItemTemplate;
import com.infoclinika.mssharing.platform.repository.ProjectRepositoryTemplate;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static com.google.common.collect.FluentIterable.from;
import static com.infoclinika.mssharing.platform.model.helper.read.SingleResultBuilder.builder;

/**
 * @author Herman Zamula
 */
@Component
@Scope(value = "prototype")
public class ProjectDetailsReaderHelper<PROJECT extends ProjectTemplate, PROJECT_ITEM extends ProjectItemTemplate>
    extends AbstractReaderHelper<PROJECT, PROJECT_ITEM, ProjectItemTemplate> {

    @Inject
    private ProjectRepositoryTemplate<PROJECT> projectRepository;
    @Inject
    private DetailsTransformersTemplate detailsTransformers;

    @Override
    public Function<PROJECT, ProjectItemTemplate> getDefaultTransformer() {
        return project -> {

            final ImmutableList<DetailsReaderTemplate.AttachmentItem> attachments = from(project.getAttachments())
                .transform(detailsTransformers.attachmentTransformer()).toList();

            final ImmutableSortedSet<DetailsReaderTemplate.SharedGroup> sharedGroups = from(project.getSharing()
                .getGroupsOfCollaborators().entrySet())
                .transform(detailsTransformers.groupAccessTransformer())
                .toSortedSet(detailsTransformers.namedItemComparator());

            final ImmutableSortedSet<DetailsReaderTemplate.SharedPerson> sharedPersons = from(project.getSharing()
                .getCollaborators().entrySet())
                .transform(detailsTransformers.sharedPersonAccessTransformer())
                .toSortedSet(detailsTransformers.namedItemComparator());

            return new ProjectItemTemplate(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getLastModification(),
                project.getSharing().getType() == Sharing.Type.PUBLIC,
                sharedGroups,
                (project.getLab() == null) ? null : project.getLab().getHead().getId(),
                project.getCreator().getEmail(),
                project.getSharing().getType() == Sharing.Type.PRIVATE,
                attachments,
                project.getSharing().getNumberOfAllCollaborators(),
                project.getAreaOfResearch(),
                sharedPersons,
                (project.getLab() == null) ? null : project.getLab().getId()
            );
        };
    }

    public SingleResultBuilder<PROJECT, PROJECT_ITEM> readProject(long id) {
        return builder(projectRepository.findOne(id), activeTransformer);
    }

}
