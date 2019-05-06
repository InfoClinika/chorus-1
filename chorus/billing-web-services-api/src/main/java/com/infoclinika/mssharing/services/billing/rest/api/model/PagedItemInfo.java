package com.infoclinika.mssharing.services.billing.rest.api.model;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author andrii.loboda
 */
public class PagedItemInfo {
    public int items;
    public int page;
    public String sortingField;
    public boolean isSortingAsc;
    public String filterQuery;
    @Nullable
    public AdvancedFilterQueryParams advancedFilter;

    public PagedItemInfo() {
    }

    public PagedItemInfo(int items, int page, String sortingField, boolean sortingAsc, String filterQuery,
                         @Nullable AdvancedFilterQueryParams advancedFilter) {
        this.items = items;
        this.page = page;
        this.sortingField = sortingField;
        isSortingAsc = sortingAsc;
        this.filterQuery = filterQuery;
        this.advancedFilter = advancedFilter;
    }


    public static class PagedItem<T> {
        public int totalPages;
        public long itemsCount;
        public List<T> items;
        public int pageNumber;
        public int pageSize;

        public PagedItem() {
        }

        public PagedItem(int totalPages, long itemsCount, int pageNumber, int size, List<T> items) {
            this.totalPages = totalPages;
            this.itemsCount = itemsCount;
            this.items = items;
            this.pageSize = size;
            this.pageNumber = pageNumber;
        }
    }

    public static class AdvancedFilterQueryParams {
        public boolean conjunction;
        public List<AdvancedFilterPredicateItem> predicates;

        public AdvancedFilterQueryParams(boolean conjunction, List<AdvancedFilterPredicateItem> predicates) {
            this.conjunction = conjunction;
            this.predicates = predicates;
        }

        public AdvancedFilterQueryParams() {
        }

        public static class AdvancedFilterPredicateItem {
            public String prop;
            public String value;
            public AdvancedFilterOperator operator;

            public AdvancedFilterPredicateItem(String prop, String value, AdvancedFilterOperator operator) {
                this.prop = prop;
                this.value = value;
                this.operator = operator;
            }

            public AdvancedFilterPredicateItem() {
            }

            public enum AdvancedFilterOperator {
                EQUAL, NOT_EQUAL, BEGINS_WITH, ENDS_WITH, CONTAINS, NOT_CONTAINS, IS_EMPTY, IS_NOT_EMPTY,
                GREATER_THAN, LESS_THAN,
                TRUE, FALSE,
                IS_ON, IS_AFTER, IS_ON_AND_AFTER, IS_ON_OR_BEFORE, IS_BEFORE, IS_TODAY, IS_YESTERDAY, IS_IN_WEEK
            }
        }
    }
}

