package com.infoclinika.mssharing.platform.entity;

import com.google.common.base.Optional;
import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

/**
 * @author Stanislav Kurilin
 */
@Embeddable
public class Sharing<U extends UserTemplate<?>, P extends ProjectTemplate<?, ?, ?, ?>, G extends GroupTemplate<U>> {
    private Type type = Type.PRIVATE;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "project")
    private Set<ProjectUserCollaborator<U, P>> collaborators = newHashSet();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "project")
    private Set<ProjectGroupCollaborator<G, P>> groupsOfCollaborators = newHashSet();

    public Sharing() {
    }

    public Map<U, Access> getCollaborators() {
        final Map<U, Access> c = newHashMap();
        for (ProjectUserCollaborator<U, P> collaborator : collaborators) {
            c.put(collaborator.getUser(), collaborator.getLevel());
        }
        return c;
    }

    public Optional<Access> getAllUsersPermission() {
        for (ProjectGroupCollaborator group : groupsOfCollaborators) {
            if (group.getGroup().isIncludesAllUsers()) {
                return Optional.of(group.getAccessLevel());
            }
        }
        return Optional.absent();
    }

    public Map<G, Access> getGroupsOfCollaborators() {
        final Map<G, Access> gc = newHashMap();
        for (ProjectGroupCollaborator<G, P> collaborator : groupsOfCollaborators) {
            gc.put(collaborator.getGroup(), collaborator.getAccessLevel());
        }
        return gc;
    }

    public void setCollaborators(Map<U, Access> collaborators, P project) {
        this.collaborators.clear();
        final Set<ProjectUserCollaborator<U, P>> c = newHashSet();
        for (U user : collaborators.keySet()) {
            c.add(new ProjectUserCollaborator<>(user, project, collaborators.get(user)));
        }
        this.collaborators.addAll(c);
    }

    public void setGroupsOfCollaborators(Map<G, Access> groupsOfCollaborators, P project) {
        this.groupsOfCollaborators.clear();
        final Set<ProjectGroupCollaborator<G, P>> gc = newHashSet();
        for (G group : groupsOfCollaborators.keySet()) {
            gc.add(new ProjectGroupCollaborator<>(group, project, groupsOfCollaborators.get(group)));
        }

        this.groupsOfCollaborators.addAll(gc);
    }

    @Transient
    private void removeCollaborators() {
        this.groupsOfCollaborators.clear();
        this.collaborators.clear();
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
        switch (type) {
            case PUBLIC:
            case PRIVATE: {
                removeCollaborators();
                return;
            }
            case SHARED:
                return;
            default:
                throw new AssertionError(type);
        }
    }

    public Map<U, Access> getAllCollaborators() {
        final Map<U, Access> allUsers = newHashMap();
        for (Map.Entry<G, Access> group : getGroupsOfCollaborators().entrySet()) {
            for (U user : group.getKey().getCollaborators()) {
                allUsers.put(user, group.getValue());
            }
        }
        allUsers.putAll(getCollaborators());
        return allUsers;
    }

    @Transient
    public int getNumberOfAllCollaborators() {
        return getAllCollaborators().size();
    }

    public enum Type {
        PUBLIC, SHARED, PRIVATE
    }

    public enum Access {READ, WRITE}


}
