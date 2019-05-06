package com.infoclinika.mssharing.dto;


public class MatrixDto {

    private String[][] matrix;
    private String clazz;

    public MatrixDto(String[][] matrix, String clazz) {
        this.matrix = matrix;
        this.clazz = clazz;
    }

    public MatrixDto() {
    }

    public String[][] getMatrix() {
        return matrix;
    }

    public void setMatrix(String[][] matrix) {
        this.matrix = matrix;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }
}
