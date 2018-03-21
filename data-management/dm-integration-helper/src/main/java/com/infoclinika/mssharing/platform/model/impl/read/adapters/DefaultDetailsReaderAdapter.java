package com.infoclinika.mssharing.platform.model.impl.read.adapters;

import com.infoclinika.mssharing.platform.entity.GroupTemplate;
import com.infoclinika.mssharing.platform.entity.InstrumentTemplate;
import com.infoclinika.mssharing.platform.entity.LabTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;
import com.infoclinika.mssharing.platform.model.impl.read.DefaultDetailsReader;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.*;
import com.infoclinika.mssharing.platform.repository.InstrumentRepositoryTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Herman Zamula
 */
@Component
public class DefaultDetailsReaderAdapter extends DefaultDetailsReader<FileMetaDataTemplate,
        ProjectTemplate,
        ExperimentTemplate,
        InstrumentTemplate,
        LabTemplate,
        GroupTemplate,
        FileItemTemplate,
        ExperimentItemTemplate,
        ProjectItemTemplate,
        InstrumentItemTemplate,
        LabItemTemplateDetailed,
        GroupItemTemplate> {

    @Override
    public FileItemTemplate transformFile(FileMetaDataTemplate fileMetaDataTemplate) {
        return fileHelper.getDefaultTransformer().apply(fileMetaDataTemplate);
    }

    @Override
    public ProjectItemTemplate transformProject(ProjectTemplate projectTemplate) {
        return projectHelper.getDefaultTransformer().apply(projectTemplate);
    }

    @Override
    public ExperimentItemTemplate transformExperiment(ExperimentTemplate experimentTemplate) {
        return experimentHelper.getDefaultTransformer().apply(experimentTemplate);
    }

    @Override
    public InstrumentItemTemplate transformInstrument(InstrumentRepositoryTemplate.AccessedInstrument<InstrumentTemplate> instrumentTemplate) {
        return instrumentHelper.getDefaultTransformer().apply(instrumentTemplate);
    }

    @Override
    public LabItemTemplateDetailed transformLab(LabTemplate labTemplate) {
        return labHelper.getDefaultTransformer().apply(labTemplate);
    }

    @Override
    public GroupItemTemplate transformGroup(GroupTemplate groupTemplate) {
        return groupHelper.getDefaultTransformer().apply(groupTemplate);
    }
}
