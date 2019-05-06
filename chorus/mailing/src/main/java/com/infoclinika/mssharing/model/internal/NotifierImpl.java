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
package com.infoclinika.mssharing.model.internal;

import com.google.common.collect.Maps;
import com.infoclinika.mssharing.blog.BlogNotifier;
import com.infoclinika.mssharing.blog.persistence.BlogPost;
import com.infoclinika.mssharing.blog.persistence.Comment;
import com.infoclinika.mssharing.model.AdminNotifier;
import com.infoclinika.mssharing.model.Notifier;
import com.infoclinika.mssharing.model.helper.MailSendingHelper;
import com.infoclinika.mssharing.platform.model.InboxNotifierTemplate;
import com.infoclinika.mssharing.platform.model.helper.MailSendingHelperTemplate.UserDetails;
import com.infoclinika.mssharing.platform.model.mailing.DefaultEmailNotifier;
import com.infoclinika.mssharing.platform.model.mailing.EmailerTemplate;
import com.infoclinika.mssharing.propertiesprovider.MailPropertiesProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.String.join;
import static java.util.stream.Collectors.toSet;

/**
 * @author Stanislav Kurilin
 */
@Component("notifier")
class NotifierImpl extends DefaultEmailNotifier implements Notifier, BlogNotifier, AdminNotifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotifierImpl.class);
    private static final String POST = "post";
    private static final String POST_URL = "postUrl";
    private static final String BLOG_URL = "blogUrl";
    private static final String COMMENT = "comment";
    private static final String BLOG_SUBSCRIBER = "blogSubscriber";
    private static final String ERROR_MESSAGE = "errorMessage";
    private static final String USER = "user";
    private static final String USER_ID = "userId";
    private static final String USER_EMAIL = "userEmail";
    private static final String DATE = "date";
    private static final String DOWNLOAD_URL = "downloadUrl";
    private static final String PROJECT = "project";
    private static final String SENDER = "sender";
    private static final String TITLE = "title";
    private static final String BODY = "body";
    private static final String FILE_DOWNLOAD_LINK = "fileDownloadLink";
    private static final String FAILED_EMAILS = "failedEmails";
    private static final String FAILED_RECORDS_IDS = "failedRecordsIds";
    private static final String PACKAGE_NUMBER = "packageNumber";
    private static final String ISSUE_ID = "issueId";
    private static final String ISSUE_TITLE = "issueTitle";
    private static final String DELIMITER = ", ";

    private static final DateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");

    private final MailSendingHelper mailSendingHelper;
    private final EmailerTemplate realEmailer;
    private final InboxNotifierTemplate inboxNotifier;
    private final MailPropertiesProvider mailPropertiesProvider;

    private EmailerTemplate mockEmailer = new MockEmailer();

    @Inject
    public NotifierImpl(MailSendingHelper mailSendingHelper,
                        EmailerTemplate realEmailer,
                        InboxNotifierTemplate inboxNotifier,
                        MailPropertiesProvider mailPropertiesProvider) {
        this.mailSendingHelper = mailSendingHelper;
        this.realEmailer = realEmailer;
        this.inboxNotifier = inboxNotifier;
        this.mailPropertiesProvider = mailPropertiesProvider;
    }

    @PostConstruct
    public void enableProperEmailer() {
        setEnabled(true);
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.emailer = enabled ? realEmailer : mockEmailer;
    }

    @Override
    protected void send(String to, String template, Map<String, Object> model) {
        if (mailSendingHelper.isSkipSending(to)) {
            LOGGER.info("Skip sending email to: \"{}\". Marked as ignored", to);
            return;
        }
        try {
            super.send(to, template, model);
        } catch (Exception ex) {
            LOGGER.warn("Couldn't send an email. To: {}, Template: {}", to, template, ex);
        }
    }

    @Override
    public void postAdded(long subscriber, BlogPost post) {
        Map<String, Object> model = new HashMap<>();
        String blogUrl = chorusPropertiesProvider.getBaseUrl() + "/pages/blog.html#/" + post.getBlog().getId();
        String postUrl = blogUrl + '/' + post.getId();
        model.put(POST, post);
        model.put(POST_URL, postUrl);
        model.put(BLOG_URL, blogUrl);

        UserDetails details = mailSendingHelper.userDetails(subscriber);
        send(details.email, getTemplateLocation("blogPostAdded.vm"), model);

        inboxNotifier.notify(post.getAuthor().getId(), subscriber,
                             "<a href=\"" + postUrl + "\">" + post.getTitle() + "</a> - blog post added"
        );
    }

    @Override
    public void commentAdded(long subscriber, Comment comment) {
        Map<String, Object> model = new HashMap<>();
        String blogUrl = chorusPropertiesProvider.getBaseUrl() + "/pages/blog.html#/" +
            comment.getPost().getBlog().getId();
        String postUrl = blogUrl + '/' + comment.getPost().getId();
        model.put(COMMENT, comment);
        model.put(POST_URL, postUrl);
        model.put(BLOG_URL, blogUrl);

        UserDetails details = mailSendingHelper.userDetails(subscriber);
        send(details.email, getTemplateLocation("commentAdded.vm"), model);

        inboxNotifier.notify(comment.getPost().getAuthor().getId(), subscriber,
                             "<a href=\"" + postUrl + "\">" + comment.getPost().getTitle() + "</a> - comment added"
        );
    }

    @Override
    public void postEdited(long subscriber, BlogPost post) {
        Map<String, Object> model = new HashMap<>();
        String blogUrl = chorusPropertiesProvider.getBaseUrl() + "/pages/blog.html#/" + post.getBlog().getId();
        String postUrl = blogUrl + '/' + post.getId();
        model.put(POST, post);
        model.put(POST_URL, postUrl);
        model.put(BLOG_URL, blogUrl);

        UserDetails details = mailSendingHelper.userDetails(subscriber);
        send(details.email, getTemplateLocation("blogPostEdited.vm"), model);

        inboxNotifier.notify(post.getAuthor().getId(), subscriber,
                             "<a href=\"" + postUrl + "\">" + post.getTitle() + "</a> - blog post edited"
        );
    }


    @Override
    public void postDeleted(long subscriber, BlogPost post, boolean blogSubscriber) {
        Map<String, Object> model = new HashMap<>();
        String blogUrl = chorusPropertiesProvider.getBaseUrl() + "/pages/blog.html#/" + post.getBlog().getId();
        model.put(POST, post);
        model.put(BLOG_URL, blogUrl);
        model.put(BLOG_SUBSCRIBER, blogSubscriber);

        UserDetails details = mailSendingHelper.userDetails(subscriber);
        send(details.email, getTemplateLocation("blogPostDeleted.vm"), model);

        inboxNotifier.notify(post.getAuthor().getId(), subscriber,
                             "\"" + post.getTitle() + "\" has been deleted from <a href=\"" + blogUrl + "\">" +
                                 post.getBlog().getName() + "</a>"
        );
    }

    @Override
    public void sendFileReadyToDownloadNotification(long actor, Collection<Long> files) {
        Map<String, Object> model = new HashMap<>();
        final UserDetails details = mailSendingHelper.userDetails(actor);
        StringBuilder downloadUrl = new StringBuilder(chorusPropertiesProvider.getBaseUrl() + "/download/bulk?");
        for (Long id : files) {
            downloadUrl.append("files=").append(id).append("&");
        }
        downloadUrl.append("experiment=");
        model.put(USER, details.name);
        model.put(DOWNLOAD_URL, downloadUrl.toString());
        send(details.email, getTemplateLocation("fileReadyTodownload.vm"), model);
    }

    @Override
    public void sendCopyProjectRequestNotification(long receiver, String senderFullName, String activeProjectName) {

        final UserDetails details = mailSendingHelper.userDetails(receiver);

        final Map<String, Object> model = Maps.newHashMap();
        model.put(USER, details.name);
        model.put(PROJECT, activeProjectName);
        model.put(SENDER, senderFullName);

        send(details.email, getTemplateLocation("copyProjectNotification.vm"), model);
    }

    @Override
    public void sendCommonEmail(long receiver, String title, String body) {

        final UserDetails details = mailSendingHelper.userDetails(receiver);

        final Map<String, Object> model = Maps.newHashMap();
        model.put(USER, details.name);
        model.put(TITLE, title);
        model.put(BODY, body);

        send(details.email, getTemplateLocation("adminBroadcastMessage.vm"), model);

    }

    @Override
    public void sendFailedEmailsNotification(String email, Set<String> failedEmails, Set<Long> failedRecordsIds) {

        final Map<String, Object> model = Maps.newHashMap();

        model.put(FAILED_EMAILS, join(DELIMITER, failedEmails));

        final String filedRecordIds = join(DELIMITER, failedRecordsIds.stream().map(Object::toString).collect(toSet()));

        model.put(
            FAILED_RECORDS_IDS,
            filedRecordIds
        );

        send(email, getTemplateLocation("failedEmailsAction.vm"), model);

    }

    @Override
    public void sendMicroArraysImportFailedNotification(long receiver, String packageNumber, String errorMessage) {
        final Map<String, Object> model = new HashMap<>();
        final UserDetails details = mailSendingHelper.userDetails(receiver);

        model.put(USER, details.name);
        model.put(PACKAGE_NUMBER, packageNumber);
        model.put(ERROR_MESSAGE, errorMessage);

        send(details.email, getTemplateLocation("microArraysImportFailedNotification.vm"), model);
    }

    @Override
    public void sendIssueDeletedByAdminNotification(long receiver, long issueId, String issueTitle) {
        final Map<String, Object> model = new HashMap<>();
        final UserDetails details = mailSendingHelper.userDetails(receiver);

        model.put(USER, details.name);
        model.put(ISSUE_ID, issueId);
        model.put(ISSUE_TITLE, issueTitle);

        send(details.email, getTemplateLocation("issueDeletedByAdminNotification.vm"), model);
    }

    @Override
    public void sendAccountRemovalRequestedNotification(long requester, Date requestDate) {
        sendAccountRemovalNotification(requester, requestDate, "accountRemovalRequestedNotification.vm");
    }

    @Override
    public void sendAccountRemovalRevokedNotification(long requester, Date revokeDate) {
        sendAccountRemovalNotification(requester, revokeDate, "accountRemovalRevokedNotification.vm");
    }

    @Override
    public void sendAccountRemovedNotification(String email) {
        final Map<String, Object> model = new HashMap<>();
        model.put(USER_EMAIL, email);
        send(email, getTemplateLocation("accountRemovedNotification.vm"), model);
    }

    private void sendAccountRemovalNotification(long requester, Date date, String template) {

        final Map<String, Object> model = new HashMap<>();
        final UserDetails details = mailSendingHelper.userDetails(requester);

        model.put(USER_ID, requester);
        model.put(USER_EMAIL, details.email);
        model.put(DATE, dateTimeFormat.format(date));

        send(mailPropertiesProvider.getSupportEmail(), getTemplateLocation(template), model);
    }
}
