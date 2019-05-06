package com.infoclinika.mssharing.platform.model.helper.read;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.GroupTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;
import com.infoclinika.mssharing.platform.repository.GroupRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.ProjectRepositoryTemplate;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static com.infoclinika.mssharing.platform.model.helper.read.ResultBuilder.builder;
import static com.infoclinika.mssharing.platform.model.read.GroupsReaderTemplate.GroupLine;

/**
 * @author Herman Zamula
 */
@Component
@Scope(value = "prototype")
public class GroupReaderHelper<G extends GroupTemplate, LINE extends GroupLine>
    extends AbstractReaderHelper<G, LINE, GroupLine> {

    @Inject
    private GroupRepositoryTemplate<G> groupRepository;

    @Inject
    private ProjectRepositoryTemplate<ProjectTemplate> projectRepository;

    public ResultBuilder<G, LINE> readGroups(long user, boolean includeAllUsers) {

        return builder(groupRepository
            .findByOwner(user, includeAllUsers), activeTransformer);
    }

    @Override
    public Function<G, GroupLine> getDefaultTransformer() {
        return input -> new GroupLine(
            input.getId(),
            input.getName(),
            input.getLastModification(),
            input.getNumberOfMembers(),
            projectRepository.findBySharedGroup(input.getId()).size()
        );
    }
}
