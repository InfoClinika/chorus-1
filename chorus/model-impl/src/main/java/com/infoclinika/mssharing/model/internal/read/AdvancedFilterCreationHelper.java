package com.infoclinika.mssharing.model.internal.read;

import com.google.common.base.Optional;
import com.infoclinika.mssharing.model.internal.entity.RawFile;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.entity.view.ExperimentDashboardRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.persistence.Query;

import static com.infoclinika.mssharing.model.PaginationItems.*;
import static com.infoclinika.mssharing.model.PaginationItems.AdvancedFilterQueryParams.*;
import static com.infoclinika.mssharing.model.internal.read.Transformers.PagedItemsTransformer.resolvePredicateForAdvancedSearch;

/**
 * Created to contain all common functions to create advanced filter
 *
 * @author andrii.loboda
 */
public class AdvancedFilterCreationHelper {
    public static String getAdvancedFilterQueryStringWithCondition(
        Class entityClass,
        PagedItemInfo pagedInfo) {
        return getAdvancedFilterQueryString(entityClass, pagedInfo, Optional.of(" AND "));
    }

    public static String getAdvancedFilterQueryString(
        Class entityClass,
        PagedItemInfo pagedInfo,
        Optional<String> queryStartString) {

        AdvancedFilterQueryParams advancedFilter = pagedInfo.advancedFilter.get();
        final String startQueryString = queryStartString.isPresent() ? queryStartString.get() : "";
        StringBuilder predicatesQueryString = new StringBuilder(startQueryString + " (");
        boolean firstItemPassed = false;
        for (AdvancedFilterPredicateItem predicateItem :
            advancedFilter.predicates) {
            if (!firstItemPassed) {
                firstItemPassed = true;
            } else {
                predicatesQueryString.append((advancedFilter.conjunction) ? " AND " : " OR ");
            }

            predicatesQueryString.append(resolvePredicateForAdvancedSearch(entityClass, predicateItem));
        }
        predicatesQueryString.append(")");

        return predicatesQueryString.toString();
    }

    public static <T> Page<T> getPageOfItemsByQuery(Pageable request, Query query, Query countQuery) {
        query.setFirstResult(request.getPageNumber() * request.getPageSize());
        query.setMaxResults(request.getPageSize());
        return new PageImpl<T>(query.getResultList(), request, (Long) countQuery.getSingleResult());
    }

    public static String getOrderingString(Class entityClass, Pageable request) {
        final String entityAlias;
        if (entityClass.equals(ActiveFileMetaData.class)) {
            entityAlias = "f";
        } else if (entityClass.equals(ExperimentDashboardRecord.class)) {
            entityAlias = "e";
        } else if (entityClass.equals(RawFile.class)) {
            entityAlias = "rawFile";
        } else {
            throw new IllegalArgumentException("Can't find ordering alias for entity: " + entityClass);
        }
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" ORDER BY ");
        boolean firstElement = true;
        for (Sort.Order sortingOrder : request.getSort()) {
            if (!firstElement) {
                stringBuilder.append(" , ");
            }
            final String direction = (sortingOrder.getDirection() == Sort.Direction.ASC) ? "ASC" : "DESC";
            stringBuilder.append(entityAlias).append(".").append(sortingOrder.getProperty()).append(" ")
                .append(direction);
            firstElement = false;
        }

        return stringBuilder.toString();
    }
}
