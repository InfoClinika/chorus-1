package com.infoclinika.mssharing.model.internal.read;

import com.google.common.base.Function;
import com.infoclinika.mssharing.model.PaginationItems;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.repository.FileMetaDataRepository;
import com.infoclinika.mssharing.model.internal.repository.UserRepository;
import com.infoclinika.mssharing.model.read.FileLine;
import com.infoclinika.mssharing.platform.entity.ExperimentFileTemplate;
import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import com.infoclinika.mssharing.platform.model.impl.read.DefaultFileReader;
import com.infoclinika.mssharing.platform.model.read.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Set;

import static com.google.common.collect.FluentIterable.from;
import static com.infoclinika.mssharing.model.internal.read.AdvancedFilterCreationHelper.*;
import static com.infoclinika.mssharing.model.internal.read.Transformers.PagedItemsTransformer.toFilterQuery;
import static com.infoclinika.mssharing.platform.model.helper.read.PagedResultBuilder.builder;

/**
 * @author Herman Zamula
 */
@Service
@Transactional(readOnly = true)
public class FileReaderImpl extends DefaultFileReader<ActiveFileMetaData, FileLine> {

    @SuppressWarnings("unused")
    private final Logger LOGGER = LoggerFactory.getLogger(FileReaderImpl.class);

    @Inject
    private Transformers transformers;

    @Inject
    private FileMetaDataRepository fileMetaDataRepository;

    @PersistenceContext(unitName = "mssharing")
    private EntityManager em;

    @Inject
    private UserRepository userRepository;

    @Override
    public FileLine transform(ActiveFileMetaData input) {
        throw new IllegalStateException("This method should not be called! Use customTransformerWithUser instead.");
    }

    public Function<ActiveFileMetaData, FileLine> customTransformerWithUser(long actor) {
        return transformers.transformToFileLineFunction(actor);
    }

    @Override
    public PagedItem<FileLine> readFilesByExperiment(long actor, long experiment, PagedItemInfo pagedItemInfo) {

        beforeReadFilesByExperiment(actor, experiment);

        final PaginationItems.PagedItemInfo pageInfo = (PaginationItems.PagedItemInfo) pagedItemInfo;

        final Pageable request;

        final Page<ActiveFileMetaData> files;
        final User userEntity = userRepository.findOne(actor);

        if (!pageInfo.advancedFilter.isPresent()) {

            request = pagedItemsTransformer.toPageRequest(ExperimentFileTemplate.class, pageInfo);
            files = fileMetaDataRepository.findByExperiment(experiment, request, toFilterQuery(pageInfo));

        } else {

            //TODO: Write tests on this condition
            request = pagedItemsTransformer.toPageRequest(ActiveFileMetaData.class, pageInfo);
            final String predicatesQueryString = getAdvancedFilterQueryStringWithCondition(ActiveFileMetaData.class, pageInfo);
            final String orderingString = getOrderingString(ActiveFileMetaData.class, request);

            final Query query = em.createQuery(FileMetaDataRepository.FIND_BY_EXPERIMENT_WITH_ADVANCED_FILTER + predicatesQueryString + orderingString);
            final Query countQuery = em.createQuery(FileMetaDataRepository.COUNT_BY_EXPERIMENT_WITH_ADVANCED_FILTER + predicatesQueryString + orderingString);
            query.setParameter("experiment", experiment);
            countQuery.setParameter("experiment", experiment);

            files = getPageOfItemsByQuery(request, query, countQuery);

        }

        return toResult(userEntity, files);

    }

    @Override
    public PagedItem<FileLine> readFiles(long actor, Filter filter, PagedItemInfo pagedItemInfo) {
        long ts1 = System.currentTimeMillis();
        final User userEntity = userRepository.findOne(actor);
        Page<ActiveFileMetaData> result = filterPageableFile(actor, filter, (PaginationItems.PagedItemInfo) pagedItemInfo);
        long ts2 = System.currentTimeMillis();
        LOGGER.debug("*** Result retrieved in " + (ts2 - ts1));
        long ts3 = System.currentTimeMillis();
        PagedItem<FileLine> transformed = toResult(userEntity, result);
        LOGGER.debug("*** Transformed in " + (System.currentTimeMillis() - ts3));
        return transformed;
    }

    @Override
    public Set<FileLine> readFiles(long actor, Filter genericFilter) {

        return fileReaderHelper.filesByFilter(actor, genericFilter)
                .transform(customTransformerWithUser(actor))
                .toSortedSet(fileComparator());

    }

    @Override
    public Set<FileLine> readUnfinishedFiles(long user) {

        return fileReaderHelper
                .readUnfinishedFilesByUser(user)
                .transform(customTransformerWithUser(user))
                .toSortedSet(fileComparator());

    }

    @Override
    public Set<FileLine> readFilesByInstrument(long actor, long instrument) {

        return fileReaderHelper
                .readFilesByInstrument(actor, instrument)
                .transform(customTransformerWithUser(actor))
                .toSortedSet(fileComparator());
    }

    @Override
    public Set<FileLine> readByNameForInstrument(long actor, long instrument, String fileName) {
        return fileReaderHelper
                .readByNameForInstrument(actor, instrument, fileName)
                .transform(customTransformerWithUser(actor))
                .toSortedSet(fileComparator());
    }

    @Override
    public Set<FileLine> readFilesByLab(long userId, long labId) {

        return fileReaderHelper
                .readFilesByLab(userId, labId)
                .transform(customTransformerWithUser(userId))
                .toSortedSet(fileComparator());
    }

    @Override
    public Set<FileLine> readFilesByExperiment(long actor, long experiment) {

        beforeReadFilesByExperiment(actor, experiment);

        return fileReaderHelper
                .filesByExperiment(actor, experiment)
                .transform(customTransformerWithUser(actor))
                .toSortedSet(fileComparator());

    }

    @Override
    public PagedItem<FileLine> readFilesByLab(long userId, long labId, PagedItemInfo pagedItemInfo) {

        PaginationItems.PagedItemInfo pagedInfo = (PaginationItems.PagedItemInfo) pagedItemInfo;
        Pageable request = pagedItemsTransformer.toPageRequest(ActiveFileMetaData.class, pagedInfo);
        final User userEntity = userRepository.findOne(userId);

        final Page<ActiveFileMetaData> result;

        if (!pagedInfo.advancedFilter.isPresent()) {
            result = fileMetaDataRepository.findByLab(labId, userId, request, toFilterQuery(pagedInfo));
        } else {
            final String predicatesQueryString = getAdvancedFilterQueryStringWithCondition(ActiveFileMetaData.class, pagedInfo);
            final String orderingString = getOrderingString(ActiveFileMetaData.class, request);

            final Query query = em.createQuery(FileMetaDataRepository.FIND_BY_LAB_WITH_ADVANCED_FILTER + predicatesQueryString + orderingString);
            final Query countQuery = em.createQuery(FileMetaDataRepository.COUNT_BY_LAB_WITH_ADVANCED_FILTER + predicatesQueryString + orderingString);
            query.setParameter("lab", labId);
            countQuery.setParameter("lab", labId);
            query.setParameter("user", userId);
            countQuery.setParameter("user", userId);
            result = getPageOfItemsByQuery(request, query, countQuery);
        }

        return toResult(userEntity, result);

    }

    @Override
    public PagedItem<FileLine> readFilesByInstrument(long actor, long instrument, PagedItemInfo pagedItemInfo) {

        PaginationItems.PagedItemInfo pagedInfo = (PaginationItems.PagedItemInfo) pagedItemInfo;
        Pageable request = pagedItemsTransformer.toPageRequest(ActiveFileMetaData.class, pagedInfo);
        final User userEntity = userRepository.findOne(actor);

        final Page<ActiveFileMetaData> files;

        if (!pagedInfo.advancedFilter.isPresent()) {
            files = fileMetaDataRepository.findByInstrument(instrument, actor, request, toFilterQuery(pagedInfo));
        } else {
            final String predicatesQueryString = getAdvancedFilterQueryStringWithCondition(ActiveFileMetaData.class, pagedInfo);
            final String orderingString = getOrderingString(ActiveFileMetaData.class, request);

            final Query query = em.createQuery(FileMetaDataRepository.FIND_BY_INSTRUMENT_WITH_ADVANCED_FILTER + predicatesQueryString + orderingString);
            final Query countQuery = em.createQuery(FileMetaDataRepository.COUNT_BY_INSTRUMENT_WITH_ADVANCED_FILTER + predicatesQueryString + orderingString);
            query.setParameter("user", actor);
            countQuery.setParameter("user", actor);
            query.setParameter("instrument", instrument);
            countQuery.setParameter("instrument", instrument);
            files = getPageOfItemsByQuery(request, query, countQuery);
        }

        return toResult(userEntity, files);
    }

    private PagedItem<FileLine> toResult(User actor, Page<ActiveFileMetaData> result) {
        return builder(result, transformers.transformFilesFn(actor, result)).transform();
    }

    private Page<ActiveFileMetaData> filterPageableFile(long user, Filter filter, PaginationItems.PagedItemInfo pagedInfo) {

        Pageable request = pagedItemsTransformer.toPageRequest(ActiveFileMetaData.class, pagedInfo);

        if (!pagedInfo.advancedFilter.isPresent()) {

            switch (filter) {
                case ALL:
                    return fileMetaDataRepository.findAllStartingWith(user, toFilterQuery(pagedInfo), request);
                case SHARED_WITH_ME:
                    return fileMetaDataRepository.findShared(user, request, toFilterQuery(pagedInfo));
                case MY:
                    return fileMetaDataRepository.findMy(user, request, toFilterQuery(pagedInfo));
                case PUBLIC:
                    return fileMetaDataRepository.findPublic(toFilterQuery(pagedInfo), request);
                default:
                    throw new AssertionError(filter);
            }

        } else {
            final String predicatesQueryString = getAdvancedFilterQueryStringWithCondition(ActiveFileMetaData.class, pagedInfo);

            final String orderingString = getOrderingString(ActiveFileMetaData.class, request);

            final Query query;
            final Query countQuery;
            switch (filter) {
                case ALL:
                    query = em.createQuery(FileMetaDataRepository.FIND_ALL_STARTING_WITH_ADVANCED_FILTER + predicatesQueryString + orderingString);
                    countQuery = em.createQuery(FileMetaDataRepository.COUNT_ALL_STARTING_WITH_ADVANCED_FILTER + predicatesQueryString + orderingString);
                    query.setParameter("user", user);
                    countQuery.setParameter("user", user);
                    break;
                case SHARED_WITH_ME:
                    query = em.createQuery(FileMetaDataRepository.FIND_SHARED_ADVANCED_FILTER + predicatesQueryString + orderingString);
                    countQuery = em.createQuery(FileMetaDataRepository.COUNT_SHARED_ADVANCED_FILTER + predicatesQueryString + orderingString);
                    query.setParameter("user", user);
                    countQuery.setParameter("user", user);
                    break;
                case MY:
                    query = em.createQuery(FileMetaDataRepository.FIND_MY_WITH_ADVANCED_FILTER + predicatesQueryString + orderingString);
                    countQuery = em.createQuery(FileMetaDataRepository.COUNT_MY_WITH_ADVANCED_FILTER + predicatesQueryString + orderingString);
                    query.setParameter("user", user);
                    countQuery.setParameter("user", user);
                    break;
                case PUBLIC:
                    query = em.createQuery(FileMetaDataRepository.FIND_PUBLIC_WITH_ADVANCED_FILTER + predicatesQueryString + orderingString);
                    countQuery = em.createQuery(FileMetaDataRepository.COUNT_PUBLIC_WITH_ADVANCED_FILTER + predicatesQueryString + orderingString);
                    break;
                default:
                    throw new AssertionError(filter);
            }
            return getPageOfItemsByQuery(request, query, countQuery);
        }

    }


}
