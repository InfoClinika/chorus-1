package com.infoclinika.mssharing.model.internal.entity;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
/**
 * @author Herman Zamula
 */
@Entity
public class ColumnDefinition extends AbstractPersistable<Long>{
    private String name;

    private ColumnsView.Type type;

    private String dataType;

    private boolean sortable;

    private boolean hideable;

    private int units;

    public ColumnDefinition() {}

    public ColumnDefinition(String name, ColumnsView.Type type, String dataType, boolean sortable, boolean hideable, int units) {
        this.name = name;
        this.type = type;
        this.dataType = dataType;
        this.sortable = sortable;
        this.hideable = hideable;
        this.units = units;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ColumnsView.Type getType() {
        return type;
    }

    public void setType(ColumnsView.Type type) {
        this.type = type;
    }

    public boolean isHideable() {
        return hideable;
    }

    public void setHideable(boolean hideable) {
        this.hideable = hideable;
    }

    public boolean isSortable() {
        return sortable;
    }

    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public int getUnits() {
        return units;
    }

    public void setUnits(int units) {
        this.units = units;
    }
}
