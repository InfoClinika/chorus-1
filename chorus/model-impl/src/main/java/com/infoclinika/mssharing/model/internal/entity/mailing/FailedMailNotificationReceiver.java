package com.infoclinika.mssharing.model.internal.entity.mailing;

import com.infoclinika.mssharing.model.internal.entity.User;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * @author Herman Zamula
 */

@Entity
@Table(name = "m_failed_mail_notification_receiver")
public class FailedMailNotificationReceiver extends AbstractPersistable<Long> {

    @OneToOne
    @Nullable
    private User user;

    @Column(nullable = false, unique = true)
    @NotNull
    private String email;

    public FailedMailNotificationReceiver(String email) {
        this.email = email;
    }

    public FailedMailNotificationReceiver() {
    }

    @Nullable
    public User getUser() {
        return user;
    }

    public void setUser(@Nullable User user) {
        this.user = user;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
