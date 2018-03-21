package com.infoclinika.mssharing.model.internal.read;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import com.infoclinika.mssharing.model.PaginationItems;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.entity.Lab;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveExperiment;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveProject;
import com.infoclinika.mssharing.model.internal.entity.view.ExperimentDashboardRecord;
import com.infoclinika.mssharing.model.internal.repository.*;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.read.ExperimentLine;
import com.infoclinika.mssharing.platform.entity.EntityUtil;
import com.infoclinika.mssharing.platform.entity.Sharing;
import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import com.infoclinika.mssharing.platform.model.impl.read.DefaultExperimentReader;
import com.infoclinika.mssharing.platform.model.read.Filter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.FluentIterable.from;
import static com.infoclinika.mssharing.model.internal.read.AdvancedFilterCreationHelper.*;
import static com.infoclinika.mssharing.model.internal.read.Transformers.PagedItemsTransformer.toFilterQuery;
import static com.infoclinika.mssharing.platform.model.helper.read.PagedResultBuilder.builder;
import static com.infoclinika.mssharing.platform.model.helper.read.ResultBuilder.builder;

/**
 * @author : Alexander Serebriyan
 */
@Component("experimentReader")
@Transactional(readOnly = true)
public class ExperimentReaderImpl extends DefaultExperimentReader<ActiveExperiment, ExperimentLine> {

    @PersistenceContext(unitName = "mssharing")
    private EntityManager em;
    @Inject
    private ExperimentRepository experimentRepository;
    @Inject
    private Transformers transformers;
    @Inject
    private RuleValidator ruleValidator;
    @Inject
    private ProjectRepository projectRepository;
    @Inject
    private UserRepository userRepository;
    @Inject
    private FileMetaDataRepository fileMetaDataRepository;

    @Override
    public ExperimentLine transform(ActiveExperiment activeExperiment) {

        final Optional<Lab> labOpt = fromNullable(activeExperiment.getLab() == null ? activeExperiment.getBillLaboratory() : activeExperiment.getLab());

        //TODO: Set proper values
        return new ExperimentLine(experimentReaderHelper.getDefaultTransformer().apply(activeExperiment),
                false,
                transformers.getChartsLink(activeExperiment),
                false,
                false, false, false,
                false,
                false,
                0,
                labOpt.transform(EntityUtil.ENTITY_TO_ID).orNull(),
                new DashboardReader.ExperimentColumns(
                        activeExperiment.getName(),
                        activeExperiment.getCreator().getFullName(),
                        labOpt.isPresent() ? labOpt.get().getName() : "",
                        activeExperiment.getProject().getName(),
                        activeExperiment.getNumberOfFiles(),
                        activeExperiment.getLastModification())
        );
    }

    @Override
    public SortedSet<ExperimentLine> readExperiments(long actor, Filter filter) {

        //TODO: Method applies very complex operations. Check performance
        final ImmutableSet.Builder<ExperimentDashboardRecord> builder = ImmutableSet.builder();
        final User actorUser = userRepository.findOne(actor);
        final FluentIterable<ActiveProject> projects = from(projectRepository.findAllAvailable(actor));

        for (ActiveProject project : projects) {
            final FluentIterable<ExperimentDashboardRecord> filteredExperiments = filterExperimentsByProject(actorUser, project, filter);
            builder.addAll(filteredExperiments); 
        }
        
        return toResultSet(actor, builder.build());
        
    }

    private FluentIterable<ExperimentDashboardRecord> filterExperimentsByProject(User actor, ActiveProject project, Filter filter) {
        return from(experimentRepository.findDashboardItemsByProject(project))
                .filter(getFilteredExperimentDashboardRecords(project, actor, filter));
    }

    private Predicate<ExperimentDashboardRecord> getFilteredExperimentDashboardRecords(final ActiveProject project, final User actor, final Filter filter) {
        return new Predicate<ExperimentDashboardRecord>() {
            @Override
            public boolean apply(ExperimentDashboardRecord input) {
                switch (filter) {
                    case ALL:
                        return true;
                    case SHARED_WITH_ME:
                        return (project.getSharing().getType() == Sharing.Type.SHARED && !input.getCreator().equals(actor)) &&
                                (project.getSharing().getAllCollaborators().keySet().contains(actor) || project.getCreator().equals(actor));
                    case PUBLIC:
                        return !input.getCreator().equals(actor) && project.getSharing().getType() == Sharing.Type.PUBLIC;
                    case MY:
                        return input.getCreator().equals(actor);
                    default:
                        throw new AssertionError(filter);
                }
            }
        };
    }

    @Override
    public SortedSet<ExperimentLine> readExperimentsByProject(long actor, long projectId) {
        
        final List<ExperimentDashboardRecord> dashboardRecords = experimentRepository.findRecordsByProject(projectId);
        
        return toResultSet(actor, dashboardRecords);
        
    }

    private ImmutableSortedSet<ExperimentLine> toResultSet(long actor, Iterable<ExperimentDashboardRecord> dashboardRecords) {
        
        return builder(dashboardRecords, transformers.experimentLineTransformerFn(actor, dashboardRecords))
                .transform()
                .toSortedSet(comparator());
        
    }
    

    @Override
    public PagedItem<ExperimentLine> readExperiments(long actor, Filter filter, PagedItemInfo pagedItemInfo) {
        return toResult(actor, filterPageableExperiment(actor, filter, (PaginationItems.PagedItemInfo) pagedItemInfo));
    }

    @Override
    public PagedItem<ExperimentLine> readExperimentsByLab(long actor, long labId, PagedItemInfo pagedItemInfo) {
        final Page<ExperimentDashboardRecord> experiments;

        PaginationItems.PagedItemInfo pageInfo = (PaginationItems.PagedItemInfo) pagedItemInfo;
        final PageRequest pageRequest = pagedItemsTransformer.toPageRequest(ExperimentDashboardRecord.class, pagedItemInfo);

        if (pageInfo.advancedFilter.isPresent()) {

            final String predicatesQueryString = getAdvancedFilterQueryStringWithCondition(ExperimentDashboardRecord.class, pageInfo);
            final String orderingString = getOrderingString(ExperimentDashboardRecord.class, pageRequest);

            final Query query = em.createQuery(ExperimentRepository.FIND_ALL_BY_LAB_WITH_ADVANCED_FILTER + predicatesQueryString + orderingString);
            final Query countQuery = em.createQuery(ExperimentRepository.COUNT_ALL_BY_LAB_WITH_ADVANCED_FILTER + predicatesQueryString + orderingString);
            query.setParameter("lab", labId);
            countQuery.setParameter("lab", labId);

            experiments = getPageOfItemsByQuery(pageRequest, query, countQuery);

        } else {

            experiments = experimentRepository.findAllRecordsByLab(labId, pagedItemsTransformer.toPageRequest(ExperimentDashboardRecord.class, pagedItemInfo), pagedItemsTransformer.toFilterQuery(pagedItemInfo));

        }

        return toResult(actor, experiments);

    }

    @Override
    public PagedItem<ExperimentLine> readPagedExperimentsByProject(long actor, long projectId, PagedItemInfo pageInfo) {

        ActiveProject project = projectRepository.findOne(projectId);

        PaginationItems.PagedItemInfo pagedItemInfo = (PaginationItems.PagedItemInfo) pageInfo;
        final PageRequest pageRequest = pagedItemsTransformer.toPageRequest(ExperimentDashboardRecord.class, pagedItemInfo);
        final Page<ExperimentDashboardRecord> experiments;
        if (!pagedItemInfo.advancedFilter.isPresent()) {
            experiments = experimentRepository.findByProject(project, pageRequest, toFilterQuery(pagedItemInfo));

        } else {
            final String predicatesQueryString = getAdvancedFilterQueryStringWithCondition(ExperimentDashboardRecord.class, pagedItemInfo);
            final String orderingString = getOrderingString(ExperimentDashboardRecord.class, pageRequest);

            final Query query = em.createQuery(ExperimentRepository.FIND_BY_PROJECT_WITH_ADVANCED_FILTER + predicatesQueryString + orderingString);
            final Query countQuery = em.createQuery(ExperimentRepository.COUNT_BY_PROJECT_WITH_ADVANCED_FILTER + predicatesQueryString + orderingString);
            query.setParameter("project", project);
            countQuery.setParameter("project", project);
            experiments = getPageOfItemsByQuery(pageRequest, query, countQuery);
        }

        return toResult(actor, experiments);

    }

    private PagedItem<ExperimentLine> toResult(long actor, Page<ExperimentDashboardRecord> result) {
        return builder(result, transformers.experimentLineTransformerFn(actor, result.getContent())).transform();
    }

    private Page<ExperimentDashboardRecord> filterPageableExperiment(long user, Filter
            filter, PaginationItems.PagedItemInfo pagedInfo) {

        Pageable request = pagedItemsTransformer.toPageRequest(ExperimentDashboardRecord.class, pagedInfo);
        if (!pagedInfo.advancedFilter.isPresent()) {
            switch (filter) {
                case ALL:
                    return experimentRepository.findAllAvailable(user, request, toFilterQuery(pagedInfo));
                case SHARED_WITH_ME:
                    return experimentRepository.findShared(user, request, toFilterQuery(pagedInfo));
                case MY:
                    return experimentRepository.findMy(user, request, toFilterQuery(pagedInfo));
                case PUBLIC:
                    return experimentRepository.findPublicRecords(toFilterQuery(pagedInfo), request);
                default:
                    throw new AssertionError(filter);
            }
        } else {
            final String predicatesQueryString = getAdvancedFilterQueryStringWithCondition(ExperimentDashboardRecord.class, pagedInfo);
            final String orderingString = getOrderingString(ExperimentDashboardRecord.class, request);

            final Query query;
            final Query countQuery;
            switch (filter) {
                case ALL:
                    //findAllAvailableRecords but with advancedFiltering
                    query = em.createQuery(ExperimentRepository.FIND_ALL_AVAILABLE_WITH_ADVANCED_FILTER + predicatesQueryString + orderingString);
                    countQuery = em.createQuery(ExperimentRepository.COUNT_ALL_AVAILABLE_WITH_ADVANCED_FILTER + predicatesQueryString + orderingString);
                    query.setParameter("user", user);
                    countQuery.setParameter("user", user);
                    break;
                case SHARED_WITH_ME:
                    query = em.createQuery(ExperimentRepository.FIND_SHARED_WITH_ADVANCED_FILTER + predicatesQueryString + orderingString);
                    countQuery = em.createQuery(ExperimentRepository.COUNT_SHARED_WITH_ADVANCED_FILTER + predicatesQueryString + orderingString);
                    query.setParameter("user", user);
                    countQuery.setParameter("user", user);
                    break;
                case MY:
                    query = em.createQuery(ExperimentRepository.FIND_MY_WITH_ADVANCED_FILTER + predicatesQueryString + orderingString);
                    countQuery = em.createQuery(ExperimentRepository.COUNT_MY_WITH_ADVANCED_FILTER + predicatesQueryString + orderingString);
                    query.setParameter("user", user);
                    countQuery.setParameter("user", user);
                    break;
                case PUBLIC:
                    query = em.createQuery(ExperimentRepository.FIND_PUBLIC_WITH_ADVANCED_FILTER + predicatesQueryString + orderingString);
                    countQuery = em.createQuery(ExperimentRepository.COUNT_PUBLIC_WITH_ADVANCED_FILTER + predicatesQueryString + orderingString);
                    break;
                default:
                    throw new AssertionError(filter);
            }
            return getPageOfItemsByQuery(request, query, countQuery);
        }
    }
}
