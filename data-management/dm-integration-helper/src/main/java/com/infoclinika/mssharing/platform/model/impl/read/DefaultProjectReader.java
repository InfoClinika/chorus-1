package com.infoclinika.mssharing.platform.model.impl.read;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import com.infoclinika.mssharing.platform.model.RuleValidator;
import com.infoclinika.mssharing.platform.model.helper.PagedItemsTransformerTemplate;
import com.infoclinika.mssharing.platform.model.helper.read.ProjectReaderHelper;
import com.infoclinika.mssharing.platform.model.impl.DefaultTransformingTemplate;
import com.infoclinika.mssharing.platform.model.read.Filter;
import com.infoclinika.mssharing.platform.model.read.ProjectReaderTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Comparator;
import java.util.SortedSet;

import static com.infoclinika.mssharing.platform.model.helper.PagedItemsTransformerTemplate.toPageRequest;

/**
 * @author : Alexander Serebriyan
 */
@Transactional(readOnly = true)
public abstract class DefaultProjectReader<PROJECT extends ProjectTemplate, PROJECT_LINE extends ProjectReaderTemplate.ProjectLineTemplate>
        implements ProjectReaderTemplate<PROJECT_LINE>, DefaultTransformingTemplate<PROJECT, PROJECT_LINE> {

    @Inject
    protected ProjectReaderHelper<PROJECT, PROJECT_LINE> projectReaderHelper;
    @Inject
    PagedItemsTransformerTemplate pagedItemsTransformerTemplate;
    @Inject
    private RuleValidator ruleValidator;

    @PostConstruct
    private void setup() {
        projectReaderHelper.setTransformer(new Function<PROJECT, PROJECT_LINE>() {
            @Nullable
            @Override
            public PROJECT_LINE apply(PROJECT input) {
                return transform(input);
            }
        });
    }

    protected Comparator<ProjectLineTemplate> comparator() {

        return new Comparator<ProjectLineTemplate>() {
            @Override
            public int compare(ProjectLineTemplate o1, ProjectLineTemplate o2) {
                final int result = o1.name.compareTo(o2.name);
                if (result != 0) {
                    return result;
                }
                return ((Long) (o1.id)).compareTo(o2.id);
            }
        };

    }

    @Override
    public PROJECT_LINE readProject(long userId, long projectID) {
        if (!ruleValidator.hasReadAccessOnProject(userId, projectID)) throw new AccessDenied("Project read restricted");
        return projectReaderHelper.readProject(projectID).transform();
    }

    @Override
    public SortedSet<PROJECT_LINE> readProjects(long actor, Filter genericFilter) {

        return projectReaderHelper.readProjectsFiltered(actor, genericFilter)
                .transform()
                .toSortedSet(comparator());
    }

    @Override
    public SortedSet<PROJECT_LINE> readProjectsAllowedForWriting(final long user) {

        return projectReaderHelper.readProjectsForWriting(user)
                .transform()
                .toSortedSet(comparator());
    }

    @Override
    public PagedItem<PROJECT_LINE> readProjects(long actor, Filter filter, PagedItemInfo pagedItemInfo) {

        final PageRequest pageRequest = toPageRequest(ProjectTemplate.class, pagedItemInfo);

        return projectReaderHelper.readProjects(actor, filter, pageRequest, pagedItemInfo.toFilterQuery())
                .transform();

    }

    @Override
    public PagedItem<PROJECT_LINE> readProjectsByLab(long actor, Long lab, PagedItemInfo pagedItemInfo) {

        final PageRequest pageRequest = toPageRequest(ProjectTemplate.class, pagedItemInfo);

        return projectReaderHelper.readProjectsByLab(actor, lab, pageRequest, pagedItemInfo.toFilterQuery())
                .transform();

    }

}
