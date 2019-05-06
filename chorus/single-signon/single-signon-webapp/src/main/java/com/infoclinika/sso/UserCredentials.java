package com.infoclinika.sso;

import com.infoclinika.sso.model.ApplicationType;
import org.pac4j.http.credentials.UsernamePasswordCredentials;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * @author andrii.loboda
 */
public class UserCredentials extends UsernamePasswordCredentials {

    private static final long serialVersionUID = 4479201924198651468L;
    @Nullable
    private final Long uniqueID;
    @NotNull
    private final ApplicationType applicationType;

    public UserCredentials(@Nullable Long uniqueID,
                           String username,
                           String password,
                           String clientName,
                           ApplicationType applicationType) {
        super(username, password, clientName);
        this.uniqueID = uniqueID;
        this.applicationType = applicationType;
    }

    @Nullable
    public Long getUniqueID() {
        return uniqueID;
    }

    public ApplicationType getApplicationType() {
        return applicationType;
    }
}
