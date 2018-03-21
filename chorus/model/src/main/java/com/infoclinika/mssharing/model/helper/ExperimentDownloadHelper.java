package com.infoclinika.mssharing.model.helper;

import com.infoclinika.mssharing.model.helper.items.ChorusExperimentDownloadData;
import com.infoclinika.mssharing.model.helper.items.ChorusFileData;
import com.infoclinika.mssharing.platform.model.helper.ExperimentDownloadHelperTemplate;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface ExperimentDownloadHelper extends ExperimentDownloadHelperTemplate<
        ExperimentDownloadHelperTemplate.ExperimentItemTemplate,
        ChorusExperimentDownloadData,
        ChorusFileData> {

}
