package com.infoclinika.mssharing.dto;

public class ColumnDto {
    String name;
    String clazz;
    String[] values;

    public ColumnDto(String name, String clazz, String[] values) {
        this.name = name;
        this.clazz = clazz;
        this.values = values;
    }

    public ColumnDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String[] values) {
        this.values = values;
    }
}
