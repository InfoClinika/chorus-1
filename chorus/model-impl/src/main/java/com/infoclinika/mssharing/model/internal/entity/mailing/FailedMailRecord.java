package com.infoclinika.mssharing.model.internal.entity.mailing;

import com.infoclinika.mssharing.model.internal.entity.User;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Herman Zamula
 */
@Entity
@Table(name = "m_failed_mail_record")
public class FailedMailRecord extends AbstractPersistable<Long> {

    @ManyToOne(optional = false)
    private User user;

    @Column(name = "bounce_type")
    private String bounceType;

    @Column(name = "bounce_sub_type")
    private String bounceSubType;

    @Column(name = "bounce_timestamp_string")
    private String bounceTimestampString;

    @Lob
    private String reason;

    @Lob
    @Column(name = "raw_json")
    private String rawJson;

    @Column(name = "creation_date")
    private Date creationDate;

    public FailedMailRecord(
        User user,
        String bounceType,
        String bounceSubType,
        String bounceTimestampString,
        String reason,
        String rawJson,
        Date creationDate
    ) {
        this.user = user;
        this.bounceType = bounceType;
        this.bounceSubType = bounceSubType;
        this.bounceTimestampString = bounceTimestampString;
        this.reason = reason;
        this.rawJson = rawJson;
        this.creationDate = creationDate;
    }

    public FailedMailRecord() {
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getBounceType() {
        return bounceType;
    }

    public void setBounceType(String bounceType) {
        this.bounceType = bounceType;
    }

    public String getBounceSubType() {
        return bounceSubType;
    }

    public void setBounceSubType(String bounceSubType) {
        this.bounceSubType = bounceSubType;
    }

    public String getBounceTimestampString() {
        return bounceTimestampString;
    }

    public void setBounceTimestampString(String bounceTimestampString) {
        this.bounceTimestampString = bounceTimestampString;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRawJson() {
        return rawJson;
    }

    public void setRawJson(String rawJson) {
        this.rawJson = rawJson;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
