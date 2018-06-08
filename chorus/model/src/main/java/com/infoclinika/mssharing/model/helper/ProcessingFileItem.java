package com.infoclinika.mssharing.model.helper;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class ProcessingFileItem {
    private Long id;
    private String name;
    private String filePath;

    List<String> experimentFiles = newArrayList();

    private List<String> experimentSampleItems = newArrayList();

    public ProcessingFileItem() {
    }

    public ProcessingFileItem(Long id, String name, String filePath, List<String> experimentSampleItems, List<String> experimentFiles) {
        this.id = id;
        this.name = name;
        this.filePath = filePath;
        this.experimentSampleItems.addAll(experimentSampleItems);
        this.experimentFiles.addAll(experimentFiles);
    }


    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFilePath() {
        return filePath;
    }

    public List<String> getExperimentFiles() {
        return experimentFiles;
    }

    public List<String> getExperimentSampleItems() {
        return experimentSampleItems;
    }
}
