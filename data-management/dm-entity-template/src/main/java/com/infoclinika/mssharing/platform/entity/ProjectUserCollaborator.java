package com.infoclinika.mssharing.platform.entity;

import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;

import static javax.persistence.CascadeType.*;

@Entity
@Table(name = "project_user_collaborator")
public class ProjectUserCollaborator<U extends UserTemplate<?>, P extends ProjectTemplate<?, ?, ?, ?>>
    extends AbstractPersistable<Long> {

    @OneToOne(targetEntity = UserTemplate.class)
    @JoinColumn(name = "user_id")
    private U user;

    @JoinColumn(name = "project_id")
    @ManyToOne(cascade = {REFRESH, DETACH, PERSIST, MERGE}, targetEntity = ProjectTemplate.class)
    private P project;
    private Sharing.Access level;

    public ProjectUserCollaborator() {
    }

    public ProjectUserCollaborator(U user, P project, Sharing.Access level) {
        this.user = user;
        this.project = project;
        this.level = level;
    }

    public U getUser() {
        return user;
    }

    public void setUser(U user) {
        this.user = user;
    }

    public P getProject() {
        return project;
    }

    public void setProject(P project) {
        this.project = project;
    }

    public Sharing.Access getLevel() {
        return level;
    }

    public void setLevel(Sharing.Access level) {
        this.level = level;
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

        ProjectUserCollaborator that = (ProjectUserCollaborator) o;

        return level == that.level && project.equals(that.project) && user.equals(that.user);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        final int hash_multiplier = 31;
        result = hash_multiplier * result + user.hashCode();
        result = hash_multiplier * result + project.hashCode();
        result = hash_multiplier * result + level.hashCode();

        return result;
    }
}
