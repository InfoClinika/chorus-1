package com.infoclinika.mssharing.platform.model.helper.write;

import com.infoclinika.mssharing.platform.entity.PersonData;
import com.infoclinika.mssharing.platform.entity.UserInvitationLink;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.DefaultTransformers;
import com.infoclinika.mssharing.platform.model.EntityFactories;
import com.infoclinika.mssharing.platform.model.NotifierTemplate;
import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate.PersonInfo;
import com.infoclinika.mssharing.platform.repository.UserInvitationLinkRepository;
import com.infoclinika.mssharing.platform.repository.UserRepositoryTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Provider;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author : Alexander Serebriyan
 */
@Component
public class UserManager<USER extends UserTemplate<?>> {

    private final SecureRandom random = new SecureRandom();
    @Inject
    private UserRepositoryTemplate<USER> userRepository;
    @Inject
    private EntityFactories factories;
    @Inject
    private PasswordEncoder encoder;
    @Inject
    private Provider<Date> current;
    @Inject
    private NotifierTemplate notifier;
    @Inject
    private UserInvitationLinkRepository userInvitationLinkRepository;
    private Logger LOG = LoggerFactory.getLogger(UserManager.class);

    //TODO: Method is unused. Consider should we remove it.
    public boolean userWithSpecifiedEmailExists(final String userEmail) {
        USER user = userRepository.findByEmail(userEmail);
        return user != null;
    }

    public USER createUser(PersonInfo userInfo, String password) {
        final USER existingUser = userRepository.findByEmail(userInfo.email);
        if (existingUser != null) {
            LOG.warn("Attempt to create a user, which already exists: " + userInfo);
            return existingUser;
        }
        //noinspection unchecked
        USER userToSave = (USER) factories.user.get();
        userToSave.setPersonData(new PersonData(userInfo.email, userInfo.firstName, userInfo.lastName));
        userToSave.setPasswordHash(encoder.encode(password));
        return saveAndGetUser(userToSave);
    }

    //TODO [code review]
    public USER createUserWithRandomPassword(PersonInfo userInfo) {
        String password = generateRandomPassword();
        return createUser(userInfo, password);
    }

    public USER updatePersonInfo(Long userId, PersonInfo user) {
        final USER userToSave = userRepository.findOne(userId);
        userToSave.setPersonData(DefaultTransformers.personalInfoToData(user));
        return saveAndGetUser(userToSave);
    }

    public USER verifyEmail(Long userId) {
        final USER user = findUser(userId);
        user.setEmailVerified(true);
        return saveAndGetUser(user);
    }

    public USER resetPassword(Long userId, String newPasswordHash) {
        final USER user = findUser(userId);
        user.setPasswordHash(newPasswordHash);
        user.setLastModification(new Date());
        return saveAndGetUser(user);
    }

    //TODO [code review]
    public void sendPasswordRecoveryInstructions(Long userId, String passwordRecoveryUrl) {
        notifier.recoverPassword(userId, passwordRecoveryUrl);
    }

    public USER changePassword(Long id, String oldPassword, String newPasswordHash) {
        final USER user = findUser(id);
        if (!encoder.matches(oldPassword, user.getPasswordHash()))
            throw new AccessDenied("Old password doesn't matched");
        user.setPasswordHash(newPasswordHash);
        return saveAndGetUser(user);
    }

    /**
     * @see #inviteUser(Long, String, String)
     */
    @Deprecated
    public String inviteUser(Long invitedBy, String futureUserEmail) {
        USER invitedUser = createUser(new PersonInfo("Invited", "User", futureUserEmail), generateRandomPassword());
        UserInvitationLink<USER> userInvitationLink = new UserInvitationLink<>();
        String invitationLink = UUID.randomUUID().toString();
        userInvitationLink.setInvitationLink(invitationLink);
        userInvitationLink.setUser(invitedUser);
        final UserInvitationLink link = userInvitationLinkRepository.save(userInvitationLink);
        notifier.sendInvitationEmail(invitedBy, futureUserEmail, link.getInvitationLink());
        return invitationLink;
    }

    public String inviteUser(Long invitedBy, String futureUserEmail, String invitationUrl) {
        USER invitedUser = createUser(new PersonInfo("Invited", "User", futureUserEmail), generateRandomPassword());
        UserInvitationLink<USER> userInvitationLink = new UserInvitationLink<>();
        userInvitationLink.setInvitationLink(invitationUrl);
        userInvitationLink.setUser(invitedUser);
        final UserInvitationLink link = userInvitationLinkRepository.save(userInvitationLink);
        notifier.sendInvitationEmail(invitedBy, futureUserEmail, link.getInvitationLink());
        return invitationUrl;
    }

    public USER saveAndGetUser(USER user) {
        user.setLastModification(current.get());
        return userRepository.save(user);
    }

    public USER findUser(long userId) {
        return checkNotNull(userRepository.findOne(userId), "Couldn't find user with id %s", userId);
    }

    public String generateRandomPassword() {
        return new BigInteger(50, random).toString(32);
    }
}
