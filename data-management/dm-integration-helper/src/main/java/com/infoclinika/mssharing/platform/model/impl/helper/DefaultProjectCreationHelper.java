package com.infoclinika.mssharing.platform.model.impl.helper;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;
import com.infoclinika.mssharing.platform.model.common.items.NamedItem;
import com.infoclinika.mssharing.platform.model.helper.ProjectCreationHelperTemplate;
import com.infoclinika.mssharing.platform.repository.ProjectRepositoryTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Set;

import static com.google.common.collect.FluentIterable.from;

/**
 * @author Herman Zamula
 */
@Transactional(readOnly = true)
public abstract class DefaultProjectCreationHelper implements ProjectCreationHelperTemplate {
    @Inject
    protected ProjectRepositoryTemplate projectRepositoryTemplate;

    @Override
    public Set<NamedItem> ownedProjects(long actor) {

        //noinspection unchecked
        return from(projectRepositoryTemplate.findMy(actor))
                .transform(new Function<ProjectTemplate, NamedItem>() {
                    @Override
                    public NamedItem apply(ProjectTemplate input) {
                        return new NamedItem(input.getId(), input.getName());
                    }
                })
                .toSet();
    }
}
