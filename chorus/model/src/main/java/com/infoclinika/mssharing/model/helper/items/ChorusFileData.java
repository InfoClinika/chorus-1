package com.infoclinika.mssharing.model.helper.items;

import com.google.common.base.Optional;
import com.infoclinika.mssharing.platform.model.helper.ExperimentDownloadHelperTemplate;
import com.infoclinika.mssharing.platform.model.read.AccessLevel;

import java.util.List;

/**
 * @author Herman Zamula
 */
public class ChorusFileData extends ExperimentDownloadHelperTemplate.FileDataTemplate {

    public final String archiveId;
    public final Optional<Long> billLab;
    public final AccessLevel accessLevel;

    public ChorusFileData(String contentId, String archiveId, String name, boolean invalid, List<ExperimentDownloadHelperTemplate.ConditionDataTemplate> conditions, long lab, long id, Optional<Long> billLab, AccessLevel accessLevel) {
        super(id, contentId, name, invalid, conditions, lab);
        this.archiveId = archiveId;
        this.billLab = billLab;
        this.accessLevel = accessLevel;
    }
}
