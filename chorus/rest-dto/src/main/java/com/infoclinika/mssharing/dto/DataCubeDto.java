package com.infoclinika.mssharing.dto;

import java.util.Map;

public class DataCubeDto {

    private Map<String, MatrixDto> sheetMatrixDto;
    private DataContainerDto rowsInfo;
    private DataContainerDto columnsInfo;

    public DataCubeDto(Map<String, MatrixDto> sheetMatrixDto, DataContainerDto rowsInfo, DataContainerDto columnsInfo) {
        this.sheetMatrixDto = sheetMatrixDto;
        this.rowsInfo = rowsInfo;
        this.columnsInfo = columnsInfo;
    }

    public DataCubeDto() {
    }

    public Map<String, MatrixDto> getSheetMatrixDto() {
        return sheetMatrixDto;
    }

    public void setSheetMatrixDto(Map<String, MatrixDto> sheetMatrixDto) {
        this.sheetMatrixDto = sheetMatrixDto;
    }

    public DataContainerDto getRowsInfo() {
        return rowsInfo;
    }

    public void setRowsInfo(DataContainerDto rowsInfo) {
        this.rowsInfo = rowsInfo;
    }

    public DataContainerDto getColumnsInfo() {
        return columnsInfo;
    }

    public void setColumnsInfo(DataContainerDto columnsInfo) {
        this.columnsInfo = columnsInfo;
    }
}
