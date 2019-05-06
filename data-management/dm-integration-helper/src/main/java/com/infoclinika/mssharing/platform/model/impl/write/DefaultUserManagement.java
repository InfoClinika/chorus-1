package com.infoclinika.mssharing.platform.model.impl.write;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.infoclinika.mssharing.platform.entity.*;
import com.infoclinika.mssharing.platform.model.*;
import com.infoclinika.mssharing.platform.model.helper.write.UserManager;
import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate;
import com.infoclinika.mssharing.platform.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Provider;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.FluentIterable.from;
import static com.infoclinika.mssharing.platform.model.impl.ValidatorPreconditions.checkPresence;
import static com.infoclinika.mssharing.platform.model.write.UserManagementTemplate.LabMembershipRequestActions.*;

/**
 * @author : Alexander Serebriyan
 */
@Component
@Transactional
public class DefaultUserManagement<USER extends UserTemplate<LAB>, LAB extends LabTemplate<?>>
    implements UserManagementTemplate {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultUserManagement.class);
    @Inject
    private ChangeEmailRequestRepository changeEmailRequestRepository;
    @Inject
    private UserRepositoryTemplate<USER> userRepository;
    @Inject
    private LabRepositoryTemplate<LAB> labRepository;
    @Inject
    private RuleValidator ruleValidator;
    @Inject
    private UserManager<USER> userManagementHelper;
    @Inject
    private NotifierTemplate notifier;
    @Inject
    private Provider<Date> current;
    @Inject
    private RequestsTemplate requests;
    @Inject
    private UserLabMembershipRequestRepositoryTemplate<UserLabMembershipRequestTemplate>
        userLabMembershipRequestRepository;
    @Inject
    private UserLabMembershipRepositoryTemplate<USER, LAB> userLabMembershipRepository;
    @Inject
    private UserInvitationLinkRepository userInvitationLinkRepository;
    @Inject
    private InboxNotifierTemplate inboxNotifier;

    @Override
    public long createPerson(PersonInfo user, String password, Set<Long> labIds, String emailVerificationUrl) {
        if (!ruleValidator.canBeCreatedOrUpdated(user, labIds)) {
            throw new AccessDenied("Couldn't create user");
        }

        final USER existingUser = userRepository.findByEmail(user.email);
        if (existingUser != null) {
            LOGGER.warn("Attempt to create a user, which already exists: " + user);
            return existingUser.getId();
        }

        final USER savedUser = userManagementHelper.createUser(user, password);

        return afterCreatePerson(user, password, labIds, emailVerificationUrl, savedUser);

    }

    private Long afterCreatePerson(PersonInfo user, String password, Set<Long> labIds, String emailVerificationUrl,
                                   USER savedUser) {
        final long userId = savedUser.getId();
        final Set<LabTemplate> labs = transformToLabs(labIds);

        for (LabTemplate lab : labs) {
            fireLabMembershipRequest(lab, savedUser);
        }

        sendUserRegisteredNotification(emailVerificationUrl, userId);
        return userId;
    }

    @Override
    public long createPersonAndSendEmail(PersonInfo user, String password, Set<Long> labIds,
                                         String emailVerificationUrl, LabMembershipConfirmationUrlProvider urlProvider)
        throws URISyntaxException {
        if (!ruleValidator.canBeCreatedOrUpdated(user, labIds)) {
            throw new AccessDenied("Couldn't create user");
        }

        final USER existingUser = userRepository.findByEmail(user.email);
        if (existingUser != null) {
            LOGGER.warn("Attempt to create a user, which already exists: {}", user);
            return existingUser.getId();
        }

        final USER savedUser = userManagementHelper.createUser(user, password);
        return afterCreatePersonAndSendEmail(user, password, labIds, emailVerificationUrl, urlProvider, savedUser);
    }

    private Long afterCreatePersonAndSendEmail(PersonInfo user, String password, Set<Long> labIds,
                                               String emailVerificationUrl,
                                               LabMembershipConfirmationUrlProvider urlProvider, USER savedUser)
        throws URISyntaxException {
        final long userId = savedUser.getId();
        sendRequestToLabs(urlProvider, savedUser, userId, labIds);
        sendUserRegisteredNotification(emailVerificationUrl, userId);
        return userId;
    }

    private void sendUserRegisteredNotification(String emailVerificationUrl, long userId) {
        if (emailVerificationUrl != null) {
            notifier.userRegistered(userId, emailVerificationUrl);
        }
    }


    private Set<LabTemplate> transformToLabs(Set<Long> labIds) {
        return from(labIds).filter(input -> input != null).transform(
            (Function<Long, LabTemplate>) labId -> labRepository.findOne(labId)
        ).toSet();
    }

    private UserLabMembershipRequestTemplate fireLabMembershipRequest(LabTemplate lab, USER applicant) {
        //todo[tymchenko]: should we notify the lab head?
        final UserLabMembershipRequestTemplate request =
            new UserLabMembershipRequestTemplate<USER, LabTemplate<?>>(applicant, lab, current.get());
        requests.addOutboxItem(applicant.getId(), lab.getHead().getFullName(),
            "Requested a membership in " + lab.getName() + " lab.", current.get()
        );
        return userLabMembershipRequestRepository.save(request);
    }

    private void sendRequestToLabs(LabMembershipConfirmationUrlProvider urlProvider, USER savedUser, long userId,
                                   Set<Long> labIds) throws URISyntaxException {
        Set<LabTemplate> labs = transformToLabs(labIds);
        for (LabTemplate lab : labs) {
            UserLabMembershipRequestTemplate request = fireLabMembershipRequest(lab, savedUser);
            String approveUrl = urlProvider.getUrl(userId, lab.getId(), request.getId(), APPROVE);
            String refuseUrl = urlProvider.getUrl(userId, lab.getId(), request.getId(), REFUSE);
            notifier.sendLabMembershipRequest(lab.getHead().getId(), lab.getName(), userId, approveUrl, refuseUrl);
        }
    }


    @Override
    public long createPersonAndApproveMembership(PersonInfo user, String password, Set<Long> labs,
                                                 String emailVerificationUrl) {
        final long person = createPerson(user, password, labs, emailVerificationUrl);
        final List<UserLabMembershipRequestTemplate> requests =
            userLabMembershipRequestRepository.findPendingByUser(person);
        for (UserLabMembershipRequestTemplate request : requests) {
            approveLabMembershipRequest(request.getLab().getHead().getId(), request.getId());
        }
        return person;
    }

    @Override
    public void approveLabMembershipRequest(long actor, long requestId) {
        final UserLabMembershipRequestTemplate<USER, LAB> request =
            checkPresence(userLabMembershipRequestRepository.findOne(requestId));
        LAB targetLab = request.getLab();
        if (!ruleValidator.canModifyLabMembershipRequests(actor, targetLab.getId())) {
            throw new AccessDenied("Current user is not allowed to approve lab membership requests");
        }
        final USER applicant = request.getUser();
        applicant.addLab(targetLab);
        USER savedApplicant = userManagementHelper.saveAndGetUser(applicant);

        request.setDecision(UserLabMembershipRequestTemplate.Decision.APPROVED);
        userLabMembershipRequestRepository.save(request);
        inboxNotifier.notify(actor, applicant.getId(),
            "Your lab membership request for lab " + targetLab.getName() + " was approved"
        );
        notifier.labMembershipApproved(applicant.getId(), targetLab.getId());
    }

    @Override
    public void rejectLabMembershipRequest(long actor, long requestId, String comment) {
        final UserLabMembershipRequestTemplate<USER, LAB> request =
            checkPresence(userLabMembershipRequestRepository.findOne(requestId));
        final LAB targetLab = request.getLab();
        if (!ruleValidator.canModifyLabMembershipRequests(actor, targetLab.getId())) {
            throw new AccessDenied("Current user is not allowed to approve lab membership requests");
        }
        request.setDecision(UserLabMembershipRequestTemplate.Decision.REJECTED);
        userLabMembershipRequestRepository.save(request);
        inboxNotifier.notify(actor, request.getUser().getId(),
            "Your lab membership request for lab " + targetLab.getName() + " was rejected: " + comment
        );
        notifier.labMembershipRejected(request.getUser().getId(), targetLab.getId(), comment);

    }

    @Override
    public void updatePersonAndSendEmail(long userId, PersonInfo user, final Set<Long> labIds,
                                         LabMembershipConfirmationUrlProvider urlProvider) throws URISyntaxException {
        USER savedUser = userManagementHelper.updatePersonInfo(userId, user);
        final Set<Long> existingLabs = from(savedUser.getLabs()).transform(input -> input.getId()).toSet();

        final Set<Long> labsToApplyTo = from(labIds).filter(input -> !existingLabs.contains(input)).toSet();

        sendRequestToLabs(urlProvider, savedUser, savedUser.getId(), labsToApplyTo);

        afterUpdatePersonAndSendEmail(userId, user, labIds, urlProvider, savedUser);
    }

    protected void afterUpdatePersonAndSendEmail(long userId, PersonInfo user, final Set<Long> labIds,
                                                 LabMembershipConfirmationUrlProvider urlProvider, USER savedUser) {
        // remove user from labs
        Set<LAB> existingLabs = savedUser.getLabs();
        final FluentIterable<LAB> labsToLeave = from(existingLabs).filter(input -> !labIds.contains(input.getId()));
        //todo[tymchenko]: think of a more safe solution
        for (LabTemplate lab : labsToLeave) {
            UserLabMembership<USER, LAB> membership =
                userLabMembershipRepository.findByLabAndUser(lab.getId(), savedUser.getId());
            final boolean removedFromUser = membership.getUser().removeLabMembership(membership);
            if (removedFromUser) {
                final boolean removedFromLab = membership.getLab().removeLabMembership(membership);
                if (removedFromLab) {
                    userLabMembershipRepository.delete(membership);
                }
            }
        }
    }

    @Override
    public void verifyEmail(long userId) {
        userManagementHelper.verifyEmail(userId);
    }

    @Override
    public void sendPasswordRecoveryInstructions(long userId, String passwordRecoveryUrl) {
        notifier.recoverPassword(userId, passwordRecoveryUrl);
    }

    @Override
    public void resetPassword(long userId, String newPasswordHash) {
        userManagementHelper.resetPassword(userId, newPasswordHash);
    }

    @Override
    public String handleLabMembershipRequest(long labId, long requestId, LabMembershipRequestActions action)
        throws RequestAlreadyHandledException {
        checkRequest(requestId);
        long labHeadId = labRepository.findOne(labId).getHead().getId();
        String labName = labRepository.findOne(labId).getName();

        if (APPROVE.equals(action)) {
            approveLabMembershipRequest(labHeadId, requestId);
            return labName;
        }
        if (REFUSE.equals(action)) {
            String comment = "Direct link used";
            rejectLabMembershipRequest(labHeadId, requestId, comment);
            return labName;
        } else {
            throw new ObjectNotFoundException("Wrong request");
        }
    }

    @Override
    public long saveInvited(PersonInfo user, String passwordHash, Set<Long> labIds, String emailVerificationUrl,
                            LabMembershipConfirmationUrlProvider urlProvider) throws URISyntaxException {
        if (!ruleValidator.canBeCreatedOrUpdated(user, labIds)) {
            throw new AccessDenied("Couldn't create user");
        }
        final USER existingUser = userRepository.findByEmail(user.email);
        if (existingUser != null) {
            USER userWithUpdatedPersonInfo = userManagementHelper.updatePersonInfo(existingUser.getId(), user);
            USER savedUser = userManagementHelper.resetPassword(userWithUpdatedPersonInfo.getId(), passwordHash);

            Long savedUserId = savedUser.getId();

            userInvitationLinkRepository.delete(userInvitationLinkRepository.findByUser(savedUserId));
            sendRequestToLabs(urlProvider, savedUser, savedUserId, labIds);
            sendUserRegisteredNotification(emailVerificationUrl, savedUserId);
            return savedUserId;
        } else {
            return createPersonAndSendEmail(user, passwordHash, labIds, emailVerificationUrl, urlProvider);
        }
    }

    @Override
    public void changePassword(long id, String oldPassword, String newPasswordHash) {
        userManagementHelper.changePassword(id, oldPassword, newPasswordHash);
    }

    @Override
    public void resendActivationEmail(long userId, String emailVerificationUrl) {
        if (emailVerificationUrl != null) {
            notifier.userRegistered(userId, emailVerificationUrl);
        }
    }

    @Override
    public void updatePerson(long userId, PersonInfo user, Set<Long> labIds) {
        if (!ruleValidator.canBeCreatedOrUpdated(user, labIds)) {
            throw new AccessDenied("Couldn't update user");
        }
        USER updatedUser = userManagementHelper.updatePersonInfo(userId, user);
        afterUpdatePerson(userId, user, labIds, updatedUser);
    }

    protected void afterUpdatePerson(Long userId, PersonInfo user, Set<Long> labIds, USER updatedUser) {
        final Set<LabTemplate> labs = transformToLabs(labIds);
        final Set<LAB> existingLabs = (Set<LAB>) updatedUser.getLabs();
        final FluentIterable<LabTemplate> labsToApplyTo = from(labs).filter(new Predicate<LabTemplate>() {
            @Override
            public boolean apply(LabTemplate input) {
                return !existingLabs.contains(input);
            }
        });

        for (LabTemplate lab : labsToApplyTo) {
            fireLabMembershipRequest(lab, updatedUser);
        }
    }

    @Override
    public String inviteUser(Long invitedBy, String invite) {
        return userManagementHelper.inviteUser(invitedBy, invite);
    }

    @Override
    public String inviteUser(long invitedBy, String email, String invitationLink) {
        return userManagementHelper.inviteUser(invitedBy, email, invitationLink);
    }

    @Override
    public void sendEmailRequestInstructions(long userId, String newEmail, String emailChangeUrl) {
        notifier.emailChange(userId, newEmail, emailChangeUrl);
    }

    @Override
    public void checkRequest(long requestId) throws RequestAlreadyHandledException {
        final UserLabMembershipRequestTemplate request =
            checkPresence(userLabMembershipRequestRepository.findOne(requestId));
        if (request.getDecision() != null) {
            String labName = userLabMembershipRequestRepository.findOne(requestId).getLab().getName();
            throw new RequestAlreadyHandledException(labName);
        }
    }

    @Override
    public long applyForLabMembership(long actor, long labId) {
        final LAB lab = checkNotNull(labRepository.findOne(labId));
        final USER applicant = userManagementHelper.findUser(actor);
        //todo[tymchenko]: check for request duplicates
        final UserLabMembershipRequestTemplate savedRequest = fireLabMembershipRequest(lab, applicant);
        return savedRequest.getId();
    }

    @Override
    public long createUserWithGeneratedPassword(PersonInfo user, String emailVerificationUrl) {
        final USER existingUser = userRepository.findByEmail(user.email);
        if (existingUser != null) {
            return existingUser.getId();
        }
        String generateRandomPassword = userManagementHelper.generateRandomPassword();
        Long newUser = userManagementHelper.createUser(user, generateRandomPassword).getId();
        verifyEmail(newUser);
        notifier.sendGeneratedPassword(newUser, generateRandomPassword);
        return newUser;
    }

    @Override
    public void createChangeEmailRequest(Long userId, String email) {
        checkNotNull(userRepository.findOne(userId));
        ChangeEmailRequest changeEmailRequest = new ChangeEmailRequest(userId, email);
        changeEmailRequestRepository.save(changeEmailRequest);
    }

    @Override
    public void removeChangeEmailRequest(Long userId) {
        changeEmailRequestRepository.delete(userId);
    }

}
