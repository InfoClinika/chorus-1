package com.infoclinika.mssharing.platform.model.helper.write;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.infoclinika.mssharing.platform.entity.EntityUtil;
import com.infoclinika.mssharing.platform.entity.GroupTemplate;
import com.infoclinika.mssharing.platform.entity.Sharing;
import com.infoclinika.mssharing.platform.entity.Sharing.Access;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;
import com.infoclinika.mssharing.platform.model.EntityFactories;
import com.infoclinika.mssharing.platform.model.NotifierTemplate;
import com.infoclinika.mssharing.platform.model.impl.write.sharing.ProjectAccessChangedEventPublisher;
import com.infoclinika.mssharing.platform.model.write.SharingManagementTemplate;
import com.infoclinika.mssharing.platform.repository.ExperimentRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.GroupRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.ProjectRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.UserRepositoryTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.*;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.ImmutableMap.copyOf;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Maps.*;
import static com.google.common.collect.Sets.newHashSet;
import static com.infoclinika.mssharing.platform.model.write.SharingManagementTemplate.Access.WRITE;

/**
 * @author Herman Zamula
 */
@Component
public class SharingManager {
    @Inject
    private ProjectRepositoryTemplate<ProjectTemplate> projectRepository;
    @Inject
    private ExperimentRepositoryTemplate<ExperimentTemplate> experimentRepository;
    @Inject
    private GroupRepositoryTemplate<GroupTemplate> groupRepository;
    @Inject
    private NotifierTemplate notifier;
    @Inject
    private EntityFactories factories;
    @Inject
    private UserRepositoryTemplate<UserTemplate> userRepository;

    @Inject
    private ProjectAccessChangedEventPublisher accessChangedEventPublisher;

    @SuppressWarnings("unchecked")
    public GroupTemplate createGroup(long actor, String name, Iterable<Long> collaborators) {
        final UserTemplate userEntity = factories.userFromId.apply(actor);
        final GroupTemplate entity = factories.group.get();
        entity.setName(name);
        entity.setOwner(userEntity);
        entity.setCollaborators(newHashSet(transform(collaborators, factories.userFromId)));
        return saveGroup(entity);
    }

    public void editGroupName(long groupId, String newName) {
        final GroupTemplate group = groupRepository.findOne(groupId);
        group.name = newName;
        saveGroup(group);
    }

    public void removeGroup(long groupId) {
        groupRepository.delete(groupId);
    }

    @SuppressWarnings("unchecked")
    public void setCollaborators(long actor, long group, Iterable<Long> collaborators, boolean withEmailNotification) {
        final GroupTemplate entity = groupRepository.findOne(group);
        final Map<Long, Map<UserTemplate, Access>> oldProjectCollaborators = projectCollaborators(entity);
        entity.setCollaborators(newHashSet(transform(collaborators, factories.userFromId)));
        final GroupTemplate saved = saveGroup(entity);
        final Map<Long, Map<UserTemplate, Access>> newProjectCollaborators = projectCollaborators(saved);
        for (long project : Sets.union(oldProjectCollaborators.keySet(), newProjectCollaborators.keySet())) {
            processCollaboratorsChanging(
                actor,
                project,
                oldProjectCollaborators.get(project),
                newProjectCollaborators.get(project),
                withEmailNotification
            );
        }
    }

    @SuppressWarnings("unchecked")
    public Map<UserTemplate, Access> updateSharingPolicy(long actor, long projectId,
                                                                 Map<Long, SharingManagementTemplate.Access> colleagues,
                                                                 Map<Long, SharingManagementTemplate.Access> groups,
                                                                 boolean withEmailNotification) {

        Sharing.Type type;
        if (colleagues.isEmpty() && groups.isEmpty()) {
            type = Sharing.Type.PRIVATE;
        } else if (groups.containsKey(groupRepository.findAllUsersGroup().getId())) {
            type = Sharing.Type.PUBLIC;
        } else {
            type = Sharing.Type.SHARED;
        }

        final ProjectTemplate project = projectRepository.findOne(projectId);
        project.getSharing().setType(type);
        final Map<UserTemplate, Access> oldAllCollaborators = project.getSharing().getAllCollaborators();
        final Map<? extends UserTemplate, Access> collaborators =
            transformIdToEntity(colleagues, factories.userFromId);
        project.getSharing().setCollaborators(collaborators, project);
        final Map<? extends GroupTemplate, Access> groupsOfCollaborators =
            transformIdToEntity(groups, factories.groupFromId);
        project.getSharing().setGroupsOfCollaborators(groupsOfCollaborators, project);
        project.setLastModification(new Date());
        final ProjectTemplate saved = saveProject(project);
        updateExperimentSharing(saved);
        final Map<UserTemplate, Access> newAllCollaborators = saved.getSharing().getAllCollaborators();
        processCollaboratorsChanging(
            actor, project.getId(), oldAllCollaborators, newAllCollaborators, withEmailNotification);
        updateProjectAccessRecords(projectId, colleagues, groups);
        return newAllCollaborators;
    }

    @SuppressWarnings("unchecked")
    public void makeProjectPrivate(long actor, long projectId) {
        final ProjectTemplate project = projectRepository.findOne(projectId);
        final ImmutableMap<UserTemplate, Access> oldCollaborators =
            copyOf(project.getSharing().getAllCollaborators());
        project.getSharing().setType(Sharing.Type.PRIVATE);
        final ProjectTemplate saved = saveProject(project);
        updateExperimentSharing(project);
        final ImmutableMap<UserTemplate, Access> newCollaborators =
            copyOf(saved.getSharing().getAllCollaborators());
        processCollaboratorsChanging(actor, projectId, oldCollaborators, newCollaborators, false);
    }

    public void makeProjectPublic(long actor, long projectId) {
        updateSharingPolicy(actor, projectId, Collections.<Long, SharingManagementTemplate.Access>emptyMap(),
            Collections.singletonMap(
                groupRepository.findAllUsersGroup().getId(),
                SharingManagementTemplate.Access.READ
            ), false
        );
    }


    /*--------------------------------- Helper Methods ---------------------------------------------------------------*/
    private void updateProjectAccessRecords(long projectId, Map<Long, SharingManagementTemplate.Access> colleagues,
                                            Map<Long, SharingManagementTemplate.Access> groups) {
        final HashMap<Long, SharingManagementTemplate.Access> sharedTo = new HashMap<>();

        // if project was shared then give all users read access
        final Long allUsersGroup = groupRepository.findAllUsersGroup().getId();
        if (groups.containsKey(allUsersGroup)) {
            final Set<Long> allIds = userRepository.findAllIds();
            for (Long allId : allIds) {
                sharedTo.put(allId, groups.get(allUsersGroup));
            }
        }

        // then update users access by specific group
        for (Long groupId : groups.keySet()) {
            final Set<Long> userIdsByGroup =
                userRepository.findUserIdsByGroup(groupId);    // there shouldn't be a lot of groups so this is Okay
            for (Long userId : userIdsByGroup) {
                sharedTo.put(userId, groups.get(groupId));
            }
        }

        // then update access by user specifically
        for (Long userId : colleagues.keySet()) {
            sharedTo.put(userId, colleagues.get(userId));
        }

        accessChangedEventPublisher.publish(projectId, sharedTo);
    }

    private GroupTemplate saveGroup(GroupTemplate group) {
        group.setLastModification(new Date());
        return groupRepository.save(group);
    }

    private Map<Long, Map<UserTemplate, Access>> projectCollaborators(final GroupTemplate entity) {
        final Map<Long, Map<UserTemplate, Access>> map =
            transformEntries(
                uniqueIndex(projectRepository.findBySharedGroup(entity.getId()), EntityUtil.ENTITY_TO_ID),
                new EntryTransformer<Long, ProjectTemplate, Map<UserTemplate, Access>>() {
                    @Override
                    public Map<UserTemplate, Access> transformEntry(Long key, ProjectTemplate value) {
                        //noinspection unchecked
                        return value.getSharing().getAllCollaborators();
                    }
                }
            );
        return copyOf(map);
    }


    private void processCollaboratorsChanging(long actor, long project,
                                              Map<UserTemplate, Access> oldCollaborators,
                                              Map<UserTemplate, Access> newCollaborators,
                                              boolean withEmailNotification) {
        processLooseWriteAccess(project, getUsersLostWriteAccess(oldCollaborators, newCollaborators));
        processGainAnyAccess(
            actor, project, Sets.difference(newCollaborators.keySet(), oldCollaborators.keySet()),
            withEmailNotification
        );
    }

    private Set<UserTemplate> getUsersLostWriteAccess(Map<UserTemplate, Access> oldCollaborators,
                                                      Map<UserTemplate, Access> newCollaborators) {
        Set<UserTemplate> result = new HashSet<>();
        for (UserTemplate user : oldCollaborators.keySet()) {
            final Optional<Access> newCollaboratorAccess = Optional.ofNullable(newCollaborators.get(user));
            if (!oldCollaborators.get(user).name().equals(WRITE.name())
                || newCollaboratorAccess.isPresent() &&
                newCollaboratorAccess.get().name().equals(WRITE.name())) {
                continue;
            }
            result.add(user);
        }
        return result;
    }

    private void processGainAnyAccess(Long actor, Long project, Set<UserTemplate> difference,
                                      boolean withEmailNotification) {
        if (!withEmailNotification) {
            return;
        }
        for (UserTemplate added : difference) {
            notifier.projectShared(actor, added.getId(), project);
        }
    }

    private void processLooseWriteAccess(Long project, Set<UserTemplate> looseAccess) {
        final FluentIterable<ExperimentTemplate> experiments = from(experimentRepository.findByProject(project));
        for (final UserTemplate each : looseAccess) {
            final FluentIterable<ExperimentTemplate> ownedByUser = experiments.filter(creator(each));
            if (ownedByUser.isEmpty()) {
                notifier.removingFromProject(each.getId(), project);
            } else {
                final ProjectTemplate projectEntity = projectRepository.findOne(project);
                final ProjectTemplate copy = createProjectCopy(each, projectEntity);
                for (ExperimentTemplate eachExperiment : ownedByUser) {
                    //noinspection unchecked
                    eachExperiment.setProject(copy);
                }
                for (ExperimentTemplate eachExperiment : ownedByUser) saveExperiment(eachExperiment);
                notifier.removingFromProjectWithCreatingNew(each.getId(), projectEntity.getId(), copy.getId());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private ProjectTemplate createProjectCopy(UserTemplate each, ProjectTemplate projectEntity) {
        final ProjectTemplate projectTemplate = factories.project.get();
        projectTemplate.setCreator(each);
        projectTemplate.setLab(projectEntity.getLab());
        projectTemplate.setName(projectEntity.getName());
        projectTemplate.setAreaOfResearch(projectEntity.getAreaOfResearch());
        projectTemplate.setDescription(projectEntity.getDescription());
        return saveProject(projectTemplate);
    }

    private Predicate<ExperimentTemplate> creator(final UserTemplate user) {
        return new Predicate<ExperimentTemplate>() {
            @Override
            public boolean apply(ExperimentTemplate input) {
                return input.getCreator().equals(user);
            }
        };
    }

    private ProjectTemplate saveProject(ProjectTemplate entity) {
        entity.setLastModification(new Date());
        return projectRepository.save(entity);
    }


    private ExperimentTemplate saveExperiment(ExperimentTemplate eachExperiment) {
        eachExperiment.setLastModification(new Date());
        return experimentRepository.save(eachExperiment);
    }

    private <T> Map<T, Access> transformIdToEntity(
        Map<Long, SharingManagementTemplate.Access> userOrGroupSharing, Function<Long, T> idToEntityFunction) {
        Map<T, Access> result = newHashMap();
        for (Map.Entry<Long, SharingManagementTemplate.Access> entry : userOrGroupSharing.entrySet()) {
            result.put(idToEntityFunction.apply(entry.getKey()), Access.valueOf(entry.getValue().name()));
        }
        return result;
    }

    private void updateExperimentSharing(ProjectTemplate project) {
        Sharing.Type type = project.getSharing().getType();
        List<ExperimentTemplate> experiments = experimentRepository.findByProject(project.getId());
        for (ExperimentTemplate experiment : experiments) {
            updateExperimentToken(type, experiment);
        }
    }

    private void updateExperimentToken(Sharing.Type type, ExperimentTemplate experiment) {
        switch (type) {
            case PUBLIC:
                if (Strings.isNullOrEmpty(experiment.getDownloadToken())) {
                    experiment.setDownloadToken(UUID.randomUUID().toString().replaceAll("-", ""));
                }
                break;
            case SHARED:
            case PRIVATE:
                experiment.setDownloadToken(null);
                break;
            default:
                throw new IllegalArgumentException("Unrecognized sharing type: " + type);
        }
        experimentRepository.save(experiment);
    }

}
