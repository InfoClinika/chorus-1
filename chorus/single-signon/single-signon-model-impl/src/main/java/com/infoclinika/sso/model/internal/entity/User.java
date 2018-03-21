package com.infoclinika.sso.model.internal.entity;

import com.google.common.base.MoreObjects;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;


/**
 * @author andrii.loboda
 */
@Entity
public class User extends AbstractPersistable<Long> {
    private static final long serialVersionUID = -6130365111803242782L;

    private String chorusUsername;
    private String chorusSecretKey;

    private String panoramaUsername;
    private String panoramaSecretKey;

    public String getChorusUsername() {
        return chorusUsername;
    }

    public void setChorusUsername(String chorusUsername) {
        this.chorusUsername = chorusUsername;
    }

    public String getChorusSecretKey() {
        return chorusSecretKey;
    }

    public void setChorusSecretKey(String chorusSecretKey) {
        this.chorusSecretKey = chorusSecretKey;
    }

    public String getPanoramaUsername() {
        return panoramaUsername;
    }

    public void setPanoramaUsername(String panoramaUsername) {
        this.panoramaUsername = panoramaUsername;
    }

    public String getPanoramaSecretKey() {
        return panoramaSecretKey;
    }

    public void setPanoramaSecretKey(String panoramaSecretKey) {
        this.panoramaSecretKey = panoramaSecretKey;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("chorusUsername", chorusUsername)
                .add("panoramaUsername", panoramaUsername)
                .toString();
    }
}
