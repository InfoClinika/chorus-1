package com.infoclinika.mssharing.model.internal.read;

import com.google.common.collect.Lists;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveProject;
import com.infoclinika.mssharing.model.internal.entity.view.ProjectDashboardRecord;
import com.infoclinika.mssharing.model.internal.repository.ProjectRepository;
import com.infoclinika.mssharing.model.read.ProjectLine;
import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import com.infoclinika.mssharing.platform.model.impl.read.DefaultProjectReader;
import com.infoclinika.mssharing.platform.model.read.Filter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.google.common.collect.FluentIterable.from;

/**
 * @author : Alexander Serebriyan
 */
@Component
@Transactional(readOnly = true)
@Deprecated
public class ProjectDashboardRecordsReaderImpl extends DefaultProjectReader<ActiveProject, ProjectLine> {

    @Inject
    private Transformers transformers;

    @Inject
    private ProjectRepository projectRepository;

    @Inject
    private Transformers.PagedItemsTransformer pagedItemsTransformer;

    @Override
    public ProjectLine transform(ActiveProject activeProject) {
        return transformers.projectTransformer.apply(activeProject);
    }

    @Override
    public PagedItem<ProjectLine> readProjectsByLab(long actor, Long lab, PagedItemInfo pagedItemInfo) {
        final Page<ProjectDashboardRecord> records = projectRepository.findByLab(lab, pagedItemsTransformer.toFilterQuery(pagedItemInfo), pagedItemsTransformer.toPageRequest(ProjectDashboardRecord.class, pagedItemInfo));
        return getProjectLinePagedItem(records);
    }

    @Override
    public PagedItem<ProjectLine> readProjects(long actor, Filter genericFilter, PagedItemInfo pagedItemInfo) {
        Page<ProjectDashboardRecord> pagedProjects = filterPageableProject(actor, genericFilter, pagedItemInfo);
        return getProjectLinePagedItem(pagedProjects);
    }

    @Override
    public SortedSet<ProjectLine> readProjects(final long actor, Filter genericFilter) {
        final List<ProjectDashboardRecord> rawRecords;
        switch (genericFilter) {
            case ALL:
                rawRecords = projectRepository.findAllAvailableRecords(actor); break;
            case SHARED_WITH_ME:
                rawRecords = projectRepository.sharedProjects(actor); break;
            case MY:
                rawRecords = projectRepository.privateProjects(actor); break;
            case PUBLIC:
                rawRecords= projectRepository.publicProjectsNotOwned(actor); break;
            default:
                throw new IllegalArgumentException("Unknown filter: " + genericFilter);
        }
        final List<ProjectLine> transformed = Lists.transform(rawRecords, transformers.projectDashboardRecordTransformer);
        final TreeSet<ProjectLine> result = new TreeSet<>(comparator());
        result.addAll(transformed);
        return result;
    }

    private PagedItem<ProjectLine> getProjectLinePagedItem(Page<ProjectDashboardRecord> pagedFiles) {
        return new PagedItem<>(
                pagedFiles.getTotalPages(),
                pagedFiles.getTotalElements(),
                pagedFiles.getNumber(), pagedFiles.getSize(), from(pagedFiles.getContent())
                .transform(transformers.projectDashboardRecordTransformer)
                .toList()
        );
    }

    private Page<ProjectDashboardRecord> filterPageableProject(long user, Filter filter, PagedItemInfo pagedInfo) {
        Pageable request = pagedItemsTransformer.toPageRequest(ProjectDashboardRecord.class, pagedInfo);
        switch (filter) {
            case ALL:
                return projectRepository.findAllAvailableRecords(user, pagedItemsTransformer.toFilterQuery(pagedInfo), request);
            case SHARED_WITH_ME:
                return projectRepository.sharedProjects(user, request, pagedItemsTransformer.toFilterQuery(pagedInfo));
            case MY:
                return projectRepository.privateProjects(user, request, pagedItemsTransformer.toFilterQuery(pagedInfo));
            case PUBLIC:
                return projectRepository.publicProjectsRecordsNotOwned(user, pagedItemsTransformer.toFilterQuery(pagedInfo), request);
            default:
                throw new AssertionError(filter);
        }
    }
}
