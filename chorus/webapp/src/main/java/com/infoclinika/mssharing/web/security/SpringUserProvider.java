package com.infoclinika.mssharing.web.security;

import com.infoclinika.mssharing.model.helper.SecurityHelper;
import com.infoclinika.mssharing.platform.model.helper.SecurityHelperTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.inject.Inject;

/**
 * @author Pavel Kaplin
 *
 * TODO <herman.zamula> : Migrate to Data Management Web
 */
//@Component
public class SpringUserProvider implements ChorusUserProvider {

    @Inject
    private SecurityHelperTemplate<SecurityHelper.UserDetails> securityHelper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SecurityHelper.UserDetails userDetails = securityHelper.getUserDetailsByEmail(username);

        if (userDetails == null) {
            throw new UsernameNotFoundException("User with email " + username + " not found");
        }

        if(userDetails.locked) {
            throw new BadCredentialsException("User is locked");
        }

        return UserProviderHelper.USER_DETAILS_TRANSFORMER.apply(userDetails);
    }

    @Override
    public UserDetails getUserDetails(long id) {
        SecurityHelper.UserDetails userDetails = securityHelper.getUserDetails(id);
        return UserProviderHelper.USER_DETAILS_TRANSFORMER.apply(userDetails);
    }

}
