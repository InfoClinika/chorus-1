package com.infoclinika.mssharing.dto;


import java.util.Set;

public class DataCubeResult {
    private Set<String> sheetNames;
    private DataCubeDto dataCubeDto;

    public DataCubeResult(Set<String> sheetNames) {
        this.sheetNames = sheetNames;
    }

    public DataCubeResult(DataCubeDto dataCubeDto) {
        this.dataCubeDto = dataCubeDto;
    }

    public DataCubeResult() {
    }

    public Set<String> getSheetNames() {
        return sheetNames;
    }

    public void setSheetNames(Set<String> sheetNames) {
        this.sheetNames = sheetNames;
    }

    public DataCubeDto getDataCubeDto() {
        return dataCubeDto;
    }

    public void setDataCubeDto(DataCubeDto dataCubeDto) {
        this.dataCubeDto = dataCubeDto;
    }
}
