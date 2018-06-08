package com.infoclinika.mssharing.model.read.dto.details;
import com.infoclinika.mssharing.model.helper.ProcessingFileItem;

import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;


public class ProcessingRunItem {

    private Long id;
    private String name;
    private Date date;
    private List<ProcessingFileItem> processingFileItems = newArrayList();


    public ProcessingRunItem(Long id, String name, List<ProcessingFileItem> processingFileItems, Date date) {
        this.id = id;
        this.name = name;
        this.processingFileItems.addAll(processingFileItems);
        this.date = date;
    }


    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<ProcessingFileItem> getProcessingFileItems() {
        return processingFileItems;
    }

    public Date getDate() {
        return date;
    }
}
