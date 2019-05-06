package com.infoclinika.mssharing.platform.model.impl.read.adapters;

import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;
import com.infoclinika.mssharing.platform.model.impl.read.DefaultProjectReader;
import com.infoclinika.mssharing.platform.model.read.ProjectReaderTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Herman Zamula
 */
@Component
public class DefaultProjectReaderAdapter
    extends DefaultProjectReader<ProjectTemplate, ProjectReaderTemplate.ProjectLineTemplate> {
    @Override
    public ProjectLineTemplate transform(ProjectTemplate projectTemplate) {
        return projectReaderHelper.getDefaultTransformer().apply(projectTemplate);
    }
}
