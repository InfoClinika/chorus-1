package com.infoclinika.mssharing.platform.model.impl.helper;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.platform.entity.GroupTemplate;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;
import com.infoclinika.mssharing.platform.model.helper.SharingProjectHelperTemplate;
import com.infoclinika.mssharing.platform.repository.ExperimentRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.GroupRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.UserRepositoryTemplate;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.FluentIterable.from;

/**
 * @author Herman Zamula
 */
@Transactional(readOnly = true)
public abstract class DefaultSharingProjectHelper<USER extends UserTemplate, GROUP extends GroupTemplate>
    implements SharingProjectHelperTemplate {

    private final Function<USER, UserDetails> userTransformer = input -> transformUser(input);
    private final Function<GROUP, GroupDetails> groupTransformer = input -> transformGroup(input);

    @Inject
    protected UserRepositoryTemplate<USER> userRepository;
    @Inject
    protected GroupRepositoryTemplate<GROUP> groupRepository;
    @Inject
    private ExperimentRepositoryTemplate<ExperimentTemplate> experimentRepository;

    @Override
    public List<UserDetails> getAvailableUsers() {

        final List<USER> all = userRepository.findAll();
        return from(all)
            .transform(userTransformer)
            .toList();

    }

    @Override
    public List<UserDetails> getAvailableUsersStartingWith(String query) {
        throw new NotImplementedException(
            "Method 'DefaultSharingProjectHelper#getAvailableUsersStartingWith(String query)' is not implemented yet"
        );
    }

    @Override
    public ImmutableSortedSet<GroupDetails> getAvailableGroups(long actor) {

        return from(groupRepository.findByOwner(actor, true))
            .transform(groupTransformer)
            .toSortedSet(Comparator.comparing(o -> o.name));

    }

    @Override
    public List<UserDetails> getCollaborators(long actor, long experiment) {

        final ProjectTemplate project = checkNotNull(experimentRepository.findOne(experiment)).getProject();
        //noinspection unchecked
        return from(project.getSharing().getAllCollaborators().keySet())
            .transform(userTransformer)
            .toList();

    }

    protected abstract UserDetails transformUser(USER user);

    protected abstract GroupDetails transformGroup(GROUP group);
}
