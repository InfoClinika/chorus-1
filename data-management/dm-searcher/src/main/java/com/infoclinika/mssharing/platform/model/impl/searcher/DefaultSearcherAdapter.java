package com.infoclinika.mssharing.platform.model.impl.searcher;

import com.infoclinika.mssharing.platform.entity.InstrumentTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;
import com.infoclinika.mssharing.platform.model.helper.read.ExperimentReaderHelper;
import com.infoclinika.mssharing.platform.model.helper.read.FileReaderHelper;
import com.infoclinika.mssharing.platform.model.helper.read.InstrumentReaderHelper;
import com.infoclinika.mssharing.platform.model.helper.read.ProjectReaderHelper;
import com.infoclinika.mssharing.platform.model.read.ExperimentReaderTemplate.ExperimentLineTemplate;
import com.infoclinika.mssharing.platform.model.read.FileReaderTemplate.FileLineTemplate;
import com.infoclinika.mssharing.platform.model.read.InstrumentReaderTemplate.InstrumentLineTemplate;
import com.infoclinika.mssharing.platform.model.read.ProjectReaderTemplate.ProjectLineTemplate;
import com.infoclinika.mssharing.platform.repository.InstrumentRepositoryTemplate.AccessedInstrument;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Herman Zamula
 */
@Component
public class DefaultSearcherAdapter extends DefaultSearcherTemplate
    <ProjectTemplate,
        ExperimentTemplate,
        FileMetaDataTemplate,
        InstrumentTemplate,
        ProjectLineTemplate,
        ExperimentLineTemplate,
        FileLineTemplate,
        InstrumentLineTemplate> {

    @Inject
    protected ProjectReaderHelper<ProjectTemplate, ProjectLineTemplate> projectReaderHelper;
    @Inject
    protected ExperimentReaderHelper<ExperimentTemplate, ExperimentLineTemplate> experimentReaderHelper;
    @Inject
    protected FileReaderHelper<FileMetaDataTemplate, FileLineTemplate> fileReaderHelper;
    @Inject
    protected InstrumentReaderHelper<InstrumentTemplate, InstrumentLineTemplate> instrumentReaderHelper;

    @Override
    protected ProjectLineTemplate transformProject(ProjectTemplate projectTemplate) {
        return projectReaderHelper.getDefaultTransformer().apply(projectTemplate);
    }

    @Override
    protected FileLineTemplate transformFile(FileMetaDataTemplate fileMetaDataTemplate) {
        return fileReaderHelper.getDefaultTransformer().apply(fileMetaDataTemplate);
    }

    @Override
    protected ExperimentLineTemplate transformExperiment(ExperimentTemplate experimentTemplate) {
        return experimentReaderHelper.getDefaultTransformer().apply(experimentTemplate);
    }

    @Override
    protected InstrumentLineTemplate transformInstrument(AccessedInstrument<InstrumentTemplate> instrumentTemplate) {
        return instrumentReaderHelper.getDefaultTransformer().apply(instrumentTemplate);
    }
}
