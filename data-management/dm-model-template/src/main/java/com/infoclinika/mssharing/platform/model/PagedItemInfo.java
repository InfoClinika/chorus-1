package com.infoclinika.mssharing.platform.model;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Herman Zamula
 */
public class PagedItemInfo {
    public final int items;
    public final int page;
    public final String sortingField;
    public final boolean isSortingAsc;
    public final String filterQuery;

    public PagedItemInfo(int items, int page, String sortingField, boolean sortingAsc, String filterQuery) {
        this.items = items;
        this.page = page;
        this.sortingField = sortingField;
        isSortingAsc = sortingAsc;
        this.filterQuery = filterQuery;
    }

    public String toFilterQuery() {
        if (StringUtils.isEmpty(this.filterQuery)) {
            return "%";
        }
        return "%" + this.filterQuery + "%";
    }
}
