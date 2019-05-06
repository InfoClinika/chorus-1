package com.infoclinika.sso.model.internal.write.application;

import com.infoclinika.sso.model.ApplicationType;
import com.infoclinika.sso.model.exception.AccountIsAlreadyLinkedException;
import com.infoclinika.sso.model.internal.entity.User;
import com.infoclinika.sso.model.internal.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author andrii.loboda
 */
@Service
public class ChorusManagement implements ApplicationTypeManagement {
    @Inject
    private UserRepository userRepository;


    @Override
    public User findByUsername(String username) {
        return userRepository.findByChorusUsername(username);
    }

    @Override
    public User updateDetailsAndPersist(String username, String userSecretKey, User targetUser) {

        updateChorusDetails(username, userSecretKey, targetUser);

        final User persistedUser = userRepository.save(targetUser);
        return persistedUser;
    }

    @Override
    public ApplicationType getApplicationType() {
        return ApplicationType.CHORUS;
    }

    private User updateChorusDetails(String username, String userSecretKey, User targetUser) {
        if (userRepository.findByChorusUsername(username) != null) {
            /**This check refers to linking accounts:
             * When user logs in first, he specifies Chorus and Panorama account.
             * The application restricts linking for other user with the same email of one application type(i.e. Chorus)
             * */
            final String errorMessage =
                "The Chorus user email: " + username + " was already created, and override is not supported";
            throw new AccountIsAlreadyLinkedException(errorMessage);
        }

        targetUser.setChorusUsername(username);
        targetUser.setChorusSecretKey(userSecretKey);
        return targetUser;
    }
}

