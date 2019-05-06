package com.infoclinika.mssharing.platform.entity;

import javax.persistence.*;

/**
 * @author : Alexander Serebriyan
 */

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class UserInvitationLink<USER extends UserTemplate> extends AbstractAggregate {
    @ManyToOne(optional = false, targetEntity = UserTemplate.class)
    private USER user;
    @Column(unique = true, nullable = false)
    private String invitationLink;

    public UserInvitationLink() {
    }

    public UserInvitationLink(USER user, String invitationLink) {
        this.user = user;
        this.invitationLink = invitationLink;
    }

    public USER getUser() {
        return user;
    }

    public void setUser(USER user) {
        this.user = user;
    }

    public String getInvitationLink() {
        return invitationLink;
    }

    public void setInvitationLink(String invitationLink) {
        this.invitationLink = invitationLink;
    }
}

