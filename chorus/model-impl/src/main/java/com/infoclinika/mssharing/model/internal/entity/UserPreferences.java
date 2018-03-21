package com.infoclinika.mssharing.model.internal.entity;

import javax.persistence.Basic;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * @author Alexander Orlov
 */
@Entity
public class UserPreferences extends AbstractAggregate{

    @OneToOne
    @JoinColumn(name = "userId")
    private User user;

    @Basic (optional = false)
    private boolean showBillingNotification;

    public UserPreferences() {
    }

    public UserPreferences(User user, boolean showBillingNotification) {
        this.user = user;
        this.showBillingNotification = showBillingNotification;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isShowBillingNotification() {
        return showBillingNotification;
    }

    public void setShowBillingNotification(boolean showBillingNotification) {
        this.showBillingNotification = showBillingNotification;
    }

}
