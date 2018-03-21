package com.infoclinika.mssharing.platform.model.impl.write.sharing;

import com.infoclinika.mssharing.platform.entity.GroupTemplate;
import com.infoclinika.mssharing.platform.entity.Sharing;
import com.infoclinika.mssharing.platform.entity.UserProjectAccess;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.EntityFactories;
import com.infoclinika.mssharing.platform.model.RuleValidator;
import com.infoclinika.mssharing.platform.model.helper.write.SharingManager;
import com.infoclinika.mssharing.platform.model.write.SharingManagementTemplate;
import com.infoclinika.mssharing.platform.repository.GroupRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.UserProjectAccessRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Herman Zamula
 */
@Transactional
@Component
public class DefaultSharingManagement<GROUP extends GroupTemplate> implements SharingManagementTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultSharingManagement.class);

    @Inject
    protected GroupRepositoryTemplate<GROUP> groupRepository;
    @Inject
    protected UserProjectAccessRepository userProjectAccessRepository;
    @Inject
    protected RuleValidator ruleValidator;
    @Inject
    protected SharingManager managementHelper;
    @Inject
    private EntityFactories entityFactories;

    @Override
    public long createGroup(long actor, String name, Iterable<Long> collaborators) {
        beforeCreateGroup(actor, name, collaborators);
        return managementHelper.createGroup(actor, name, collaborators).getId();
    }

    protected void beforeCreateGroup(long actor, String name, Iterable<Long> collaborators) {
        checkGroupName(name, null);
        if (!collaborators.iterator().hasNext()) {
            throw new IllegalArgumentException("No members were supplied for the new group '" + name + "'");
        }
    }

    @Override
    public void removeGroup(long actor, long groupId) {
        beforeRemoveGroup(actor, groupId);
        managementHelper.removeGroup(groupId);
    }

    protected void beforeRemoveGroup(long actor, long groupId) {
        if (!ruleValidator.canRemoveGroup(actor, groupId))
            throw new AccessDenied("User isn't permitted to remove group");
    }

    @Override
    public void renameGroup(long actor, long groupId, String newName) {
        beforeRenameGroup(actor, groupId, newName);
        managementHelper.editGroupName(groupId, newName);
    }

    protected void beforeRenameGroup(long actor, long groupId, String newName) {
        if (!ruleValidator.userHasReadPermissionsToEditGroup(actor, groupId))
            throw new AccessDenied("Couldn't edit group");
        checkGroupName(newName, groupId);
    }

    @Override
    public void setCollaborators(long actor, long group, Iterable<Long> collaborators, boolean withEmailNotification) {

        beforeSetCollaborators(actor, group, collaborators);

        managementHelper.setCollaborators(actor, group, collaborators, withEmailNotification);

    }

    private void beforeSetCollaborators(long actor, long group, Iterable<Long> collaborators) {
        if (!ruleValidator.userHasReadPermissionsToEditGroup(actor, group))
            throw new AccessDenied("Couldn't edit group");

        if (!collaborators.iterator().hasNext()) {
            throw new IllegalArgumentException("No members were supplied for the group");
        }
    }

    @Override
    public void updateSharingPolicy(long actor, long project, Map<Long, Access> colleagues, Map<Long, Access> groups, boolean withEmailNotification) {
        beforeUpdateSharingPolicy(actor, project, colleagues, groups, withEmailNotification);
        final Map newAllCollaborators = managementHelper.updateSharingPolicy(actor, project, colleagues, groups, withEmailNotification);
        afterUpdateSharingPolicy(actor, project, colleagues, groups, withEmailNotification, newAllCollaborators);
    }

    protected void afterUpdateSharingPolicy(long actor, long project, Map<Long, Access> colleagues, Map<Long, Access> groups, boolean withEmailNotification, Map newAllCollaborators) {

    }

    private void beforeUpdateSharingPolicy(long actor, long project, Map<Long, Access> colleagues, Map<Long, Access> groups, boolean withEmailNotification) {
        if (!ruleValidator.hasWriteAccessOnProject(actor, project))
            throw new AccessDenied("User have no permission to share project");
    }


    @Override
    public void makeProjectPublic(long actor, long projectId) {
        beforeMakeProjectPublic(actor, projectId);
        managementHelper.makeProjectPublic(actor, projectId);
    }

    protected void beforeMakeProjectPublic(long actor, long projectId) {
        if (!ruleValidator.hasWriteAccessOnProject(actor, projectId))
            throw new AccessDenied("User have no permission to make project public");
    }

    @Override
    public void makeProjectPrivate(long actor, long projectId) {
        beforeMakeProjectPrivate(actor, projectId);
        managementHelper.makeProjectPrivate(actor, projectId);
    }


    @Override
    public void updateProjectAccessRecords(long projectId, final Map<Long, Access> sharedTo) {
        LOG.info("*** Creating intermediate records about user access level to projects");
        final List<UserProjectAccess> byProject = userProjectAccessRepository.findByProjectId(projectId);

        userProjectAccessRepository.delete(byProject);
        userProjectAccessRepository.flush();

        final LinkedList<UserProjectAccess> userProjectAccesses = new LinkedList<>();
        for (Long userId : sharedTo.keySet()) {
            final UserTemplate user = entityFactories.userFromId.apply(userId);
            final ProjectTemplate project = entityFactories.projectFromId.apply(projectId);
            userProjectAccesses.add(new UserProjectAccess<>(user, project, Sharing.Access.valueOf(sharedTo.get(userId).name())));
        }

        userProjectAccessRepository.save(userProjectAccesses);
    }


    protected void beforeMakeProjectPrivate(long actor, long projectId) {
        if (!ruleValidator.hasWriteAccessOnProject(actor, projectId))
            throw new AccessDenied("User have no permission to make project private");
    }


    private void checkGroupName(String newName, final Long id) {
        final GROUP group = groupRepository.findOneByName(newName);
        if (group != null) {
            if (id != null && group.equals(groupRepository.findOne(id))) {
                return;
            }
            throw new IllegalArgumentException("Group with newName '" + newName + "' already exists");
        }
    }

}
