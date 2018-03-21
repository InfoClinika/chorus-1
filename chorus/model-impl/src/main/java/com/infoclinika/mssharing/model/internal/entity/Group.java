package com.infoclinika.mssharing.model.internal.entity;

import com.infoclinika.mssharing.platform.entity.GroupTemplate;

import javax.persistence.*;
import java.util.Date;

import static com.google.common.collect.Sets.newHashSet;

/**
 * @author Stanislav Kurilin
 */
@Entity
@Table(name = "sharing_group")
@AssociationOverride(name = "collaborators",
        joinTable = @JoinTable(name = "sharing_group_USER",
                joinColumns = @JoinColumn(name = "sharing_group_id"),
                inverseJoinColumns = @JoinColumn(name = "collaborators_id")))
public class Group extends GroupTemplate<User> {

    public Group() {
    }

    public Group(Long id) {
        setId(id);
    }

    public Group(String name, User owner, Date lastModification) {
        setName(name);
        setOwner(owner);
        setLastModification(lastModification);
    }

    @Transient
    public void addCollaborator(User user) {
        getCollaborators().add(user);
    }

}
