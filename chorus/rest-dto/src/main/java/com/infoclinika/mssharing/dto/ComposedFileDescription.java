package com.infoclinika.mssharing.dto;

public class ComposedFileDescription {
    public String fileName;
    public boolean allRequiredFilesPresented;
    public FileDescription[] fileDescriptions;

    public ComposedFileDescription() {
    }

    public ComposedFileDescription(String fileName, boolean allRequiredFilesPresented,
                                   FileDescription[] fileDescriptions) {
        this.fileName = fileName;
        this.allRequiredFilesPresented = allRequiredFilesPresented;
        this.fileDescriptions = fileDescriptions;
    }
}
