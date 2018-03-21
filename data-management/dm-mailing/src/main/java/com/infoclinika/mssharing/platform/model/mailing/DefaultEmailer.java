/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.platform.model.mailing;

import com.google.common.base.Throwables;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * @author Stanislav Kurilin, Herman Zamula
 */
@Component
class DefaultEmailer implements EmailerTemplate {
    private static final Logger LOG = Logger.getLogger(DefaultEmailer.class);
    private JavaMailSender javaMailSender;
    @Value("${mail.from.email}")
    private String from;

    @Value("${mail.smtp.host}")
    private String host;

    @Value("${mail.smtp.port}")
    private Integer port;

    @Value("${mail.smtp.username}")
    private String username;

    @Value("${mail.smtp.password}")
    private String password;

    @Value("${mail.smtp.starttls.enable}")
    private Boolean startTls;

    @Value("${mail.smtp.debug}")
    private Boolean debug;

    @Value("${mail.smtp.auth}")
    private Boolean auth;

    @Value("${mail.smtp.socketFactory.port}")
    private Integer socketFactoryPort;

    @Value("${mail.smtp.socketFactory.class}")
    private String socketFactoryClass;

    @Value("${mail.smtp.socketFactory.fallback}")
    private Boolean socketFactoryFallback;


    @Override
    public void send(String to, String subject, String message) {
        LOG.debug("Sending email to: " + to + " with subject: " + subject);
        try {

            final MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
            helper.setTo(new InternetAddress(to));
            helper.setSubject(subject);
            helper.setText(message, true);
            helper.setFrom(new InternetAddress(from));
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw Throwables.propagate(e);
        }
    }

    @PostConstruct
    public void initMailSender() {
        final JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setDefaultEncoding("UTF-8");
        javaMailSender.setHost(host);
        javaMailSender.setPort(port);
        if (username != null && !username.isEmpty()) {
            javaMailSender.setUsername(username);
        }
        if (password != null && !password.isEmpty()) {
            javaMailSender.setPassword(password);
        }
        final Properties props = new Properties();

        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.starttls.enable", startTls);
        props.put("mail.smtp.debug", debug);
        props.put("mail.smtp.auth", auth);
        if (socketFactoryClass != null && !socketFactoryClass.isEmpty()) {
            props.put("mail.smtp.socketFactory.port", socketFactoryPort);
            props.put("mail.smtp.socketFactory.class", socketFactoryClass);
            props.put("mail.smtp.socketFactory.fallback", socketFactoryFallback);
        }

        javaMailSender.setJavaMailProperties(props);
        this.javaMailSender = javaMailSender;
    }
}
