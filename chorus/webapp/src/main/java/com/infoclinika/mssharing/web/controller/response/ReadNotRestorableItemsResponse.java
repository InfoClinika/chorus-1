package com.infoclinika.mssharing.web.controller.response;

import com.infoclinika.mssharing.model.read.TrashReader;

import java.util.Set;

/**
 * @author Nikita Matrosov
 */
public class ReadNotRestorableItemsResponse {
    public final Set<TrashReader.TrashLineShort> projects;
    public final Set<TrashReader.TrashLineShort> experiments;
    public final Set<TrashReader.TrashLineShort> files;

    public ReadNotRestorableItemsResponse(
        Set<TrashReader.TrashLineShort> projects,
        Set<TrashReader.TrashLineShort> experiments,
        Set<TrashReader.TrashLineShort> files
    ) {
        this.projects = projects;
        this.experiments = experiments;
        this.files = files;
    }
}
