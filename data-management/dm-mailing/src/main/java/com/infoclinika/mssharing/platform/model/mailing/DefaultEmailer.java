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
package com.infoclinika.mssharing.platform.model.mailing;

import com.google.common.base.Throwables;
import com.infoclinika.mssharing.propertiesprovider.MailPropertiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Properties;

/**
 * @author Stanislav Kurilin, Herman Zamula
 */
@Component
class DefaultEmailer implements EmailerTemplate {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEmailer.class);

    private JavaMailSender javaMailSender;

    @Inject
    private MailPropertiesProvider mailPropertiesProvider;

    @Override
    public void send(String to, String subject, String message) {
        LOGGER.debug("Sending email to: " + to + " with subject: " + subject);
        try {
            MimeMessageHelper helper = prepareMimeMessage(subject, message);
            helper.setTo(new InternetAddress(to));
            javaMailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void send(List<String> to, String subject, String message) {
        LOGGER.debug("Sending email to: {} with subject: ", to, subject);
        try {
            MimeMessageHelper helper = prepareMimeMessage(subject, message);
            for (String email : to) {
                helper.addCc(new InternetAddress(email));
            }
            javaMailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            throw Throwables.propagate(e);
        }
    }

    private MimeMessageHelper prepareMimeMessage(String subject, String message) throws MessagingException {
        final MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
        helper.setSubject(subject);
        helper.setText(message, true);
        helper.setFrom(new InternetAddress(mailPropertiesProvider.getEmailFrom()));
        return helper;
    }

    @PostConstruct
    public void initMailSender() {
        final JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setDefaultEncoding("UTF-8");
        javaMailSender.setHost(mailPropertiesProvider.getSmtpHost());
        javaMailSender.setPort(mailPropertiesProvider.getSmtpPort());

        final String username = mailPropertiesProvider.getSmtpUsername();
        final String password = mailPropertiesProvider.getSmtpPassword();

        if (username != null && !username.isEmpty()) {
            javaMailSender.setUsername(username);
        }
        if (password != null && !password.isEmpty()) {
            javaMailSender.setPassword(password);
        }
        final Properties props = new Properties();

        props.put("mail.smtp.host", mailPropertiesProvider.getSmtpHost());
        props.put("mail.smtp.port", mailPropertiesProvider.getSmtpPort());
        props.put("mail.smtp.starttls.enable", mailPropertiesProvider.isSmtpStartTls());
        props.put("mail.smtp.debug", mailPropertiesProvider.isSmtpDebug());
        props.put("mail.smtp.auth", mailPropertiesProvider.isSmtpAuth());

        final String socketFactoryClass = mailPropertiesProvider.getSmtpSocketFactoryClass();

        if (socketFactoryClass != null && !socketFactoryClass.isEmpty()) {
            props.put("mail.smtp.socketFactory.port", mailPropertiesProvider.getSmtpSocketFactoryPort());
            props.put("mail.smtp.socketFactory.class", socketFactoryClass);
            props.put("mail.smtp.socketFactory.fallback", mailPropertiesProvider.isSmtpSocketFactoryFallback());
        }

        javaMailSender.setJavaMailProperties(props);
        this.javaMailSender = javaMailSender;
    }
}
