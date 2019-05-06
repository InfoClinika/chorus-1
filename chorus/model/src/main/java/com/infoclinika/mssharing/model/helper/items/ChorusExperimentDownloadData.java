package com.infoclinika.mssharing.model.helper.items;

import com.infoclinika.mssharing.platform.model.helper.ExperimentDownloadHelperTemplate;

import java.util.List;

/**
 * @author Herman Zamula
 */
public class ChorusExperimentDownloadData extends ExperimentDownloadHelperTemplate.ExperimentDownloadDataTemplate {

    public ChorusExperimentDownloadData(List<ExperimentDownloadHelperTemplate.AttachmentDataTemplate> attachments,
                                        List<ExperimentDownloadHelperTemplate.FileDataTemplate> files) {
        super(attachments, files);
    }
}
