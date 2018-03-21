package com.infoclinika.mssharing.model.internal.entity;

import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveProject;
import com.infoclinika.mssharing.platform.entity.PersonData;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * @author Stanislav Kurilin
 */
@Entity
@Table(name = "USER",
        uniqueConstraints = {@UniqueConstraint(columnNames = "EMAIL")})
public class User extends UserTemplate<Lab> {
    @OneToMany(mappedBy = "user", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UploadAppConfiguration> uploadAppConfigurations = newHashSet();
    @OneToOne(optional = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "id")
    @Fetch(FetchMode.JOIN)
    private RestToken restToken;
    @ManyToMany
    @JoinTable(name = "project_access_view")
    @Immutable
    private Set<ActiveProject> projectsWithReadAccess;
    private Subscription subscription = new Subscription();

    @Column(name = "skip_emails_sending", nullable = false)
    private boolean skipEmailsSending = false;

    /**
     * This secret token should be used to get attributes from Chorus for SSO application instead of providing password
     * */
    private String secretToken;

    @Column
    private String clientToken;

    @Column(name = "email_verification_sent_on")
    private Date emailVerificationSentOnDate = new Date();

    @Column(name = "password_reset_sent_on")
    private Date passwordResetSentOnDate = new Date();

    @Column(nullable = false)
    private boolean locked;

    @Column(name = "unsuccessful_login_attempts", nullable = false)
    private int unsuccessfulLoginAttempts;


    public User(PersonData personData, String passwordHash) {
        setPersonData(personData);
        setPasswordHash(passwordHash);
    }

    public User(long id) {
        setId(id);
    }

    public User() {
    }

    public RestToken getRestToken() {
        return restToken;
    }

    public void setRestToken(RestToken restToken) {
        this.restToken = restToken;
    }


    public Set<ActiveProject> getProjectsWithReadAccess() {
        return projectsWithReadAccess;
    }

    public Subscription getSubscription() {
        if (subscription == null) {
            subscription = new Subscription();
        }
        return subscription;
    }

    @Transient
    public void removeUploadAppConfiguration(final UploadAppConfiguration configuration) {
        uploadAppConfigurations.remove(configuration);
    }

    public boolean isSkipEmailsSending() {
        return skipEmailsSending;
    }

    public void setSkipEmailsSending(boolean skipEmails) {
        this.skipEmailsSending = skipEmails;
    }

    public String getSecretToken() {
        return secretToken;
    }

    public void setSecretToken(String secretToken) {
        this.secretToken = secretToken;
    }

    public Date getEmailVerificationSentOnDate() {
        return emailVerificationSentOnDate;
    }

    public void setEmailVerificationSentOnDate(Date emailVerificationSentOnDate) {
        this.emailVerificationSentOnDate = emailVerificationSentOnDate;
    }

    public Date getPasswordResetSentOnDate() {
        return passwordResetSentOnDate;
    }

    public void setPasswordResetSentOnDate(Date passwordResetSentOnDate) {
        this.passwordResetSentOnDate = passwordResetSentOnDate;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public int getUnsuccessfulLoginAttempts() {
        return unsuccessfulLoginAttempts;
    }

    public void setUnsuccessfulLoginAttempts(int unsuccessfulLoginAttempts) {
        this.unsuccessfulLoginAttempts = unsuccessfulLoginAttempts;
    }

    public String getClientToken() {
        return clientToken;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    @Transient
    public void resetEmailVerificationDate() {
        this.emailVerificationSentOnDate = new Date();
    }

    @Transient
    public void resetPasswordResetDate() {
        this.passwordResetSentOnDate = new Date();
    }
}
