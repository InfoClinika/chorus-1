package com.infoclinika.mssharing.platform.model;

import java.util.List;

/**
 * @author Herman Zamula
 */
public interface NotifierTemplate {

    void projectShared(long whoShared, long withWhomShared, long project);

    void projectCopied(long newOwner, long oldOwner, long newProject);

    void removingFromProject(long person, long project);

    void removingFromProjectWithCreatingNew(long person, long project, long newProject);

    void labCreationApproved(String requesterEmail, long newLabId);

    void labCreationRejected(String requesterEmail, String comment, String labName);

    void projectSharingApproved(long requester, String projectName, List<String> downloadExperimentLinks);

    void projectSharingRejected(long requester, String projectName, String comment);

    /**
     * If there are several different request processing result, like one admin approve creation and second refuse
     * First one wins. Second should be notified.
     */
    void staleOnLabRequest(long actor, long labRequestId);

    void userWasAddedToOperators(long actor, long newOperator, long instrument);

    void instrumentRequest(long requester, long operator, String instrumentName);

    void instrumentRequestApproved(long actor, long initiator, long instrument);

    void instrumentRequestRefuse(long actor, long initiator, long instrument, String comment);

    /**
     * If there are several different request processing result, like one operator approve adding operator and second
     * one refuse
     * First one wins. Second should be notified.
     */
    void staleOnInstrumentRequest(long actor, long instrument, long initiator);

    void userRegistered(long user, String verificationUrl);

    void recoverPassword(long userId, String passwordRecoveryUrl);

    void labMembershipApproved(long requester, long lab);

    void labMembershipRejected(long requester, long lab, String comment);

    void sendGeneratedPassword(long labHead, String password);

    void sendExperimentPublicDownloadLink(long actor, long experiment, String sendToEmail, String downloadLink);

    void sendExperimentPrivateDownloadLink(long actor, long experiment, String sendToEmail, String downloadLink);

    void sendLabCreationRequestNotification(long admin,
                                            String requesterEmail,
                                            String headName,
                                            String labName,
                                            String headEmail);

    void sendLabCreatedNotification(long head, String labName);

    void sendProjectSharingRequestNotification(long projectCreator, long requester, long project, long experiment);

    void emailChange(long userId, String newEmail, String emailChangeUrl);

    void sendIssueToEmail(long actor, String issueTitle, String issueContents, String destinationEmail);

    void sendInstrumentCreationRequestNotification(long labHead,
                                                   String requesterEmail,
                                                   String labName,
                                                   String instrumentName);

    void staleOnNewInstrumentRequest(long actor, long request);

    void sendLabMembershipRequest(long labHeadId,
                                  String labName,
                                  long applicantId,
                                  String approveUrl,
                                  String refuseUrl);

    void sendInstrumentCreationApprovedNotification(long requester, String labName, String instrumentName);

    void sendInstrumentCreationRejectedNotification(long requester,
                                                    String comment,
                                                    String labName,
                                                    String instrumentName);

    void sendInvitationEmail(long invitedBy, String destinationEmail, String link);
}
