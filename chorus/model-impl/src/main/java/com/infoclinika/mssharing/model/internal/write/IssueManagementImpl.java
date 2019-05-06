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
package com.infoclinika.mssharing.model.internal.write;

import com.google.common.base.Optional;
import com.infoclinika.mssharing.model.Notifier;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.repository.UserRepository;
import com.infoclinika.mssharing.model.write.IssueManagement;
import com.infoclinika.mssharing.model.write.LogUploader;
import com.infoclinika.mssharing.propertiesprovider.BitbucketPropertiesProvider;
import com.infoclinika.mssharing.utils.logging.LogBuffer;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(IssueManagementImpl.class);
    private static final String TITLE_PARAM = "title";
    private static final String CONTENT_PARAM = "content";
    private static final String COMPONENT_PARAM = "component";

    @Inject
    private UserRepository userRepository;

    @Inject
    private Notifier notifier;

    @Inject
    private LogUploader logUploader;

    @Inject
    private BitbucketPropertiesProvider bitbucketPropertiesProvider;

    @Override
    @Async
    public void postIssue(long actor, final String issueTitle, final String issueContents) {

        try {
            LOGGER.debug("Posting an issue: title = {}; contents = {}. User ID = {}", issueTitle, issueContents, actor);

            final DefaultHttpClient httpClient = new DefaultHttpClient();
            //todo[tymchenko]: load the credentials from the file
            httpClient.getCredentialsProvider()
                .setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(
                        bitbucketPropertiesProvider.getIssuesBitbucketUsername(),
                        bitbucketPropertiesProvider.getIssuesBitbucketPassword()
                    )
                );

            final HttpPost httpPost = new HttpPost(bitbucketPropertiesProvider.getIssuesEndpoint());
            final List<NameValuePair> params = new ArrayList<NameValuePair>();

            final User user = userRepository.findOne(actor);
            if (user == null) {
                LOGGER.error("Cannot post an issue. User not found for ID: {}", user);
                return;
            }

            final String actorName = user.getFullName() + " (" + user.getEmail() + ")";
            String downloadUrl = uploadLogFile();
            String contentsWithUser = "Reported by: " + actorName + "\n\n" + issueContents + "\n\n" + downloadUrl;
            params.add(new BasicNameValuePair(TITLE_PARAM, issueTitle));
            params.add(new BasicNameValuePair(CONTENT_PARAM, contentsWithUser));
            params.add(new BasicNameValuePair(COMPONENT_PARAM, bitbucketPropertiesProvider.getIssuesComponentName()));

            httpPost.setEntity(new UrlEncodedFormEntity(params));
            httpClient.execute(httpPost);

            LOGGER.debug(
                "Sending via email an issue:  User ID = {}; title = {}; contents = {}",
                actor,
                issueTitle,
                issueContents
            );

            notifier.sendIssueToEmail(
                actor,
                issueTitle,
                issueContents,
                bitbucketPropertiesProvider.getIssueSupportEmail()
            );

        } catch (IOException e) {
            LOGGER.error("Cannot post the issue for actor with ID = {}. Message = {}", actor, issueContents, e);

        } catch (IllegalArgumentException e) {
            LOGGER.error(
                "Cannot send via email the issue for actor with ID = {}. Title = {}, message = {}",
                actor,
                issueTitle,
                issueContents,
                e
            );
        }
    }

    private String uploadLogFile() throws IOException {
        LogBuffer appender = (LogBuffer) org.apache.log4j.Logger.getRootLogger().getAppender("buffer");
        final Optional<File> logs = appender.getLasLogFile();
        String downloadUrl = "";
        if (logs.isPresent()) {
            downloadUrl = logUploader.uploadFile(logs.get()).toString();
        }
        return downloadUrl;
    }

}
