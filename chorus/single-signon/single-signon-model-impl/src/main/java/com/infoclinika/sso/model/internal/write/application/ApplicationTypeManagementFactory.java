package com.infoclinika.sso.model.internal.write.application;

import com.infoclinika.sso.model.ApplicationType;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author andrii.loboda
 */
@Service
public class ApplicationTypeManagementFactory {

    @Inject
    private ChorusManagement chorusUserManagement;
    @Inject
    private PanoramaManagement panoramaUserManagement;

    public ApplicationTypeManagement getInstance(ApplicationType applicationType) {
        checkNotNull(applicationType, "ApplicationType shouldn't be null");
        switch (applicationType) {
            case CHORUS:
                return chorusUserManagement;
            case PANORAMA:
                return panoramaUserManagement;
            default:
                throw new IllegalArgumentException("Unsupported applicationType: " + applicationType);
        }
    }
}
