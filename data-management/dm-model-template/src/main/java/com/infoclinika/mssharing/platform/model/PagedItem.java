package com.infoclinika.mssharing.platform.model;

import java.util.Iterator;
import java.util.List;

/**
 * @author Herman Zamula
 */
public class PagedItem<T> implements Iterable<T> {
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


    @Override
    public Iterator<T> iterator() {
        return this.items.iterator();
    }
}
