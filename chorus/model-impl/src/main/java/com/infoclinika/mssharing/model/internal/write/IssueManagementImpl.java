/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.internal.write;

import com.google.common.base.Optional;
import com.infoclinika.mssharing.model.Notifier;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.repository.UserRepository;
import com.infoclinika.mssharing.model.write.IssueManagement;
import com.infoclinika.mssharing.model.write.LogUploader;
import com.infoclinika.mssharing.utils.logging.LogBuffer;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Oleksii Tymchenko
 */
@Service
public class IssueManagementImpl implements IssueManagement {
    public static final String TITLE_PARAM = "title";
    public static final String CONTENT_PARAM = "content";
    public static final String COMPONENT_PARAM = "component";
    private static final Logger LOG = Logger.getLogger(IssueManagementImpl.class);
    @Inject
    private UserRepository userRepository;
    @Inject
    private Notifier notifier;
    @Inject
    private LogUploader logUploader;
    @Value("${issues.endpoint}")
    private String endpoint;
    @Value("${issues.component.name}")
    private String componentName;
    @Value("${issues.bitbucket.username}")
    private String bitBucketUsername;
    @Value("${issues.bitbucket.password}")
    private String bitBucketPassword;
    @Value("${issue.support.email}")
    private String supportEmail;

    @Override
    @Async
    public void postIssue(long actor, final String issueTitle, final String issueContents) {

        try {
            LOG.debug("Posting an issue: title = " + issueTitle + "; contents = " + issueContents + ". User ID = " + actor);

            final DefaultHttpClient httpClient = new DefaultHttpClient();
            //todo[tymchenko]: load the credentials from the file
            httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(bitBucketUsername, bitBucketPassword));

            final HttpPost httpPost = new HttpPost(endpoint);
            final List<NameValuePair> params = new ArrayList<NameValuePair>();

            final User user = userRepository.findOne(actor);
            if (user == null) {
                LOG.error("Cannot post an issue. User not found for ID: " + user);
                return;
            }

            final String actorName = user.getFullName() + " (" + user.getEmail() + ")";
            String downloadUrl = uploadLogFile();
            String contentsWithUser = "Reported by: " + actorName + "\n\n" + issueContents + "\n\n" + downloadUrl;
            params.add(new BasicNameValuePair(TITLE_PARAM, issueTitle));
            params.add(new BasicNameValuePair(CONTENT_PARAM, contentsWithUser));
            params.add(new BasicNameValuePair(COMPONENT_PARAM, componentName));

            httpPost.setEntity(new UrlEncodedFormEntity(params));
            httpClient.execute(httpPost);

            LOG.debug("Sending via email an issue:  User ID = " + actor + " ; title = " + issueTitle + "; contents = " + issueContents);

            notifier.sendIssueToEmail(actor, issueTitle, issueContents, supportEmail);

        } catch (IOException e) {
            LOG.error("Cannot post the issue for actor with ID = " + actor + ". Message = " + issueContents, e);

        } catch (IllegalArgumentException e) {
            LOG.error("Cannot send via email the issue for actor with ID = " + actor + ". Title = " + issueTitle + ",  message = " + issueContents, e);
        }
    }

    private String uploadLogFile() throws IOException {
        LogBuffer appender = (LogBuffer) Logger.getRootLogger().getAppender("buffer");
        final Optional<File> logs = appender.getLasLogFile();
        String downloadUrl = "";
        if (logs.isPresent()) {
            downloadUrl = logUploader.uploadFile(logs.get()).toString();
        }
        return downloadUrl;
    }

}
