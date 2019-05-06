package com.infoclinika.mssharing.platform.entity;

import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;

import javax.persistence.*;

/**
 * @author : Alexander Serebriyan
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Table(name = "user_project_access",
    uniqueConstraints = {@UniqueConstraint(name = "user_project", columnNames = {"user_id", "project_id"})})
public class UserProjectAccess<USER extends UserTemplate, PROJECT extends ProjectTemplate> extends AbstractPersistable {

    @ManyToOne(optional = false, targetEntity = UserTemplate.class)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private USER user;

    @ManyToOne(optional = false, targetEntity = ProjectTemplate.class)
    @JoinColumn(name = "project_id", referencedColumnName = "id", nullable = false)
    private PROJECT project;

    @Column(name = "access_level", nullable = false)
    private Sharing.Access accessLevel;

    public UserProjectAccess() {
    }

    public UserProjectAccess(USER user, PROJECT project, Sharing.Access accessLevel) {
        this.user = user;
        this.project = project;
        this.accessLevel = accessLevel;
    }

    public USER getUser() {
        return user;
    }

    public void setUser(USER user) {
        this.user = user;
    }

    public PROJECT getProject() {
        return project;
    }

    public void setProject(PROJECT project) {
        this.project = project;
    }

    public Sharing.Access getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(Sharing.Access level) {
        this.accessLevel = level;
    }
}
