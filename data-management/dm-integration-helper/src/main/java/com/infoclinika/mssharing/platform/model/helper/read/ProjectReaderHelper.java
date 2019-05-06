package com.infoclinika.mssharing.platform.model.helper.read;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;
import com.infoclinika.mssharing.platform.model.DefaultTransformers;
import com.infoclinika.mssharing.platform.model.read.Filter;
import com.infoclinika.mssharing.platform.model.read.ProjectReaderTemplate.ProjectLineTemplate;
import com.infoclinika.mssharing.platform.repository.ProjectRepositoryTemplate;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

import static com.google.common.collect.FluentIterable.from;

/**
 * @author : Alexander Serebriyan
 */
@Component
@Scope(value = "prototype")
public class ProjectReaderHelper<PROJECT extends ProjectTemplate, PROJECT_LINE extends ProjectLineTemplate>
    extends AbstractReaderHelper<PROJECT, PROJECT_LINE, ProjectLineTemplate> {

    @Inject
    private ProjectRepositoryTemplate<PROJECT> projectRepository;


    public SingleResultBuilder<PROJECT, PROJECT_LINE> readProject(long projectId) {
        PROJECT project = projectRepository.findOne(projectId);
        return SingleResultBuilder.builder(project, activeTransformer);
    }

    public ResultBuilder<PROJECT, PROJECT_LINE> readProjectsFiltered(long userId, Filter filter) {
        FluentIterable<PROJECT> projectsByFilter = getProjectsByFilter(userId, filter);
        return ResultBuilder.builder(projectsByFilter.toSet(), activeTransformer);
    }


    public PagedResultBuilder<PROJECT, PROJECT_LINE> readProjects(long actor, Filter genericFilter,
                                                                  PageRequest pageRequest, String s) {

        final Page<PROJECT> pagedProjectsByFilter = getPagedProjectsByFilter(actor, genericFilter, pageRequest, s);

        return PagedResultBuilder.builder(pagedProjectsByFilter, activeTransformer);

    }

    public PagedResultBuilder<PROJECT, PROJECT_LINE> readProjectsByLab(long actor, long lab, Pageable pageable,
                                                                       String filter) {
        Page<PROJECT> projects = projectRepository.findByLabAndName(lab, filter, pageable);
        return PagedResultBuilder.builder(projects, activeTransformer);
    }

    public ResultBuilder<PROJECT, PROJECT_LINE> readProjectsForWriting(long actor) {
        List<PROJECT> projects = projectRepository.findAllowedForWriting(actor);
        return ResultBuilder.builder(projects, activeTransformer);
    }

    private FluentIterable<PROJECT> getProjectsByFilter(long actor, Filter filter) {
        switch (filter) {
            case ALL:
                return from(projectRepository.findAllAvailable(actor));
            case MY:
                return from(projectRepository.findMy(actor));
            case SHARED_WITH_ME:
                return from(projectRepository.findSharedNotOwned(actor));
            case PUBLIC:
                return from(projectRepository.findPublicNotOwned(actor));
            default:
                throw new AssertionError("Unknown filter for project: " + filter);
        }
    }

    private Page<PROJECT> getPagedProjectsByFilter(long actor, Filter filter, PageRequest pageRequest, String query) {
        switch (filter) {
            case ALL:
                return projectRepository.findAllAvailable(actor, query, pageRequest);
            case MY:
                return projectRepository.findMy(actor, query, pageRequest);
            case SHARED_WITH_ME:
                return projectRepository.findSharedNotOwned(actor, query, pageRequest);
            case PUBLIC:
                return projectRepository.findPublicNotOwned(actor, query, pageRequest);
            default:
                throw new AssertionError("Unknown filter for project: " + filter);
        }
    }

    @Override
    public Function<PROJECT, ProjectLineTemplate> getDefaultTransformer() {

        return input -> new ProjectLineTemplate(
            input.getId(),
            input.getName(),
            input.getLastModification(),
            input.getAreaOfResearch(),
            input.getCreator().getEmail(),
            DefaultTransformers.fromSharingType(input.getSharing().getType()),
            DefaultTransformers.labLineTemplateTransformer().apply(input.getLab()),
            input.getCreator().getFullName()
        );

    }

}
