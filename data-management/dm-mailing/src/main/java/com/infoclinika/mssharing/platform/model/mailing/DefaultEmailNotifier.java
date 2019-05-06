package com.infoclinika.mssharing.platform.model.mailing;

import com.google.common.base.Splitter;
import com.infoclinika.mssharing.platform.model.NotifierTemplate;
import com.infoclinika.mssharing.platform.model.helper.MailSendingHelperTemplate;
import com.infoclinika.mssharing.platform.model.helper.MailSendingHelperTemplate.UserDetails;
import com.infoclinika.mssharing.propertiesprovider.ChorusPropertiesProvider;
import com.infoclinika.mssharing.propertiesprovider.MailPropertiesProvider;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.ui.velocity.VelocityEngineUtils;

import javax.inject.Inject;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.trimToEmpty;

/**
 * @author Herman Zamula
 */
@Component
public class DefaultEmailNotifier implements NotifierTemplate {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEmailNotifier.class);
    private static final String HTML_TEMPLATE = "html-common.vm";

    @Inject
    protected EmailerTemplate emailer;

    @Inject
    protected VelocityEngine velocityEngine;

    @Inject
    protected MailSendingHelperTemplate mailSendingHelper;

    @Inject
    protected ChorusPropertiesProvider chorusPropertiesProvider;

    @Inject
    protected MailPropertiesProvider mailPropertiesProvider;

    protected void send(String to, String template, Map<String, Object> model) {
        final Email email = prepareEmail(template, model);
        emailer.send(to, email.subject, email.message);
    }

    protected Email prepareEmail(String template, Map<String, Object> model) {
        final Email email = new Email();
        String message;
        model.put("url", chorusPropertiesProvider.getBaseUrl());
        model.put("currentYear", Calendar.getInstance().get(Calendar.YEAR));
        model.put("projectTitle", chorusPropertiesProvider.getProjectTitle());
        model.put("imagesPrefix", mailPropertiesProvider.getImagesPrefix());
        try {
            message = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, template, model);
        } catch (VelocityException e) {
            throw new RuntimeException(
                "Could not compose message from template " + template + " with model " + model,
                e
            );
        }

        final List<String> paragraphs = Splitter.on("\n").splitToList(message);
        if (paragraphs.size() < 2) {
            throw new IllegalStateException(
                "Cannot send message: a title should be present and a body should have at least one line. Template " +
                    "used: " +
                    template + " with model: " + model);
        }
        email.subject = paragraphs.get(0);
        model.put("paragraphs", paragraphs.subList(1, paragraphs.size()));
        try {
            email.message =
                VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, getTemplateLocation(HTML_TEMPLATE), model);
        } catch (VelocityException e) {
            throw new RuntimeException(
                "Could not compose message from template " + getTemplateLocation(HTML_TEMPLATE) + " with model " +
                    model,
                e
            );
        }
        return email;
    }

    protected static class Email {
        String subject;
        String message;
    }

    @Override
    public void projectShared(long whoShared, long withWhomShared, long project) {
        Map<String, Object> model = new HashMap<>();

        MailSendingHelperTemplate.UserDetails withWhom = mailSendingHelper.userDetails(withWhomShared);
        model.put("sharedToUser", withWhom.name);
        model.put("user", mailSendingHelper.userDetails(whoShared).name);
        model.put("project", mailSendingHelper.projectName(project));
        send(withWhom.email, getTemplateLocation("projectShared.vm"), model);
    }

    @Override
    public void projectCopied(long newOwner, long oldOwner, long newProject) {
        Map<String, Object> model = new HashMap<>();

        UserDetails withWhom = mailSendingHelper.userDetails(newOwner);
        model.put("newOwner", withWhom.name);
        model.put("oldOwner", mailSendingHelper.userDetails(oldOwner).name);
        model.put("project", mailSendingHelper.projectName(newProject));
        send(withWhom.email, getTemplateLocation("projectCopied.vm"), model);
    }

    @Override
    public void removingFromProject(long person, long project) {
        Map<String, Object> model = new HashMap<>();
        model.put("project", mailSendingHelper.projectName(project));
        model.put("user", mailSendingHelper.userDetails(person).name);
        send(
            mailSendingHelper.userDetails(person).email,
            getTemplateLocation("removingFromProject.vm"),
            model
        );
    }

    @Override
    public void removingFromProjectWithCreatingNew(long person, long project, long newProject) {
        Map<String, Object> model = new HashMap<>();
        model.put("project", mailSendingHelper.projectName(project));
        model.put("user", mailSendingHelper.userDetails(person).name);
        send(
            mailSendingHelper.userDetails(person).email,
            getTemplateLocation("removingFromProjectWithCreatingNew.vm"),
            model
        );
    }

    @Override
    public void labCreationApproved(String requesterEmail, long newLabId) {
        final Map<String, Object> map = new HashMap<>();
        map.put("lab", mailSendingHelper.labName(newLabId));
        send(
            requesterEmail,
            getTemplateLocation("labCreationApproved.vm"),
            map
        );
    }

    @Override
    public void labCreationRejected(String requesterEmail, String comment, String labName) {
        final HashMap<String, Object> params = new HashMap<>();
        params.put("comment", comment);
        params.put("lab", labName);
        send(
            requesterEmail,
            getTemplateLocation("labCreationRejected.vm"),
            params
        );
    }

    @Override
    public void projectSharingApproved(long requester, String projectName, List<String> downloadExperimentLinks) {
        final Map<String, Object> params = new HashMap<>();
        final UserDetails requesterInfo = mailSendingHelper.userDetails(requester);
        params.put("requester", requesterInfo.name);
        params.put("experimentLinks", downloadExperimentLinks);
        params.put("project", projectName);
        send(
            requesterInfo.email,
            getTemplateLocation("projectSharingApproved.vm"),
            params
        );
    }

    @Override
    public void projectSharingRejected(long requester, String projectName, String comment) {
        final Map<String, Object> params = new HashMap<>();
        final UserDetails requesterInfo = mailSendingHelper.userDetails(requester);
        params.put("requester", requesterInfo.name);
        params.put("project", projectName);
        params.put("comment", comment);
        send(
            requesterInfo.email,
            getTemplateLocation("projectSharingRejected.vm"),
            params
        );
    }

    @Override
    public void staleOnLabRequest(long actor, long labRequestId) {
        final HashMap<String, Object> params = new HashMap<>();
        params.put("user", mailSendingHelper.userDetails(actor).name);
        send(mailSendingHelper.userDetails(actor).email,
            getTemplateLocation("staleOnLabRequest.vm"), params
        );
    }

    @Override
    public void userWasAddedToOperators(long actor, long newOperator, long instrument) {
        Map<String, Object> model = new HashMap<>();
        model.put("instrument", mailSendingHelper.instrumentName(instrument));
        model.put("user", mailSendingHelper.userDetails(newOperator).name);
        send(
            mailSendingHelper.userDetails(newOperator).email,
            getTemplateLocation("userWasAddedToOperators.vm"),
            model
        );
    }

    @Override
    public void instrumentRequest(long requesterId, long operatorId, String instrumentName) {
        final UserDetails requester = mailSendingHelper.userDetails(requesterId);
        final UserDetails operator = mailSendingHelper.userDetails(operatorId);
        Map<String, Object> model = new HashMap<>();
        model.put("operatorName", operator.name);
        model.put("requesterEmail", requester.email);
        model.put("requesterName", requester.name);
        model.put("instrumentName", instrumentName);
        send(operator.email, getTemplateLocation("instrumentRequest.vm"), model);
    }

    @Override
    public void instrumentRequestApproved(long actor, long initiator, long instrument) {
        Map<String, Object> model = new HashMap<>();
        model.put("instrument", mailSendingHelper.instrumentName(instrument));
        model.put("user", mailSendingHelper.userDetails(actor).name);
        send(
            mailSendingHelper.userDetails(initiator).email,
            getTemplateLocation("instrumentRequestApproved.vm"),
            model
        );
    }

    @Override
    public void instrumentRequestRefuse(long actor, long initiator, long instrument, String comment) {
        final HashMap<String, Object> params = new HashMap<>();
        params.put("user", mailSendingHelper.userDetails(initiator).name);
        params.put("comment", comment);
        params.put("instrument", mailSendingHelper.instrumentName(instrument));
        send(
            mailSendingHelper.userDetails(initiator).email,
            getTemplateLocation("instrumentRequestRefuse.vm"),
            params
        );
    }

    @Override
    public void staleOnInstrumentRequest(long actor, long instrument, long initiator) {
        Map<String, Object> model = new HashMap<>();
        model.put("user", mailSendingHelper.userDetails(actor).name);
        model.put("requester", mailSendingHelper.userDetails(initiator).name);
        model.put("instrument", mailSendingHelper.instrumentName(instrument));
        send(
            mailSendingHelper.userDetails(actor).email,
            getTemplateLocation("staleOnInstrumentRequest.vm"),
            model
        );
    }

    @Override
    public void userRegistered(long user, String verificationUrl) {
        Map<String, Object> model = new HashMap<>();
        model.put("verificationUrl", verificationUrl);
        model.put("user", mailSendingHelper.userDetails(user).name);
        send(
            mailSendingHelper.userDetails(user).email,
            getTemplateLocation("userRegistered.vm"),
            model
        );
    }

    @Override
    public void recoverPassword(long userId, String passwordRecoveryUrl) {
        Map<String, Object> model = new HashMap<>();
        model.put("passwordRecoveryUrl", passwordRecoveryUrl);
        model.put("user", mailSendingHelper.userDetails(userId).name);
        send(
            mailSendingHelper.userDetails(userId).email,
            getTemplateLocation("recoverPassword.vm"),
            model
        );
    }

    @Override
    public void labMembershipApproved(long requester, long lab) {
        Map<String, Object> model = new HashMap<>();
        model.put("lab", mailSendingHelper.labName(lab));
        model.put("user", mailSendingHelper.userDetails(requester).name);
        send(
            mailSendingHelper.userDetails(requester).email,
            getTemplateLocation("labMembershipApproved.vm"),
            model
        );
    }

    @Override
    public void labMembershipRejected(long requester, long lab, String comment) {
        Map<String, Object> model = new HashMap<>();
        model.put("user", mailSendingHelper.userDetails(requester).name);
        model.put("lab", mailSendingHelper.labName(lab));
        model.put("comment", comment);
        send(
            mailSendingHelper.userDetails(requester).email,
            getTemplateLocation("labMembershipRejected.vm"),
            model
        );
    }

    @Override
    public void sendGeneratedPassword(long person, String password) {
        String email = mailSendingHelper.userDetails(person).email;
        Map<String, Object> model = new HashMap<>();
        model.put("password", password);
        model.put("user", mailSendingHelper.userDetails(person).name);
        send(
            email,
            getTemplateLocation("sendGeneratedPassword.vm"),
            model
        );
    }

    @Override
    public void sendExperimentPublicDownloadLink(long actor, long experiment, String sendToEmail, String downloadLink) {
        Map<String, Object> model = new HashMap<>();
        UserDetails details = mailSendingHelper.userDetails(actor);
        model.put("user", details.name);
        model.put("experiment", mailSendingHelper.experimentName(experiment));
        model.put("downloadLink", downloadLink);
        send(sendToEmail, getTemplateLocation("publicExperimentDownload.vm"), model);
    }

    @Override
    public void sendExperimentPrivateDownloadLink(long actor,
                                                  long experiment,
                                                  String sendToEmail,
                                                  String downloadLink) {
        Map<String, Object> model = new HashMap<>();
        UserDetails details = mailSendingHelper.userDetails(actor);
        model.put("user", details.name);
        model.put("experiment", mailSendingHelper.experimentName(experiment));
        model.put("downloadLink", downloadLink);
        send(sendToEmail, getTemplateLocation("privateExperimentDownload.vm"), model);
    }

    @Override
    public void sendLabCreationRequestNotification(long admin,
                                                   String requesterEmail,
                                                   String headName,
                                                   String labName,
                                                   String headEmail) {
        Map<String, Object> model = new HashMap<>();
        UserDetails details = mailSendingHelper.userDetails(admin);
        model.put("admin", details.name);
        model.put("laboratory", labName);
        model.put("labHead", headName);
        model.put("requesterEmail", requesterEmail);
        model.put("labHeadEmail", headEmail);
        send(details.email, getTemplateLocation("labCreationNotificationRequest.vm"), model);
    }

    @Override
    public void sendLabCreatedNotification(long head, String labName) {
        final HashMap<String, Object> model = new HashMap<>();
        final UserDetails headDetails = mailSendingHelper.userDetails(head);
        model.put("labHead", headDetails.name);
        model.put("laboratory", labName);
        send(headDetails.email, getTemplateLocation("labCreated.vm"), model);
    }

    @Override
    public void sendProjectSharingRequestNotification(long projectCreatorId,
                                                      long requester,
                                                      long project,
                                                      long experiment) {
        Map<String, Object> model = new HashMap<>();
        UserDetails projectCreator = mailSendingHelper.userDetails(projectCreatorId);
        model.put("projectCreator", projectCreator.name);
        model.put("user", mailSendingHelper.userDetails(requester).name);
        model.put("experiment", mailSendingHelper.experimentName(experiment));
        model.put("project", mailSendingHelper.projectName(project));
        send(projectCreator.email, getTemplateLocation("projectSharingRequestNotification.vm"), model);
    }


    @Override
    public void emailChange(long userId, String newEmail, String emailChangeUrl) {
        UserDetails userDetails = mailSendingHelper.userDetails(userId);
        Map<String, Object> model = new HashMap<>();
        model.put("user", userDetails.name);
        model.put("emailChangeUrl", emailChangeUrl);
        send(newEmail, getTemplateLocation("emailChange.vm"), model);
    }

    @Override
    public void sendIssueToEmail(long actor, String issueTitle, String issueContents, String destinationEmail) {
        final UserDetails details = mailSendingHelper.userDetails(actor);

        LOGGER.debug(
            "Mailing an issue: title: {} ; content : {} to email: ",
            issueTitle,
            issueContents,
            destinationEmail
        );
        //Template variables
        Map<String, Object> model = new HashMap<>();
        model.put("username", details.name);
        model.put("issueTitle", issueTitle);
        model.put("issueContents", issueContents);
        send(destinationEmail, getTemplateLocation("issueCreated.vm"), model);
    }

    @Override
    public void sendLabMembershipRequest(long labHeadId,
                                         String labName,
                                         long applicantId,
                                         String approveUrl,
                                         String refuseUrl) {
        final UserDetails labHead = mailSendingHelper.userDetails(labHeadId);
        final UserDetails applicant = mailSendingHelper.userDetails(applicantId);

        Map<String, Object> model = new HashMap<>();
        model.put("labHead", labHead.name);
        model.put("laboratory", labName);
        model.put("requesterName", applicant.name);
        model.put("approveUrl", approveUrl);
        model.put("refuseUrl", refuseUrl);
        send(labHead.email, getTemplateLocation("labMembershipRequest.vm"), model);
    }

    @Override
    public void sendInstrumentCreationRequestNotification(long labHead,
                                                          String requesterEmail,
                                                          String labName,
                                                          String instrumentName) {

        final Map<String, Object> model = new HashMap<>();
        final UserDetails details = mailSendingHelper.userDetails(labHead);

        model.put("labHead", details.name);
        model.put("laboratory", labName);
        model.put("requesterEmail", requesterEmail);
        model.put("instrument", instrumentName);

        send(details.email, getTemplateLocation("instrumentCreationNotificationRequest.vm"), model);

    }

    @Override
    public void staleOnNewInstrumentRequest(long actor, long request) {

        final HashMap<String, Object> params = new HashMap<>();
        final UserDetails userDetails = mailSendingHelper.userDetails(actor);

        params.put("user", userDetails.name);

        send(userDetails.email, getTemplateLocation("staleOnNewInstrumentRequest.vm"), params);
    }

    @Override
    public void sendInstrumentCreationApprovedNotification(long requester, String labName, String instrumentName) {

        final Map<String, Object> model = new HashMap<>();
        final UserDetails details = mailSendingHelper.userDetails(requester);

        model.put("user", details.name);
        model.put("laboratory", labName);
        model.put("instrument", instrumentName);

        send(details.email, getTemplateLocation("instrumentCreationApprovedNotification.vm"), model);

    }

    @Override
    public void sendInstrumentCreationRejectedNotification(long requester,
                                                           String comment,
                                                           String labName,
                                                           String instrumentName) {

        final Map<String, Object> model = new HashMap<>();
        final UserDetails details = mailSendingHelper.userDetails(requester);

        model.put("user", details.name);
        model.put("laboratory", labName);
        model.put("instrument", instrumentName);
        model.put("comment", comment);

        send(details.email, getTemplateLocation("instrumentCreationRejectedNotification.vm"), model);
    }

    @Override
    public void sendInvitationEmail(long actor, String destinationEmail, String link) {
        final UserDetails invitedBy = mailSendingHelper.userDetails(actor);
        Map<String, Object> model = new HashMap<>();
        model.put("user", invitedBy.name);
        model.put("projectUrl", chorusPropertiesProvider.getBaseUrl() + link);
        send(destinationEmail, getTemplateLocation("userInvitation.vm"), model);
    }

    protected final String getTemplateLocation(String name) {
        return trimToEmpty(mailPropertiesProvider.getTemplatesLocation()) + name;
    }

}
