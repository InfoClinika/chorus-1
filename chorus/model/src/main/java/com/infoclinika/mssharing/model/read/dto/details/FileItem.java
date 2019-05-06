package com.infoclinika.mssharing.model.read.dto.details;

import com.infoclinika.mssharing.model.helper.ExperimentPreparedSampleItem;
import com.infoclinika.mssharing.model.read.DashboardReader.StorageStatus;

import static com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.FileItemTemplate;

/**
 * @author Herman Zamula
 */
public class FileItem extends FileItemTemplate {
    public final String archiveId;
    public final StorageStatus storageStatus;
    public final int fractionNumber;
    public final Integer pairedEnd;
    public final ExperimentPreparedSampleItem preparedSample;

    public FileItem(FileItemTemplate fileItemTemplate, String archiveId, StorageStatus storageStatus,
                    Integer pairedEnd, int fractionNumber, ExperimentPreparedSampleItem preparedSample) {
        super(fileItemTemplate);
        this.archiveId = archiveId;
        this.storageStatus = storageStatus;
        this.pairedEnd = pairedEnd;
        this.fractionNumber = fractionNumber;
        this.preparedSample = preparedSample;
    }
}
