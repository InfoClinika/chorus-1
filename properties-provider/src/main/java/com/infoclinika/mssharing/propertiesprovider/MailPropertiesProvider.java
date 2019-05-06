package com.infoclinika.mssharing.propertiesprovider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MailPropertiesProvider extends AbstractPropertiesProvider {

    @Value("${mail.from.email}")
    private String emailFrom;

    @Value("${mail.smtp.auth}")
    private boolean smtpAuth;

    @Value("${mail.smtp.debug}")
    private boolean smtpDebug;

    @Value("${mail.smtp.host}")
    private String smtpHost;

    @Value("${mail.smtp.password}")
    private String smtpPassword;

    @Value("${mail.smtp.port:0}")
    private int smtpPort;

    @Value("${mail.smtp.socketFactory.class}")
    private String smtpSocketFactoryClass;

    @Value("${mail.smtp.socketFactory.fallback:false}")
    private boolean smtpSocketFactoryFallback;

    @Value("${mail.smtp.socketFactory.port:0}")
    private int smtpSocketFactoryPort;

    @Value("${mail.smtp.starttls.enable:true}")
    private boolean smtpStartTls;

    @Value("${mail.smtp.username}")
    private String smtpUsername;

    @Value("${mail.support.email}")
    private String supportEmail;

    @Value("${mailing.image.templates.location}")
    private String mailingImageTemplatesLocation;

    @Value("${mailing.images.prefix}")
    private String imagesPrefix;

    @Value("${mailing.templates.location}")
    private String templatesLocation;

    @Value("#{'${analysisRuns.error.emails}'.split(',')}")
    private List<String> analysisRunsErrorEmails;

    @Value("#{'${translation.error.emails}'.split(',')}")
    private List<String> translationErrorEmails;

    public String getEmailFrom() {
        return emailFrom;
    }

    public boolean isSmtpAuth() {
        return smtpAuth;
    }

    public boolean isSmtpDebug() {
        return smtpDebug;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public String getSmtpPassword() {
        return smtpPassword;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public String getSmtpSocketFactoryClass() {
        return smtpSocketFactoryClass;
    }

    public boolean isSmtpSocketFactoryFallback() {
        return smtpSocketFactoryFallback;
    }

    public int getSmtpSocketFactoryPort() {
        return smtpSocketFactoryPort;
    }

    public boolean isSmtpStartTls() {
        return smtpStartTls;
    }

    public String getSmtpUsername() {
        return smtpUsername;
    }

    public String getSupportEmail() {
        return supportEmail;
    }

    public String getMailingImageTemplatesLocation() {
        return mailingImageTemplatesLocation;
    }

    public String getImagesPrefix() {
        return imagesPrefix;
    }

    public String getTemplatesLocation() {
        return templatesLocation;
    }

    public List<String> getAnalysisRunsErrorEmails() {
        return analysisRunsErrorEmails;
    }

    public List<String> getTranslationErrorEmails() {
        return translationErrorEmails;
    }
}
