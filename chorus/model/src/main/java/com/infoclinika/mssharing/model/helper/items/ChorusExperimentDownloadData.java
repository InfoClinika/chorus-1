package com.infoclinika.mssharing.model.helper.items;

import com.google.common.base.Optional;
import com.infoclinika.mssharing.platform.model.helper.ExperimentDownloadHelperTemplate;
import com.infoclinika.mssharing.platform.model.read.AccessLevel;

import java.util.List;

/**
* @author Herman Zamula
*/
public class ChorusExperimentDownloadData extends ExperimentDownloadHelperTemplate.ExperimentDownloadDataTemplate {

    public List<LockMzData> lockMzs;
    public final AccessLevel accessLevel;
    //All experiments must specify the laboratory to send billing. For old experiments bill lab may not present in experiment
    public final Optional<Long> billLaboratory;

    public ChorusExperimentDownloadData(long creatorId,
                                        String name,
                                        String description,
                                        String projectName,
                                        String specie,
                                        String experimentType,

                                        boolean allow2dLc,
                                        String instrumentName,
                                        List<LockMzData> lockMzs,
                                        List<ExperimentDownloadHelperTemplate.AttachmentDataTemplate> attachments,
                                        List<ExperimentDownloadHelperTemplate.FileDataTemplate> files,
                                        AccessLevel accessLevel, Optional<Long> billLaboratory) {

        super(creatorId, name, description, projectName, specie, experimentType, allow2dLc, instrumentName, attachments, files);

        this.lockMzs = lockMzs;
        this.accessLevel = accessLevel;
        this.billLaboratory = billLaboratory;
    }
}
