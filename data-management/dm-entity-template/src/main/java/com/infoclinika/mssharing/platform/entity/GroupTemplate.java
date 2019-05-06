package com.infoclinika.mssharing.platform.entity;

import javax.persistence.*;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * @author Herman Zamula
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class GroupTemplate<U extends UserTemplate<?>> extends AbstractAggregate {

    @Basic(optional = false)
    public String name;

    @ManyToOne
    private U owner;

    @ManyToMany(fetch = FetchType.EAGER, targetEntity = UserTemplate.class)
    private Set<U> collaborators = newHashSet();

    private boolean includesAllUsers;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public U getOwner() {
        return owner;
    }

    public void setOwner(U owner) {
        this.owner = owner;
    }

    public Set<U> getCollaborators() {
        return collaborators;
    }

    public void setCollaborators(Set<U> collaborators) {
        this.collaborators = collaborators;
    }

    public boolean isIncludesAllUsers() {
        return includesAllUsers;
    }

    public void setIncludesAllUsers(boolean includesAllUsers) {
        this.includesAllUsers = includesAllUsers;
    }

    @Transient
    public int getNumberOfMembers() {
        return getCollaborators().size();
    }
}
