/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika,
 * Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use,
 * duplication or disclosure by the
 */
package com.infoclinika.mssharing.model;

import com.infoclinika.mssharing.platform.model.NotifierTemplate;

import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

/**
 * @author Stanislav Kurilin
 */
public interface Notifier extends NotifierTemplate {

    void setEnabled(boolean enabled);

    void sendFileReadyToDownloadNotification(long actor, Collection<Long> files);

    void sendCopyProjectRequestNotification(long requester, String senderFullName, String activeProjectName);

    void sendFailedEmailsNotification(String email, Set<String> failedEmails, Set<Long> failedRecordsIds);

    void sendMicroArraysImportFailedNotification(long receiver, String packageNumber, String errorMessage);

    void sendIssueDeletedByAdminNotification(long receiver, long issueId, String issueName);

    void sendAccountRemovalRequestedNotification(long requester, Date requestDate);

    void sendAccountRemovalRevokedNotification(long requester, Date revokeDate);

    void sendAccountRemovedNotification(String email);
}
