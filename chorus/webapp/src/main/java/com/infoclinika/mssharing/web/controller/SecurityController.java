package com.infoclinika.mssharing.web.controller;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.model.features.ApplicationFeature;
import com.infoclinika.mssharing.model.helper.BillingFeaturesHelper;
import com.infoclinika.mssharing.model.helper.FeaturesHelper;
import com.infoclinika.mssharing.model.helper.SecurityHelper;
import com.infoclinika.mssharing.model.read.UserPreferencesReader;
import com.infoclinika.mssharing.model.read.UserReader;
import com.infoclinika.mssharing.model.write.*;
import com.infoclinika.mssharing.model.write.ClientTokenService.ClientToken;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.ObjectNotFoundException;
import com.infoclinika.mssharing.platform.model.RequestAlreadyHandledException;
import com.infoclinika.mssharing.platform.model.helper.RegistrationHelperTemplate;
import com.infoclinika.mssharing.platform.model.helper.RegistrationHelperTemplate.LabItem;
import com.infoclinika.mssharing.platform.model.write.LabManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate;
import com.infoclinika.mssharing.platform.web.security.RichUser;
import com.infoclinika.mssharing.propertiesprovider.ChorusPropertiesProvider;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeature;
import com.infoclinika.mssharing.web.controller.request.LaboratoryOperationRequest;
import com.infoclinika.mssharing.web.controller.response.SuccessErrorResponse;
import com.infoclinika.mssharing.web.controller.response.ValueResponse;
import com.infoclinika.mssharing.web.security.ChorusUserProvider;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import java.net.URISyntaxException;
import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.find;
import static com.infoclinika.mssharing.model.read.UserPreferencesReader.UserPreferencesInfo;
import static com.infoclinika.mssharing.platform.web.security.RichUser.get;
import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;

/**
 * @author Pavel Kaplin
 */
@Controller
@RequestMapping("/security")
public class SecurityController extends ErrorHandler {
    public static final String EMAIL_VERIFIED = SecurityController.class.getName() + "_emailVerified";
    private static final int MAX_ALLOWED_VERIFICATION_LINK_AGE_IN_HOURS = 8;
    private static final int MAX_ALLOWED_PASSWORD_RESET_LINK_AGE_IN_HOURS = 24;
    private static final String SUCCESS_MESSAGE = SecurityController.class.getName() + "_successMessage";
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityController.class);
    private static final String REDIRECT_TO_EMAIL_VERIFIED_PAGE =
        "redirect:../pages/authentication.html#/emailVerification";
    private static final String YOU_ARE_NOT_ABLE_TO_CHANGE_PASSWORD_WITH_THIS_LINK =
        "You are not able to change password with this link";
    private static final String CHECK_YOUR_EMAIL_AND_CONFIRM_EMAIL_CHANGING =
        "Check your email and confirm email changing.";
    private static final String YOU_WERE_SUCCESSFULLY_REGISTERED =
        "You were successfully registered. Please check your email";
    private static final String REDIRECT_TO_DASHBOARD_PAGE = "redirect:../pages/dashboard.html";
    private static final String MAC_IS_VALID = "Mac is valid";
    private static final String YOU_CAN_RESET_PASSWOR_YD = "You can reset passworYd.";
    private static final String DEPRECATED = "deprecated";
    private static final String LAB_MEMBER_FIRST_NAME = "labMemberFirstName";
    private static final String LAB_MEMBER_SECOND_NAME = "labMemberSecondName";
    private static final String LAB_NAME = "labName";
    private static final String RESULT = "result";
    private static final String APPROVE = "approve";
    private static final String REFUSE = "refuse";
    private static final String USER_ID = "userId";
    private static final String LAB_ID = "labId";
    private static final String REQUEST_ID = "requestId";
    private static final String ACTION = "action";
    private static final String TOKEN = "token";
    private static final String EMAIL = "email";
    private static final String MAC = "mac";
    private static final Date EMPTY_DATE = null;

    @Inject
    private UserManagement userManagement;

    @Inject
    private UserReader userReader;

    @Inject
    private PasswordEncoder passwordEncoder;

    @Inject
    private LabManagement labManagement;

    @Inject
    private RegistrationHelperTemplate registrationHelper;

    @Inject
    private EmailVerificationCrypto crypto;

    @Inject
    private SecurityHelper securityHelper;

    @Inject
    private ChorusUserProvider chorusUserProvider;

    @Inject
    private LabHeadManagement labHeadManagement;

    @Inject
    private UserPreferencesReader userPreferencesReader;

    @Inject
    private UserPreferencesManagement userPreferencesManagement;

    @Inject
    private ClientTokenService clientTokenService;

    @Inject
    private BillingFeaturesHelper billingFeaturesHelper;

    @Inject
    private FeaturesHelper featuresHelper;

    @Inject
    private ChorusPropertiesProvider chorusPropertiesProvider;

    public SecurityController() {
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    public RichUser loggedInUser(Principal principal) {
        try {
            final RichUser richUser = get(principal);
            updateUserLabChanges(richUser);
            return richUser;
        } catch (AccessDeniedException e) {
            return null;
        }
    }

    @ResponseBody
    @RequestMapping(value = "/getConsentToPrivacyPolicyDate", method = RequestMethod.GET)
    public ValueResponse getConsentToPrivacyPolicyDate(Principal principal) {
        final RichUser richUser = get(principal);
        final Date date = userReader.readConsentToPrivacyPolicyDate(richUser.getId());
        return new ValueResponse<>(date);
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/setConsentToPrivacyPolicyDate", method = RequestMethod.PUT)
    public void setConsentToPrivacyPolicyDate(Principal principal) {
        final RichUser richUser = get(principal);
        userManagement.setConsentToPrivacyPolicyDate(richUser.getId(), new Date());
    }

    @ResponseBody
    @RequestMapping(value = "/getAccountRemovalRequestDate", method = RequestMethod.GET)
    public ValueResponse getAccountRemovalRequestDate(Principal principal) {
        final RichUser richUser = get(principal);
        final Date date = userReader.readAccountRemovalRequestDate(richUser.getId());
        return new ValueResponse<>(date);
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/requestAccountRemoval", method = RequestMethod.PUT)
    public void requestAccountRemoval(Principal principal) {
        final RichUser richUser = get(principal);
        userManagement.setAccountRemovalRequestDate(richUser.getId(), new Date());
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/revokeAccountRemoval", method = RequestMethod.PUT)
    public void revokeAccountRemoval(Principal principal) {
        final RichUser richUser = get(principal);
        userManagement.setAccountRemovalRequestDate(richUser.getId(), EMPTY_DATE);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = "/invited")
    public SecurityHelper.UserDetails invitedUser(@RequestParam String link) {
        try {
            SecurityHelper.UserDetails userDetails = securityHelper.getUserDetailsByInvitationLink(link);
            return userDetails;
        } catch (AccessDeniedException e) {
            return null;
        }
    }

    private void updateUserLabChanges(RichUser richUser) {
        final long id = richUser.getId();
        final Set<Long> labs = securityHelper.getUserDetails(id).labs;
        if (labs.size() != richUser.getLabs().size() || !labs.containsAll(richUser.getLabs())) {
            updatePrincipal(id);
        }
    }

    @ResponseBody
    @RequestMapping("/loginResult")
    public SuccessErrorResponse getLoginResult(HttpSession session) {
        RuntimeException exception = (RuntimeException) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        if (exception == null) {
            return new SuccessErrorResponse(null, (String) session.getAttribute(SUCCESS_MESSAGE));
        }
        final Throwable exceptionCause = exception.getCause();
        if (exceptionCause != null && !AuthenticationException.class.isAssignableFrom(exceptionCause.getClass())) {
            return new SuccessErrorResponse("Something went wrong on server", null);
        }
        return new SuccessErrorResponse(exception.getMessage(), null);
    }

    @ResponseBody
    @RequestMapping("/isEmailVerified")
    public EmailVerifiedResponse isEmailVerified(HttpSession session) {
        final Boolean emailVerified = (Boolean) session.getAttribute(EMAIL_VERIFIED);
        return new EmailVerifiedResponse(emailVerified != null && emailVerified);
    }

    @ResponseBody
    @RequestMapping("/labs")
    public ImmutableSortedSet<LabItem> getLabs() {
        return registrationHelper.availableLabs();
    }

    @RequestMapping(value = "labRequest", method = RequestMethod.POST)
    @ResponseBody
    public SuccessErrorResponse requestLabs(@RequestBody LaboratoryOperationRequest laboratoryOperationRequest) {
        UserManagement.PersonInfo personInfoLab =
            new UserManagement.PersonInfo(laboratoryOperationRequest.getHeadFirstName(),
                laboratoryOperationRequest.getHeadLastName(), laboratoryOperationRequest.getHeadEmail()
            );
        try {
            labManagement.requestLabCreation(
                new LabManagementTemplate.LabInfoTemplate(laboratoryOperationRequest.getInstitutionUrl(), personInfoLab,
                    laboratoryOperationRequest.getName()
                ), laboratoryOperationRequest.getContactEmail());
        } catch (IllegalArgumentException e) {
            return new SuccessErrorResponse(e.getMessage(), null);
        }
        return new SuccessErrorResponse(null, "Lab creation request has been successfully sent");
    }

    @RequestMapping("/verifyEmail")
    public String verifyEmail(@RequestParam String email, @RequestParam String mac, HttpSession session) {
        if (!crypto.isMacValid(email, mac)) {
            LOGGER.warn("Mac code is not valid for email(\" {} \").", email);
            addExceptionDetailsIntoSession(
                session,
                "Email verification code is corrupted. Your email hasn't been verified."
            );
            return REDIRECT_TO_EMAIL_VERIFIED_PAGE;
        }
        final SecurityHelper.UserDetails userDetails = securityHelper.getUserDetailsByEmail(email);

        if (verificationLinkHasExpired(userDetails)) {
            LOGGER.warn("Verification link has expired for email(\" {} \").", email);
            addExceptionDetailsIntoSession(
                session,
                "Email verification code has expired. Your email hasn't been verified."
            );
            return REDIRECT_TO_EMAIL_VERIFIED_PAGE;
        }

        if (userDetails == null) {
            LOGGER.warn("Account with email(\" {} \") doesn't exist.", email);
            addExceptionDetailsIntoSession(session, "Account with this email doesn't exist.");
            return REDIRECT_TO_EMAIL_VERIFIED_PAGE;
        }
        if (userDetails.emailVerified) {
            addSuccessMessageIntoSession(session, "Your email has already been verified. Please log in.");
        } else {
            userManagement.verifyEmail(userDetails.id);
            addSuccessMessageIntoSession(session, "Your email was verified. Please log in.");
        }
        return REDIRECT_TO_EMAIL_VERIFIED_PAGE;
    }

    private void addSuccessMessageIntoSession(HttpSession session, String msg) {
        session.setAttribute(SUCCESS_MESSAGE, msg);
        session.setAttribute(EMAIL_VERIFIED, true);
        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }

    private void addExceptionDetailsIntoSession(HttpSession session, String msg) {
        session.setAttribute(
            WebAttributes.AUTHENTICATION_EXCEPTION,
            new AuthenticationCredentialsNotFoundException(msg)
        );
        session.setAttribute(EMAIL_VERIFIED, false);
        session.removeAttribute(SUCCESS_MESSAGE);
    }

    private boolean verificationLinkHasExpired(SecurityHelper.UserDetails userDetails) {

        if (userDetails.emailVerificationSentOnDate == null) {
            return true;
        }

        final Duration linkAge = Duration.between(userDetails.emailVerificationSentOnDate.toInstant(), Instant.now());

        // check if greater than allowed
        return linkAge.compareTo(Duration.ofHours(MAX_ALLOWED_VERIFICATION_LINK_AGE_IN_HOURS)) == 1;
    }

    private boolean passwordResetLinkHasExpired(SecurityHelper.UserDetails userDetails) {

        if (userDetails.passwordResetSentOnDate == null) {
            return true;
        }

        final Duration linkAge = Duration.between(userDetails.passwordResetSentOnDate.toInstant(), Instant.now());

        // check if greater than allowed
        return linkAge.compareTo(Duration.ofHours(MAX_ALLOWED_PASSWORD_RESET_LINK_AGE_IN_HOURS)) == 1;
    }

    @RequestMapping("/sendInstructions")
    @ResponseBody
    public SuccessErrorResponse sendInstructions(@RequestParam String email, HttpSession session)
        throws URISyntaxException {
        SecurityHelper.UserDetails userDetails = securityHelper.getUserDetailsByEmail(email);
        if (userDetails == null) {
            return new SuccessErrorResponse("Such email is not registered", null);
        }
        userManagement.sendPasswordRecoveryInstructions(userDetails.id, getPasswordRecoveryUrl(userDetails));
        return new SuccessErrorResponse(null, "Reset password instructions have been sent. Check your email.");
    }

    @RequestMapping("/resetPassword")
    @ResponseStatus(HttpStatus.OK)
    public void resetPassword(@RequestParam String email, @RequestParam String password, HttpSession session) {
        SecurityHelper.UserDetails userDetails = securityHelper.getUserDetailsByEmail(email);
        userManagement.resetPassword(userDetails.id, passwordEncoder.encode(password));
        session.setAttribute(SUCCESS_MESSAGE, "Your password has been reset.");
    }

    @RequestMapping("/isMacValid")
    @ResponseBody
    public SuccessErrorResponse isMacValid(@RequestParam String email, @RequestParam String mac, HttpSession session) {
        SecurityHelper.UserDetails userDetails = securityHelper.getUserDetailsByEmail(email);
        String resetPasswordMacString = getMacString(userDetails);
        if (crypto.isMacValid(resetPasswordMacString, mac)) {
            session.setAttribute(SUCCESS_MESSAGE, YOU_CAN_RESET_PASSWOR_YD);
            return new SuccessErrorResponse(null, MAC_IS_VALID);
        } else {
            return new SuccessErrorResponse(YOU_ARE_NOT_ABLE_TO_CHANGE_PASSWORD_WITH_THIS_LINK, null);
        }
    }

    @RequestMapping("/canResetPassword")
    @ResponseBody
    public SuccessErrorResponse canResetPassword(
        @RequestParam String email,
        @RequestParam String mac,
        HttpSession session
    ) {
        SecurityHelper.UserDetails userDetails = securityHelper.getUserDetailsByEmail(email);
        String resetPasswordMacString = getMacString(userDetails);
        final boolean linkHasExpired = passwordResetLinkHasExpired(userDetails);
        final boolean macIsValid = crypto.isMacValid(resetPasswordMacString, mac);
        if (macIsValid && !linkHasExpired) {
            session.setAttribute(SUCCESS_MESSAGE, YOU_CAN_RESET_PASSWOR_YD);
            return new SuccessErrorResponse(null, MAC_IS_VALID);
        } else {
            return new SuccessErrorResponse(YOU_ARE_NOT_ABLE_TO_CHANGE_PASSWORD_WITH_THIS_LINK, null);
        }
    }

    String getEmailVerificationUrl(String email) throws URISyntaxException {
        String mac = getMac(email);

        URIBuilder uriBuilder = new URIBuilder(chorusPropertiesProvider.getBaseUrl() + "/security/verifyEmail");
        uriBuilder.addParameter(EMAIL, email);
        uriBuilder.addParameter(MAC, mac);

        return uriBuilder.build().toString();
    }

    @RequestMapping("/labMembership")
    @ResponseStatus(HttpStatus.OK)
    public String manageLabMembershipDirectly(
        @RequestParam long userId,
        @RequestParam String token,
        @RequestParam long labId,
        @RequestParam long requestId,
        @RequestParam String action
    ) throws URISyntaxException {

        String securityString = String.valueOf(userId + labId + requestId);

        if (!crypto.isMacValid(securityString, token)) {
            return redirectTo404();
        }

        final SecurityHelper.UserDetails userDetails = securityHelper.getUserDetails(userId);

        UserManagement.LabMembershipRequestActions operation = null;
        if (APPROVE.equals(action)) {
            operation = UserManagementTemplate.LabMembershipRequestActions.APPROVE;
        }
        if (REFUSE.equals(action)) {
            operation = UserManagementTemplate.LabMembershipRequestActions.REFUSE;
        }

        try {
            String labName = userManagement.handleLabMembershipRequest(labId, requestId, operation);
            return getLabMembershipConfirmMessageUrl(userDetails.firstName, userDetails.lastName, labName, action);

        } catch (RequestAlreadyHandledException labName) {
            return getLabMembershipConfirmMessageUrl(
                userDetails.firstName,
                userDetails.lastName,
                labName.getMessage(),
                DEPRECATED
            );

        } catch (ObjectNotFoundException e) {
            return redirectTo404();
        }
    }

    private String getLabMembershipConfirmMessageUrl(
        String labMemberFirstName,
        String labMemberSecondName,
        String labName,
        String action
    ) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.addParameter(LAB_MEMBER_FIRST_NAME, labMemberFirstName);
        uriBuilder.addParameter(LAB_MEMBER_SECOND_NAME, labMemberSecondName);
        uriBuilder.addParameter(LAB_NAME, labName);
        uriBuilder.addParameter(RESULT, action);

        return "redirect:../pages/labMembershipMessage.html" + uriBuilder.build();
    }

    private String redirectTo404() {
        return "redirect:../pages/404.html";
    }

    @ResponseBody
    @RequestMapping("/emailAvailable")
    public boolean isEmailAvailable(@RequestParam String email) {
        return registrationHelper.isEmailAvailable(email);
    }

    @ResponseBody
    @RequestMapping("/emailActivated")
    public boolean isEmailActivated(@RequestParam String email) {
        return registrationHelper.isEmailActivated(email);
    }

    String getPasswordRecoveryUrl(SecurityHelper.UserDetails userDetails) throws URISyntaxException {
        String mac = getMac(userDetails);

        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.addParameter(EMAIL, userDetails.email);
        uriBuilder.addParameter(MAC, mac);

        // used this because URIBuilder does not support "#?" construction
        final String baseUrl = chorusPropertiesProvider.getBaseUrl();

        return baseUrl + "/pages/reset-password.html#?" + uriBuilder.build().getRawQuery();
    }

    String getMac(String email) {
        return crypto.getMac(email);
    }

    String getMac(SecurityHelper.UserDetails userDetails) {
        return crypto.getMac(getMacString(userDetails));
    }

    String getMacString(SecurityHelper.UserDetails userDetails) {
        return userDetails.email + userDetails.lastModification.getTime();
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void createAccount(@RequestBody AccountDetails input, HttpSession session) throws URISyntaxException {
        UserManagement.PersonInfo personInfo =
            new UserManagement.PersonInfo(input.firstName, input.lastName, input.email);

        userManagement.createPersonAndSendEmail(
            personInfo,
            input.password,
            input.laboratories,
            getEmailVerificationUrl(personInfo.email),
            new UserManagement.LabMembershipConfirmationUrlProvider() {
                @Override
                public String getUrl(
                    long user,
                    long lab,
                    long requestId,
                    UserManagement.LabMembershipRequestActions action
                ) throws URISyntaxException {
                    //send separate letter for every requested lab
                    return getLabMembershipManagementUrl(user, lab, requestId, action);
                }
            }
        );
        session.setAttribute(SUCCESS_MESSAGE, YOU_WERE_SUCCESSFULLY_REGISTERED);
    }

    @RequestMapping(method = RequestMethod.POST, value = "saveInvited")
    @ResponseStatus(HttpStatus.OK)
    public void saveInvited(@RequestBody AccountDetails input, HttpSession session) throws URISyntaxException {
        UserManagement.PersonInfo personInfo =
            new UserManagement.PersonInfo(input.firstName, input.lastName, input.email);
        final String encodedNewPassword = passwordEncoder.encode(input.password);
        userManagement.saveInvited(
            personInfo,
            encodedNewPassword,
            input.laboratories,
            getEmailVerificationUrl(personInfo.email),
            new UserManagement.LabMembershipConfirmationUrlProvider() {
                @Override
                public String getUrl(
                    long user,
                    long lab,
                    long requestId,
                    UserManagement.LabMembershipRequestActions action
                ) throws URISyntaxException {
                    //send separate letter for every requested lab
                    return getLabMembershipManagementUrl(user, lab, requestId, action);
                }
            }
        );
        session.setAttribute(SUCCESS_MESSAGE, YOU_WERE_SUCCESSFULLY_REGISTERED);
    }

    @RequestMapping("/resendActivationEmail")
    @ResponseStatus(HttpStatus.OK)
    public void resendActivationEmail(@RequestParam String email, HttpSession session) throws URISyntaxException {
        long userId = securityHelper.getUserDetailsByEmail(email).id;
        userManagement.resendActivationEmail(userId, getEmailVerificationUrl(email));
        session.setAttribute(SUCCESS_MESSAGE, "Activation email was resend. Please check your email");
    }

    @RequestMapping(method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public void updateAccount(@RequestBody AccountDetails input, Principal principal) throws URISyntaxException {
        UserManagement.PersonInfo personInfo =
            new UserManagement.PersonInfo(input.firstName, input.lastName, input.email);
        final long userId = getUserId(principal);

        userManagement.updatePersonAndSendEmail(
            userId,
            personInfo,
            input.laboratories,
            new UserManagement.LabMembershipConfirmationUrlProvider() {
                @Override
                public String getUrl(
                    long user,
                    long lab,
                    long requestId,
                    UserManagement.LabMembershipRequestActions action
                ) throws URISyntaxException {
                    //send separate letter for every requested lab
                    return getLabMembershipManagementUrl(user, lab, requestId, action);
                }
            }
        );
        updatePrincipal(userId);
    }

    private String getLabMembershipManagementUrl(
        long userId,
        long labId,
        long requestId,
        UserManagement.LabMembershipRequestActions operation
    ) throws URISyntaxException {

        String securityString = String.valueOf(userId + labId + requestId);
        final String token = crypto.getMac(securityString);
        String action = "";
        if (UserManagementTemplate.LabMembershipRequestActions.APPROVE.equals(operation)) {
            action = APPROVE;
        }
        if (UserManagementTemplate.LabMembershipRequestActions.REFUSE.equals(operation)) {
            action = REFUSE;
        }

        URIBuilder uriBuilder = new URIBuilder(chorusPropertiesProvider.getBaseUrl() + "/security/labMembership");
        uriBuilder.addParameter(USER_ID, String.valueOf(userId));
        uriBuilder.addParameter(LAB_ID, String.valueOf(labId));
        uriBuilder.addParameter(REQUEST_ID, String.valueOf(requestId));
        uriBuilder.addParameter(ACTION, action);
        uriBuilder.addParameter(TOKEN, token);

        return uriBuilder.build().toString();
    }

    @RequestMapping(value = "/changePassword", method = RequestMethod.PUT)
    @ResponseBody
    public SuccessErrorResponse changePassword(@RequestBody ChangePasswordDetails input, Principal principal)
        throws URISyntaxException {
        final long userId = getUserId(principal);
        final String encodedNewPassword = passwordEncoder.encode(input.newPassword);
        try {
            userManagement.changePassword(userId, input.oldPassword, encodedNewPassword);
        } catch (AccessDenied e) {
            return new SuccessErrorResponse(e.getMessage(), null);
        }
        if (input.newPassword.equals(input.oldPassword)) {
            return new SuccessErrorResponse("New and old password are equals", null);
        }
        updatePrincipal(userId);
        return new SuccessErrorResponse(null, "Password changed successfully");
    }

    @RequestMapping(value = "/inviteUser")
    @ResponseBody
    public SecurityHelper.UserDetails inviteUser(@RequestParam String email, Principal principal) {
        final long userId = getUserId(principal);
        final String invitationLink = userManagement.inviteUser(
            userId,
            email,
            "/pages/register.html#/registerInvited/" + UUID.randomUUID().toString()
        );
        return securityHelper.getUserDetailsByInvitationLink(invitationLink);
    }

    @ResponseBody
    @RequestMapping("/getEmailRequest")
    public ChangeEmailRequest isUserChangingEmail(Principal principal) {
        final long userId = getUserId(principal);
        final SecurityHelper.UserDetails userDetails = securityHelper.getUserDetails(userId);
        return new ChangeEmailRequest() {
            {
                oldEmail = userDetails.email;
                newEmail = userDetails.emailRequest;
            }
        };
    }

    @RequestMapping("/resendEmailRequest")
    @ResponseBody
    public SuccessErrorResponse resendEmailRequestNotification(Principal principal) throws URISyntaxException {
        final long userId = getUserId(principal);
        SecurityHelper.UserDetails userDetails = securityHelper.getUserDetails(userId);
        String changeEmailUrl = getEmailChangeUrl(userDetails);
        userManagement.sendEmailRequestInstructions(userDetails.id, userDetails.emailRequest, changeEmailUrl);
        return new SuccessErrorResponse(null, CHECK_YOUR_EMAIL_AND_CONFIRM_EMAIL_CHANGING);
    }

    @RequestMapping("/cancelEmailRequest")
    @ResponseBody
    public SuccessErrorResponse cancelEmailRequest(Principal principal) {
        final long userId = getUserId(principal);
        userManagement.removeChangeEmailRequest(userId);
        return new SuccessErrorResponse(null, "Email request has been removed");
    }

    @RequestMapping("/emailRequestConfirm")
    public String changeEmailConfirm(@RequestParam String email, @RequestParam String mac) throws URISyntaxException {
        SecurityHelper.UserDetails userDetails = securityHelper.getUserDetailsByEmail(email);
        if (userDetails == null || userDetails.emailRequest == null) {
            return REDIRECT_TO_DASHBOARD_PAGE;
        }
        String macString = getMacString(userDetails);
        boolean isMacValid = crypto.isMacValid(macString, mac);
        if (!isMacValid) {
            return REDIRECT_TO_DASHBOARD_PAGE;
        }
        UserManagement.PersonInfo newPersonInfo =
            new UserManagement.PersonInfo(userDetails.firstName, userDetails.lastName, userDetails.emailRequest);
        final long userId = userDetails.id;
        userManagement.updatePerson(userId, newPersonInfo, userDetails.labs);
        userManagement.removeChangeEmailRequest(userId);
        updatePrincipal(userId);
        return REDIRECT_TO_DASHBOARD_PAGE;
    }

    @RequestMapping(value = "/emailRequest", method = RequestMethod.PUT)
    @ResponseBody
    public SuccessErrorResponse changeEmail(@RequestBody ChangeEmailRequest emailRequest, Principal principal)
        throws URISyntaxException {
        String newEmail = emailRequest.newEmail;
        SecurityHelper.UserDetails detailsToCheck = securityHelper.getUserDetailsByEmail(newEmail);
        if (detailsToCheck != null) {
            return new SuccessErrorResponse("Such email is already registered", null);
        }
        final long userId = getUserId(principal);
        userManagement.createChangeEmailRequest(userId, newEmail);
        SecurityHelper.UserDetails userDetails = securityHelper.getUserDetails(userId);
        String changeEmailUrl = getEmailChangeUrl(userDetails);
        userManagement.sendEmailRequestInstructions(userDetails.id, newEmail, changeEmailUrl);
        return new SuccessErrorResponse(null, CHECK_YOUR_EMAIL_AND_CONFIRM_EMAIL_CHANGING);
    }

    @RequestMapping("/isLabHead")
    @ResponseBody
    public ValueResponse<Boolean> isLabHead(Principal principal) {
        final long userId = getUserId(principal);
        return new ValueResponse<Boolean>(labHeadManagement.isLabHead(userId));
    }

    @RequestMapping("/showBilling")
    @ResponseBody
    public ValueResponse<Boolean> showBilling(Principal principal) {
        final long userId = getUserId(principal);
        final Collection<Long> labs = labHeadManagement.findLabsForLabHead(userId);
        for (Long lab : labs) {
            if (securityHelper.isBillingEnabledForLab(lab)) {
                return new ValueResponse<>(true);
            }
        }
        return new ValueResponse<>(false);
    }


    @RequestMapping("/isLabHeadOfLab")
    @ResponseBody
    public ValueResponse<Boolean> isLabHeadOfLab(Principal principal, @RequestParam long lab) {
        return new ValueResponse<>(labManagement.isLabHead(getUserId(principal), lab));
    }

    @RequestMapping("/enabledBillingFeatures")
    @ResponseBody
    public Map<Long, Set<BillingFeature>> getEnabledBillingFeatures(@RequestParam Set<Long> labIds) {
        return labIds
            .stream()
            .collect(
                Collectors.toMap(
                    java.util.function.Function.identity(),
                    labId -> billingFeaturesHelper.enabledBillingFeatures(labId)
                )
            );
    }

    @RequestMapping("/enabledFeatures")
    @ResponseBody
    public Map<Long, Set<ApplicationFeature>> getEnabledFeatures(@RequestParam Set<Long> labIds) {
        return labIds
            .stream()
            .collect(
                Collectors.toMap(
                    java.util.function.Function.identity(),
                    labId -> featuresHelper.allEnabledForLab(labId)
                )
            );
    }

    @RequestMapping("/userLabsWithEnabledFeature")
    @ResponseBody
    public ImmutableSet<LabItem> userLabsWithEnabledFeature(@RequestParam final String feature, Principal principal) {
        if (principal == null) {
            return ImmutableSet.of();
        }

        final ImmutableSortedSet<LabItem> labs = registrationHelper.availableLabs();
        return from(securityHelper.getUserDetails(getUserId(principal)).labs)
            .transform(new Function<Long, LabItem>() {
                @Override
                public LabItem apply(final Long input) {
                    return find(labs, new Predicate<LabItem>() {
                        @Override
                        public boolean apply(LabItem item) {
                            return item.id == input;
                        }
                    });
                }
            })
            .filter(new Predicate<LabItem>() {
                @Override
                public boolean apply(LabItem input) {
                    return securityHelper.isFeatureEnabledForLab(feature, input.id);
                }
            })
            .toSet();
    }

    @RequestMapping("/shouldShowBillingNotification")
    @ResponseBody
    public ValueResponse<Boolean> shouldShowBillingNotification(Principal principal) {
        final long userId = getUserId(principal);
        final UserPreferencesInfo userPreferencesInfo = userPreferencesReader.readUserPreferences(userId);

        return new ValueResponse<>(userPreferencesInfo.shouldShowBillingNotification);
    }

    @RequestMapping(value = "/removeBillingNotification", method = RequestMethod.PUT)
    @ResponseBody
    public void removeBillingNotification(Principal principal) {
        userPreferencesManagement.removeBillingNotification(getUserId(principal));
    }

    @RequestMapping(value = "/generateClientToken")
    @ResponseBody
    public ClientToken generateClientToken(Principal principal) {
        return clientTokenService.generateTokenForUser(getUserId(principal));
    }

    String getEmailChangeUrl(SecurityHelper.UserDetails userDetails) throws URISyntaxException {
        String mac = getMac(userDetails);
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.addParameter(EMAIL, userDetails.email);
        uriBuilder.addParameter(MAC, mac);

        final String baseUrl = chorusPropertiesProvider.getBaseUrl();

        return baseUrl + "/security/emailRequestConfirm?" + uriBuilder.build().getRawQuery();
    }

    private void updatePrincipal(long userId) {
        final UserDetails userDetails = chorusUserProvider.getUserDetails(userId);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                userDetails, userDetails.getPassword(), userDetails.getAuthorities()));
    }

    public static class AccountDetails {
        public String firstName;
        public String lastName;
        public String email;
        public String password;
        public Set<Long> laboratories;
    }

    public static class ChangePasswordDetails {
        public String oldPassword;
        public String newPassword;
    }

    public static class ChangeEmailRequest {
        public String oldEmail;
        public String newEmail;
    }

    public static class EmailVerifiedResponse {
        public final boolean verified;

        public EmailVerifiedResponse(boolean verified) {
            this.verified = verified;
        }
    }
}
