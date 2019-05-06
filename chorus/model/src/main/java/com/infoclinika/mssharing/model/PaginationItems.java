package com.infoclinika.mssharing.model;


import com.google.common.base.Optional;

import java.util.List;

public class PaginationItems {

    public static final class PagedItemInfo extends com.infoclinika.mssharing.platform.model.PagedItemInfo {

        public final Optional<AdvancedFilterQueryParams> advancedFilter;

        public PagedItemInfo(int items, int page, String sortingField, boolean sortingAsc, String filterQuery,
                             Optional<AdvancedFilterQueryParams> advancedFilter) {
            super(items, page, sortingField, sortingAsc, filterQuery);
            this.advancedFilter = advancedFilter;
        }
    }

    public static class PagedItem<T> {
        public final int totalPages;
        public final long itemsCount;
        public final List<T> items;
        public final int pageNumber;
        public final int pageSize;

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
                IS_ON, IS_AFTER, IS_ON_AND_AFTER, IS_ON_OR_BEFORE, IS_BEFORE, IS_TODAY, IS_YESTERDAY, IS_IN_WEEK,
                IS_IN, IS_NOT_IN;
            }
        }

        public AdvancedFilterQueryParams() {
        }

        public AdvancedFilterQueryParams(boolean conjunction, List<AdvancedFilterPredicateItem> predicates) {
            this.conjunction = conjunction;
            this.predicates = predicates;
        }
    }
}
