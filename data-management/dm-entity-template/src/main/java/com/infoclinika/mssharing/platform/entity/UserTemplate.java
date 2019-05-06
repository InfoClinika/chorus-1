package com.infoclinika.mssharing.platform.entity;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.Collection;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static javax.persistence.CascadeType.*;
import static javax.persistence.FetchType.*;
import static javax.persistence.InheritanceType.TABLE_PER_CLASS;

/**
 * @author Herman Zamula
 */
@Entity
@Inheritance(strategy = TABLE_PER_CLASS)
public abstract class UserTemplate<L extends LabTemplate<?>> extends AbstractAggregate {
    @Embedded
    private PersonData personData;

    @Basic(optional = false)
    private String passwordHash;

    @OneToMany(orphanRemoval = true, cascade = ALL, fetch = LAZY, targetEntity = UserLabMembership.class)
    @JoinColumn(name = "user_id")
    private Set<UserLabMembership<? extends UserTemplate<?>, L>> labMemberships = newHashSet();

    private boolean admin;

    @Basic(optional = false)
    private boolean emailVerified;

    @OneToOne(optional = true, cascade = ALL, fetch = EAGER)
    @JoinColumn(name = "id")
    @Fetch(FetchMode.JOIN)
    private ChangeEmailRequest changeEmailRequest;

    @Column(name = "deleted", columnDefinition = "tinyint(1) not null default false")
    private boolean deleted;

    public PersonData getPersonData() {
        return personData;
    }

    public void setPersonData(PersonData personData) {
        this.personData = personData;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Set<UserLabMembership<? extends UserTemplate<?>, L>> getLabMemberships() {
        return labMemberships;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public ChangeEmailRequest getChangeEmailRequest() {
        return changeEmailRequest;
    }

    public void setChangeEmailRequest(ChangeEmailRequest changeEmailRequest) {
        this.changeEmailRequest = changeEmailRequest;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Transient
    public void addLab(L savedLab) {
        addLabMembership(new UserLabMembership<>(this, savedLab, false));
    }

    @Transient
        /*package*/ void addLabMembership(UserLabMembership labMembership) {
        if (!labMembership.getUser().getEmail().equals(this.getEmail())) {
            return;
        }
        for (UserLabMembership membership1 : labMemberships) {
            if (labMembership.getLab().equals(membership1.getLab())) {
                if (labMembership.isHead()) {
                    membership1.setHead(labMembership.isHead());
                }
                return;
            }
        }
        labMemberships.add(labMembership);
    }

    @Transient
    public void addLabs(Collection<L> labs) {
        for (L lab : labs) {
            addLab(lab);
        }
    }

    @Transient
    public boolean removeLabMembership(UserLabMembership labMembership) {
        if (!labMembership.getUser().getEmail().equals(this.getEmail())) {
            return false;
        }
        boolean found = false;
        for (UserLabMembership existingMembership : labMemberships) {
            if (labMembership.getLab().equals(existingMembership.getLab())) {
                if (labMembership.isHead()) {
                    return false;
                }
                found = true;
            }
        }
        if (found) {
            labMemberships.remove(labMembership);
        }
        return found;
    }

    @Transient
    public Set<L> getLabs() {
        return FluentIterable.from(labMemberships).transform(input -> input.getLab()).toSet();
    }

    @Transient
    public String getEmail() {
        return getPersonData().email;
    }

    @Transient
    public String getFullName() {
        return getPersonData().getFullName();
    }

    @Transient
    public String getFirstName() {
        return getPersonData().getFirstName();
    }

    @Transient
    public void setFirstName(String firstName) {
        this.personData.firstName = firstName;
    }

    @Transient
    public String getLastName() {
        return getPersonData().getLastName();
    }

    @Transient
    public void setLastName(String lastName) {
        this.personData.lastName = lastName;
    }

}
