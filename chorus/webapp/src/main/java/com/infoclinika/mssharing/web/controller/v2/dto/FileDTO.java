package com.infoclinika.mssharing.web.controller.v2.dto;

/**
 * @author Vitalii Petkanych
 */
public class FileDTO {

    private final long id;
    private final String name;
    private final long date;
    private final long size;
    private final long specieId;
    private final String labels;

    public FileDTO(long id, String name, long date, long size, long specieId, String labels) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.size = size;
        this.specieId = specieId;
        this.labels = labels;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getDate() {
        return date;
    }

    public long getSize() {
        return size;
    }

    public long getSpecieId() {
        return specieId;
    }

    public String getLabels() {
        return labels;
    }
}
