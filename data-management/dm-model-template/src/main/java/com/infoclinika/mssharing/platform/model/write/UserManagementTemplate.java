package com.infoclinika.mssharing.platform.model.write;

import com.infoclinika.mssharing.platform.model.RequestAlreadyHandledException;

import java.net.URISyntaxException;
import java.util.Set;

/**
 * Handles Organizational structure changes operations. With include Users and Labs.
 * <p/>
 * Each user works in laboratory. On user creation he should select one of existing or send request to create new.
 * Requests should be handled by admins. Admins is role for users.
 * <p/>
 * Since we can not create admin without lab and can not create lab without admin,
 * some predefined admin should exist in system.
 *
 * @author Stanislav Kurilin, Herman Zamula
 */
public interface UserManagementTemplate {

    /**
     * Creates user, send verification email and creates lab membership requests if <code>labIds</code> is not empty.
     * If the user with the given email already present in the system, returns it's id
     *
     * @param user                 person data
     * @param password             user password
     * @param labIds               laboratory ids to request membership
     * @param emailVerificationUrl url to confirm user registration
     * @return created (or already existed) user id
     */
    long createPerson(PersonInfo user, String password, Set<Long> labIds, String emailVerificationUrl);

    /**
     * Creates user, send verification email and creates lab membership requests
     * with APPROVE and REFUSE lab membership actions to the lab head.
     * <p>
     * If the user with the given email already present in the system, returns it's id
     *
     * @param user                 person data
     * @param password             user password
     * @param labIds               laboratory ids to request membership
     * @param emailVerificationUrl url to confirm user registration
     * @param urlProvider          APPROVE/REFUSE actions url provider
     * @return created (or already existed) user id
     */
    long createPersonAndSendEmail(PersonInfo user, String password, Set<Long> labIds, String emailVerificationUrl, LabMembershipConfirmationUrlProvider urlProvider) throws URISyntaxException;

    /**
     * Approves lab membership request
     *
     * @param actor     lab head identifier
     * @param requestId lab membership request identifier
     */
    void approveLabMembershipRequest(long actor, long requestId);

    /**
     * Refuses lab membership request with given comment
     *
     * @param actor     lab head
     * @param requestId lab membership request
     * @param comment   optional comment of reject decision
     */
    void rejectLabMembershipRequest(long actor, long requestId, String comment);

    /**
     * Creates user, send verification email and immediately adds it to the given labs.
     * If the user with the given email already present in the system, returns it's id
     *
     * @param user                 person data
     * @param password             user password
     * @param labIds               laboratory ids to request membership
     * @param emailVerificationUrl url to confirm user registration
     * @return created (or already existed) user id
     */
    long createPersonAndApproveMembership(PersonInfo user, String password, Set<Long> labIds, String emailVerificationUrl);

    void updatePersonAndSendEmail(long userId, PersonInfo user, Set<Long> labIds, LabMembershipConfirmationUrlProvider urlProvider) throws URISyntaxException;

    void verifyEmail(long userId);

    void sendPasswordRecoveryInstructions(long userId, String passwordRecoveryUrl);

    void resetPassword(long userId, String newPasswordHash);

    String handleLabMembershipRequest(long labId, long requestId, LabMembershipRequestActions action) throws RequestAlreadyHandledException;

    long saveInvited(PersonInfo user, String passwordHash, Set<Long> labIds, String emailVerificationUrl, LabMembershipConfirmationUrlProvider urlProvider) throws URISyntaxException;

    void changePassword(long id, String oldPassword, String newPasswordHash);

    void resendActivationEmail(long userId, String emailVerificationUrl);

    void updatePerson(long userId, PersonInfo user, Set<Long> labIds);

    /**
     * @see #inviteUser(long, String, String)
     */
    @Deprecated
    String inviteUser(Long invitedBy, String email);

    String inviteUser(long invitedBy, String email, String invitationLink);

    void sendEmailRequestInstructions(long userId, String newEmail, String emailChangeUrl);


    /**
     * Create a new user with the generated password. Or DO NOTHING if the user with the email equal to user.email already exists.
     * <p>
     * The created user (if any) will have the email already verified.
     *
     * @param user                 person info data
     * @param emailVerificationUrl the email to send the verification instructions to.
     * @return the ID of the created user or the ID of existing user if she already exists
     */
    long createUserWithGeneratedPassword(PersonInfo user, String emailVerificationUrl);

    /**
     * Check request for the lab membership.
     * Void if decision state == null or throw RequestAlreadyHandledException if decision state == APPROVED or REJECTED
     *
     * @param requestId the ID of the lab membership request
     */
    void checkRequest(long requestId) throws RequestAlreadyHandledException;

    /**
     * Apply for the lab membership for the current actor
     *
     * @param actor the applicant user ID
     * @param labId the target lab ID
     * @return the ID of the lab membership request
     */
    long applyForLabMembership(long actor, long labId);

    void removeChangeEmailRequest(Long userId);

    void createChangeEmailRequest(Long userId, String email);

    enum LabMembershipRequestActions {
        APPROVE,
        REFUSE
    }

    interface LabMembershipConfirmationUrlProvider {
        String getUrl(long user, long lab, long requestId, LabMembershipRequestActions action) throws URISyntaxException;
    }

    class PersonInfo {
        public final String firstName;
        public final String lastName;
        public final String email;

        public PersonInfo(String firstName, String lastName, String email) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
        }

        @Override
        public String toString() {
            return "PersonInfo{" +
                    "firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    ", email='" + email + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PersonInfo that = (PersonInfo) o;

            if (email != null ? !email.equals(that.email) : that.email != null) return false;
            if (firstName != null ? !firstName.equals(that.firstName) : that.firstName != null) return false;
            if (lastName != null ? !lastName.equals(that.lastName) : that.lastName != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = firstName != null ? firstName.hashCode() : 0;
            result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
            result = 31 * result + (email != null ? email.hashCode() : 0);
            return result;
        }
    }
}
