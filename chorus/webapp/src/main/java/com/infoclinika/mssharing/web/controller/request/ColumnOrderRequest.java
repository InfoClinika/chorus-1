package com.infoclinika.mssharing.web.controller.request;

import java.util.Set;

public class ColumnOrderRequest {

    public Set<OrderedColumnsRequest> columns;

    public String name;
    public boolean isPrimary;

    public ColumnOrderRequest() {
    }

    public static class OrderedColumnsRequest {
        public int order;
        public long columnId;
    }
}
