/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
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
import com.infoclinika.mssharing.platform.model.mailing.DefaultNotifier;
import com.infoclinika.mssharing.platform.model.mailing.EmailerTemplate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.String.join;
import static java.util.stream.Collectors.toSet;

/**
 * @author Stanislav Kurilin
 */
@Component("notifier")
class NotifierImpl extends DefaultNotifier implements Notifier, BlogNotifier, AdminNotifier {
    private static final Logger LOG = Logger.getLogger(NotifierImpl.class);
    private static final String POST = "post";
    private static final String POST_URL = "postUrl";
    private static final String BLOG_URL = "blogUrl";
    private static final String COMMENT = "comment";
    private static final String BLOG_SUBSCRIBER = "blogSubscriber";
    private static final String FILE_NAME = "fileName";
    private static final String FORMATTED_DATE = "formattedDate";
    private static final String ERROR_MESSAGE = "errorMessage";
    private static final String EXPERIMENT_NAME = "experimentName";
    private static final String AUTHOR_EMAIL = "authorEmail";
    private static final String USER = "user";
    private static final String DOWNLOAD_URL = "downloadUrl";
    private static final String ANALYSIS_URL = "analysisUrl";
    private static final String FAILED_WORKFLOW_STEP = "failedWorkflowStep";
    private static final String PROJECT = "project";
    private static final String SENDER = "sender";
    private static final String TITLE = "title";
    private static final String BODY = "body";
    private static final String FILE_DOWNLOAD_LINK = "fileDownloadLink";
    private static final String FAILED_EMAILS = "failedEmails";
    private static final String FAILED_RECORDS_IDS = "failedRecordsIds";
    private static final String EXPERIMENT_ID = "experimentId";
    private static final String PACKAGE_NUMBER = "packageNumber";
    private static final String DELIMITER = ", ";

    @Inject
    private MailSendingHelper mailSendingHelper;
    private EmailerTemplate mockEmailer = new MockEmailer();
    @Inject
    private EmailerTemplate realEmailer;
    @Value("${base.url}")
    private String baseUrl;
    @Value("#{'${translation.error.emails}'.split(',')}")
    private List<String> translationErrorEmails;

    @Value("#{'${analysisRuns.error.emails}'.split(',')}")
    private List<String> analysisRunEmails;
    @Inject
    private InboxNotifierTemplate inboxNotifier;
    @Value("${amazon.sns.failed.emails.arn}")
    private String snsFailedEmailsArn;

    @PostConstruct
    public void enableProperEmailer() {
        setEnabled(true);
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.emailer = enabled ? realEmailer : mockEmailer;
    }

    protected void send(String to, String template, Map<String, Object> model) {
        if (mailSendingHelper.isSkipSending(to)) {
            LOG.info("Skip sending email to: \"" + to + "\". Marked as ignored");
            return;
        }
        super.send(to, template, model);
    }

    @Override
    public void postAdded(long subscriber, BlogPost post) {
        MailSendingHelper.UserDetails details = mailSendingHelper.userDetails(subscriber);
        Map<String, Object> model = new HashMap<>();
        String blogUrl = baseUrl + "/pages/blog.html#/" + post.getBlog().getId();
        String postUrl = blogUrl + '/' + post.getId();
        model.put(POST, post);
        model.put(POST_URL, postUrl);
        model.put(BLOG_URL, blogUrl);
        send(details.email, getTemplateLocation("blogPostAdded.vm"), model);

        inboxNotifier.notify(post.getAuthor().getId(), subscriber, "<a href=\"" + postUrl + "\">" + post.getTitle() + "</a> - blog post added");
    }

    @Override
    public void commentAdded(long subscriber, Comment comment) {
        MailSendingHelper.UserDetails details = mailSendingHelper.userDetails(subscriber);
        Map<String, Object> model = new HashMap<>();
        String blogUrl = baseUrl + "/pages/blog.html#/" + comment.getPost().getBlog().getId();
        String postUrl = blogUrl + '/' + comment.getPost().getId();
        model.put(COMMENT, comment);
        model.put(POST_URL, postUrl);
        model.put(BLOG_URL, blogUrl);
        send(details.email, getTemplateLocation("commentAdded.vm"), model);

        inboxNotifier.notify(comment.getPost().getAuthor().getId(), subscriber, "<a href=\"" + postUrl + "\">" + comment.getPost().getTitle() + "</a> - comment added");
    }

    @Override
    public void postEdited(long subscriber, BlogPost post) {
        MailSendingHelper.UserDetails details = mailSendingHelper.userDetails(subscriber);
        Map<String, Object> model = new HashMap<>();
        String blogUrl = baseUrl + "/pages/blog.html#/" + post.getBlog().getId();
        String postUrl = blogUrl + '/' + post.getId();
        model.put(POST, post);
        model.put(POST_URL, postUrl);
        model.put(BLOG_URL, blogUrl);
        send(details.email, getTemplateLocation("blogPostEdited.vm"), model);

        inboxNotifier.notify(post.getAuthor().getId(), subscriber, "<a href=\"" + postUrl + "\">" + post.getTitle() + "</a> - blog post edited");
    }


    @Override
    public void postDeleted(long subscriber, BlogPost post, boolean blogSubscriber) {
        MailSendingHelper.UserDetails details = mailSendingHelper.userDetails(subscriber);
        Map<String, Object> model = new HashMap<>();
        String blogUrl = baseUrl + "/pages/blog.html#/" + post.getBlog().getId();
        model.put(POST, post);
        model.put(BLOG_URL, blogUrl);
        model.put(BLOG_SUBSCRIBER, blogSubscriber);
        send(details.email, getTemplateLocation("blogPostDeleted.vm"), model);

        inboxNotifier.notify(post.getAuthor().getId(), subscriber, "\"" + post.getTitle() + "\" has been deleted from <a href=\"" + blogUrl + "\">" + post.getBlog().getName() + "</a>");
    }

    @Override
    public void sendFileReadyToDownloadNotification(long actor, Collection<Long> files) {
        Map<String, Object> model = new HashMap<>();
        final MailSendingHelper.UserDetails details = mailSendingHelper.userDetails(actor);
        StringBuilder downloadUrl = new StringBuilder(baseUrl + "/download/bulk?");
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

        final MailSendingHelper.UserDetails details = mailSendingHelper.userDetails(receiver);

        final Map<String, Object> model = Maps.newHashMap();
        model.put(USER, details.name);
        model.put(PROJECT, activeProjectName);
        model.put(SENDER, senderFullName);

        send(details.email, getTemplateLocation("copyProjectNotification.vm"), model);
    }

    @Override
    public void sendCommonEmail(long receiver, String title, String body) {

        final MailSendingHelper.UserDetails details = mailSendingHelper.userDetails(receiver);

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
        model.put(FAILED_RECORDS_IDS, join(DELIMITER, failedRecordsIds.stream().map(Object::toString).collect(toSet())));

        send(email, getTemplateLocation("failedEmailsAction.vm"), model);

    }

}
