package com.infoclinika.mssharing.web.security;

import com.infoclinika.mssharing.model.helper.SecurityHelper;
import com.infoclinika.mssharing.model.helper.SecurityHelper.UserDetails;
import com.infoclinika.mssharing.model.write.UserManagement;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author : Alexander Serebriyan
 */
@Component
public class AuthenticationListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    @Inject
    private SecurityHelper securityHelper;
    @Inject
    private UserManagement userManagement;

    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent appEvent) {
        final String userEmail = (String) appEvent.getAuthentication().getPrincipal();
        final UserDetails userDetails = securityHelper.getUserDetailsByEmail(userEmail);
        userManagement.logUnsuccessfulLoginAttempt(userDetails.id);
    }
}
