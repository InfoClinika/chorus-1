package com.infoclinika.mssharing.wizard.upload.model;

import com.infoclinika.mssharing.dto.response.DictionaryDTO;

import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author timofey.kasyanov
 *         date:   28.01.14
 */
public class EditFileItem {
    private final String name;
    private DictionaryDTO specie;
    private String labels;

    public EditFileItem(String name) {
        this.name = name;
    }

    public EditFileItem(String name, DictionaryDTO specie, String labels) {
        this.name = name;
        this.specie = specie;
        this.labels = labels;
    }

    public String getName() {
        return name;
    }

    public DictionaryDTO getSpecie() {
        return specie;
    }

    public void setSpecie(DictionaryDTO specie) {
        this.specie = specie;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }
}
