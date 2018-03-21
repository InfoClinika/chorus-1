package com.infoclinika.mssharing.web.controller;

import com.google.common.base.Optional;
import com.infoclinika.mssharing.model.PaginationItems;
import com.infoclinika.mssharing.model.PaginationItems.AdvancedFilterQueryParams;
import com.infoclinika.mssharing.web.controller.request.PageRequest;

import javax.annotation.Nullable;


public class PagedItemsController extends ErrorHandler {

    public PagedItemsController() {
    }

    protected com.infoclinika.mssharing.platform.model.PagedItemInfo createPagedInfo(int page, int items, String sortingField, boolean asc, String filterQuery) {
        page = page - 1; //Requested pages started from 1
        return new PaginationItems.PagedItemInfo(items, page, sortingField, asc, filterQuery, Optional.<AdvancedFilterQueryParams>absent());
    }
    protected com.infoclinika.mssharing.platform.model.PagedItemInfo createPagedInfo(int page, int items, String sortingField, boolean asc, String filterQuery, @Nullable AdvancedFilterQueryParams advancedFilter) {
        page = page - 1; //Requested pages started from 1
        return new PaginationItems.PagedItemInfo(items, page, sortingField, asc, filterQuery, Optional.fromNullable(advancedFilter));
    }

    protected com.infoclinika.mssharing.platform.model.PagedItemInfo createPagedInfo(final PageRequest request) {
        int page = request.page - 1; //Requested pages started from 1
        return new PaginationItems.PagedItemInfo(request.items, page, request.sortingField, request.asc, request.filterQuery, Optional.<AdvancedFilterQueryParams>absent());
    }

}
