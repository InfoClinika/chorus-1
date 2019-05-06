package com.infoclinika.mssharing.web.controller.v2.dto;

import java.util.List;

/**
 * @author Vitalii Petkanych
 */
public class ItemBoxDTO<T> {
    private List<T> items;
    private long totalItems;

    public ItemBoxDTO(List<T> items, long totalItems) {
        this.items = items;
        this.totalItems = totalItems;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }
}
