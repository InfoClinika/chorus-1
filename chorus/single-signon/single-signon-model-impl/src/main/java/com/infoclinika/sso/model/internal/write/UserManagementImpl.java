package com.infoclinika.sso.model.internal.write;

import com.google.common.base.Optional;
import com.infoclinika.sso.model.ApplicationType;
import com.infoclinika.sso.model.internal.entity.User;
import com.infoclinika.sso.model.internal.repository.UserRepository;
import com.infoclinika.sso.model.internal.write.application.ApplicationTypeManagement;
import com.infoclinika.sso.model.internal.write.application.ApplicationTypeManagementFactory;
import com.infoclinika.sso.model.write.UserManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author andrii.loboda
 */
@Service
@Transactional
public class UserManagementImpl implements UserManagement {
    private static final Logger LOG = LoggerFactory.getLogger(UserManagementImpl.class);
    @Inject
    private UserRepository userRepository;
    @Inject
    private ApplicationTypeManagementFactory appTypeManagementFactory;

    @Override
    public long addApplicationForUser(Optional<Long> userID, ApplicationType applicationType, String username, String userSecretKey) {
        final ApplicationTypeManagement userManagement = appTypeManagementFactory.getInstance(applicationType);
        if (userID.isPresent()) {
            final long linkedUser = linkAccount(userID.get(), userManagement, username, userSecretKey);
            return linkedUser;
        } else {
            final User user = userManagement.findByUsername(username);

            if (user == null) {
                final User userToPersist = new User();
                final User persistedUser = userManagement.updateDetailsAndPersist(username, userSecretKey, userToPersist);
                return persistedUser.getId();
            } else {
                return user.getId();
            }
        }
    }


    private long linkAccount(long userID, ApplicationTypeManagement userManagement, String username, String userSecretKey) {
        LOG.info("Linking user account: " + userID + " with application: " + userManagement.getApplicationType());
        final User userToUpdate = userRepository.findOne(userID);
        checkNotNull(userToUpdate, "There is no user registered in the system, id: %s", userID);
        final User persistedUser = userManagement.updateDetailsAndPersist(username, userSecretKey, userToUpdate);
        return persistedUser.getId();
    }


}
