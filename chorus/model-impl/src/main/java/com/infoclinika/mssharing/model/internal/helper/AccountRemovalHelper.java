package com.infoclinika.mssharing.model.internal.helper;

import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.repository.UserRepository;
import com.infoclinika.mssharing.model.write.UserManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Date;

/**
 * @author timofei.kasianov 7/23/18
 */
@Component
public class AccountRemovalHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountRemovalHelper.class);

    @Inject
    private UserRepository userRepository;
    @Inject
    private UserManagement userManagement;

    public void removeUsersWithRemovalRequestOlderThan(long removalRequestDelay) {
        LOGGER.info(" *** Remove users with removal request older than " + removalRequestDelay);
        final Date now = new Date();
        userRepository.findAllRequestedAccountRemoval()
            .forEach(u -> removeIfRequestIsOldEnough(u, now, removalRequestDelay));
    }

    private void removeIfRequestIsOldEnough(User user, Date currentDate, long removalRequestDelay) {
        try {
            final Date accountRemovalRequestDate = user.getAccountRemovalRequestDate();
            final long removalRequestAge = currentDate.getTime() - accountRemovalRequestDate.getTime();
            if (removalRequestAge > removalRequestDelay) {
                userManagement.markUserAsDeleted(user.getId());
            }
        } catch (Exception ex) {
            LOGGER.warn(" *** Couldn't mark user as deleted. User ID: " + user.getId(), ex);
        }
    }

}
