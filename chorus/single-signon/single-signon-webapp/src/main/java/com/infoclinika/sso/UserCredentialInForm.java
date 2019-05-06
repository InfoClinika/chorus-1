package com.infoclinika.sso;

import com.infoclinika.sso.model.ApplicationType;
import org.jasig.cas.authentication.UsernamePasswordCredential;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * @author andrii.loboda
 */
public class UserCredentialInForm extends UsernamePasswordCredential {
    private static final long serialVersionUID = -3829662710429691095L;
    @Nullable
    private Long uniqueID;
    @NotNull
    private ApplicationType applicationType;

    public ApplicationType getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(ApplicationType applicationType) {
        this.applicationType = applicationType;
    }

    @Nullable
    public Long getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(@Nullable Long uniqueID) {
        this.uniqueID = uniqueID;
    }
}
