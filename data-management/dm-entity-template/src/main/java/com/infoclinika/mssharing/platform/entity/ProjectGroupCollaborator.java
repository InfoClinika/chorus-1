package com.infoclinika.mssharing.platform.entity;


import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;

@Entity
@Table(name = "project_group_collaborator")
public class ProjectGroupCollaborator<G extends GroupTemplate<?>, P extends ProjectTemplate<?, ?, ?, ?>> extends AbstractPersistable<Long> {

    @OneToOne(targetEntity = GroupTemplate.class)
    @JoinColumn(name = "group_id")
    private G group;

    @ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.DETACH, CascadeType.PERSIST, CascadeType.MERGE}, targetEntity = ProjectTemplate.class)
    @JoinColumn(name = "project_id")
    private P project;

    private Sharing.Access accessLevel;

    public ProjectGroupCollaborator() {
    }

    public ProjectGroupCollaborator(G group, P project, Sharing.Access accessLevel) {
        this.group = group;
        this.project = project;
        this.accessLevel = accessLevel;
    }

    public G getGroup() {
        return group;
    }

    public void setGroup(G group) {
        this.group = group;
    }

    public P getProject() {
        return project;
    }

    public void setProject(P project) {
        this.project = project;
    }

    public Sharing.Access getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(Sharing.Access accessLevel) {
        this.accessLevel = accessLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        ProjectGroupCollaborator that = (ProjectGroupCollaborator) o;

        return accessLevel == that.accessLevel && group.equals(that.group) && project.equals(that.project);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        final int hash_multiplier = 31;
        result = hash_multiplier * result + group.hashCode();
        result = hash_multiplier * result + project.hashCode();
        result = hash_multiplier * result + accessLevel.hashCode();

        return result;
    }
}
