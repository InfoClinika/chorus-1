package com.infoclinika.mssharing.dto;

import java.util.Set;
public class DataCubeRequest {
    public String path;
    public Set<String> matrix;
    public boolean readAllAnnotations;

    public DataCubeRequest(String path, Set<String> matrix, boolean requestAnnotations) {
        this.path = path;
        this.matrix = matrix;
        this.readAllAnnotations = requestAnnotations;
    }

    public DataCubeRequest(String path) {
        this.path = path;
    }

    public DataCubeRequest() {
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Set<String> getMatrix() {
        return matrix;
    }

    public void setMatrix(Set<String> matrix) {
        this.matrix = matrix;
    }

    public boolean isReadAllAnnotations() {
        return readAllAnnotations;
    }

    public void setReadAllAnnotations(boolean readAllAnnotations) {
        this.readAllAnnotations = readAllAnnotations;
    }
}
