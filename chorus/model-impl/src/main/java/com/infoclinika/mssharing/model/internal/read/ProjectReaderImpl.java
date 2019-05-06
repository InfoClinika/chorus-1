package com.infoclinika.mssharing.model.internal.read;

import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveProject;
import com.infoclinika.mssharing.model.read.ProjectLine;
import com.infoclinika.mssharing.platform.model.impl.read.DefaultProjectReader;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Herman Zamula
 */
@Component
public class ProjectReaderImpl extends DefaultProjectReader<ActiveProject, ProjectLine> {

    @Inject
    private Transformers transformers;

    @Override
    public ProjectLine transform(ActiveProject activeProject) {
        return transformers.projectTransformer.apply(activeProject);
    }
}
