package com.infoclinika.mssharing.platform.entity;

import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static javax.persistence.InheritanceType.TABLE_PER_CLASS;

/**
 * @author Herman Zamula
 */
@Entity
@Inheritance(strategy = TABLE_PER_CLASS)
public abstract class LabTemplate<U extends UserTemplate<?>> extends AbstractAggregate {

    @Index(name = "NAME_IDX")
    @Basic(optional = false)
    private String name;
    @Basic(optional = false)
    private String institutionUrl;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, targetEntity = UserLabMembership.class)
    @JoinColumn(name = "lab_id")
    private Set<UserLabMembership<U, ? extends LabTemplate<U>>> labMemberships = newHashSet();

    private String contactEmail;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInstitutionUrl() {
        return institutionUrl;
    }

    public void setInstitutionUrl(String institutionUrl) {
        this.institutionUrl = institutionUrl;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public Set<UserLabMembership<U, ? extends LabTemplate<U>>> getLabMemberships() {
        return labMemberships;
    }

    @Transient
    public int getMembersAmount() {
        return labMemberships.size();
    }

    @Transient
    public U getHead() {
        for (UserLabMembership<U, ? extends LabTemplate<?>> labMembership : labMemberships) {
            if (labMembership.isHead()) {
                return labMembership.getUser();
            }
        }
        throw new IllegalStateException("Lab head not found for the lab: " + this);
    }

    @Transient
    public void setHead(U head) {
        boolean found = false;
        for (UserLabMembership<U, ? extends LabTemplate<?>> labMembership : labMemberships) {
            if (head.getEmail().equals(labMembership.getUser().getEmail())) {
                labMembership.setHead(true);
                found = true;
            } else {
                labMembership.setHead(false);
            }
        }
        if (!found) {
            final UserLabMembership<U, ? extends LabTemplate<U>> m = new UserLabMembership<>(head, this, true);
            head.addLabMembership(m);
            labMemberships.add(m);
        }
    }

    @Transient
    public boolean removeLabMembership(UserLabMembership labMembership) {
        // warning name not unique
        if (!labMembership.getLab().getName().equals(this.getName())) {
            return false;
        }
        boolean found = false;
        for (UserLabMembership existingMembership : labMemberships) {
            if (labMembership.getUser().equals(existingMembership.getUser())) {
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
    public void removeUser(U user) {
        UserLabMembership targetMembership = null;
        for (UserLabMembership membership : labMemberships) {
            if (membership.getUser().equals(user) && membership.getLab().equals(this)) {
                targetMembership = membership;
            }
        }
        if (targetMembership != null) {
            labMemberships.remove(targetMembership);
            user.removeLabMembership(targetMembership);
        }
    }


    @Override
    public String toString() {
        return "Lab{" +
                "id='" + getId() + "'" +
                "name='" + name + '\'' +
                ", institutionUrl='" + institutionUrl + '\'' +
                ", contactEmail='" + contactEmail + '\'' +
                '}';
    }

}
