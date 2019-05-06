package com.infoclinika.mssharing.web.controller;

import com.google.common.base.Optional;
import com.infoclinika.mssharing.model.PaginationItems;
import com.infoclinika.mssharing.model.PaginationItems.AdvancedFilterQueryParams;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import com.infoclinika.mssharing.web.controller.request.PageRequest;

import javax.annotation.Nullable;
import java.util.Collections;

import static com.infoclinika.mssharing.model.PaginationItems.AdvancedFilterQueryParams.AdvancedFilterPredicateItem.AdvancedFilterOperator.EQUAL;


public class PagedItemsController extends ErrorHandler {

    public PagedItemsController() {
    }

    protected PagedItemInfo createPagedInfo(int page, int items, String sortingField, boolean asc, String filterQuery) {
        int pageZeroBased = page - 1;
        return new PaginationItems.PagedItemInfo(
            items,
            pageZeroBased,
            sortingField,
            asc,
            filterQuery,
            Optional.<AdvancedFilterQueryParams>absent()
        );
    }

    protected PagedItemInfo createPagedInfo(
        int page,
        int items,
        String sortingField,
        boolean asc,
        String filterQuery,
        @Nullable AdvancedFilterQueryParams advancedFilter
    ) {
        int pageZeroBased = page - 1;
        return new PaginationItems.PagedItemInfo(
            items,
            pageZeroBased,
            sortingField,
            asc,
            filterQuery,
            Optional.fromNullable(advancedFilter)
        );
    }

    protected PagedItemInfo createPagedInfo(final PageRequest request) {
        int pageZeroBased = request.page - 1;
        return new PaginationItems.PagedItemInfo(
            request.items,
            pageZeroBased,
            request.sortingField,
            request.asc,
            request.filterQuery,
            Optional.<AdvancedFilterQueryParams>absent()
        );
    }

    protected PagedItemInfo createPagedInfoByStudyType(
        int page,
        int items,
        String sortingField,
        boolean asc,
        String studyType,
        String filterQuery,
        @Nullable AdvancedFilterQueryParams advancedFilter
    ) {
        int pageZeroBased = page - 1;
        if (advancedFilter == null) {
            advancedFilter = studyTypeAdvancedFilterQueryParams(studyType);
        } else {
            advancedFilter.predicates.add(studyTypePredicateItem(studyType));
        }
        return new PaginationItems.PagedItemInfo(
            items,
            pageZeroBased,
            sortingField,
            asc,
            filterQuery,
            Optional.of(advancedFilter)
        );
    }

    private PaginationItems.AdvancedFilterQueryParams studyTypeAdvancedFilterQueryParams(String technologyType) {
        return new PaginationItems.AdvancedFilterQueryParams(
            true,
            Collections.singletonList(studyTypePredicateItem(technologyType))
        );
    }

    private AdvancedFilterQueryParams.AdvancedFilterPredicateItem studyTypePredicateItem(String studyType) {
        return new AdvancedFilterQueryParams.AdvancedFilterPredicateItem("studyType", studyType.toUpperCase(), EQUAL);
    }
}
