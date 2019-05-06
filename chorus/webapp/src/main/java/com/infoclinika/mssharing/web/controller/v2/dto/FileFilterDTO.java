package com.infoclinika.mssharing.web.controller.v2.dto;

import com.infoclinika.mssharing.model.PaginationItems.AdvancedFilterQueryParams;

/**
 * @author Vitalii Petkanych
 */
public class FileFilterDTO {

    private AdvancedFilterQueryParams filter;
    private String query;
    private String sortBy;
    private boolean sortAsc;
    private int page;
    private int pageSize;

    public FileFilterDTO() {
    }

    public AdvancedFilterQueryParams getFilter() {
        return filter;
    }

    public void setFilter(AdvancedFilterQueryParams filter) {
        this.filter = filter;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public boolean isSortAsc() {
        return sortAsc;
    }

    public void setSortAsc(boolean sortAsc) {
        this.sortAsc = sortAsc;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
