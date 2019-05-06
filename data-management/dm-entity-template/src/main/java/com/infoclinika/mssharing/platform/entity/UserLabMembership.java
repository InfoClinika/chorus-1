package com.infoclinika.mssharing.platform.entity;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.Objects;

/**
 * @author Herman Zamula
 */
@Entity
public class UserLabMembership<U extends UserTemplate<?>, L extends LabTemplate<?>> extends AbstractAggregate {

    @ManyToOne(targetEntity = UserTemplate.class)
    @JoinColumn(name = "user_id")
    @Fetch(FetchMode.JOIN)
    private U user;
    @ManyToOne(targetEntity = LabTemplate.class)
    @JoinColumn(name = "lab_id")
    @Fetch(FetchMode.JOIN)
    private L lab;
    private boolean head;

    protected UserLabMembership() {
    }

    public UserLabMembership(U user, L lab, boolean head) {
        this.user = user;
        this.lab = lab;
        this.head = head;
    }

    public U getUser() {
        return user;
    }

    public void setUser(U user) {
        this.user = user;
    }

    public L getLab() {
        return lab;
    }

    public void setLab(L lab) {
        this.lab = lab;
    }

    public boolean isHead() {
        return head;
    }

    public void setHead(boolean head) {
        this.head = head;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserLabMembership)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        UserLabMembership<?, ?> that = (UserLabMembership<?, ?>) o;
        return head == that.head &&
            Objects.equals(user, that.user) &&
            Objects.equals(lab, that.lab);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        final int hash_multiplier = 31;
        result = hash_multiplier * result + (user != null ? user.hashCode() : 0);
        result = hash_multiplier * result + (lab != null ? lab.hashCode() : 0);
        result = hash_multiplier * result + (head ? 1 : 0);
        return result;
    }
}
