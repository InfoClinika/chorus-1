package com.infoclinika.mssharing.dto;

import java.util.List;

public class DataContainerDto {
    private List<ColumnDto> columns;

    public DataContainerDto(List<ColumnDto> columns) {
        this.columns = columns;
    }

    public DataContainerDto() {
    }

    public List<ColumnDto> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnDto> columns) {
        this.columns = columns;
    }

}
