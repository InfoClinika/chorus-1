package com.infoclinika.mssharing.model.internal.entity;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * @author Herman Zamula
 */
@Entity
public class ViewColumn extends AbstractPersistable<Long> {

    @ManyToOne
    private ColumnDefinition columnDefinition;
    private int orderNum;

    public ViewColumn() {
    }

    public ViewColumn(ColumnDefinition columnDefinition, int orderNum) {
        this.columnDefinition = columnDefinition;
        this.orderNum = orderNum;
    }

    public ColumnDefinition getColumnDefinition() {
        return columnDefinition;
    }

    public void setColumnDefinition(ColumnDefinition columnDefinition) {
        this.columnDefinition = columnDefinition;
    }

    public Integer getOrder() {
        return orderNum;
    }

    public void setOrder(int order) {
        this.orderNum = order;
    }
}
