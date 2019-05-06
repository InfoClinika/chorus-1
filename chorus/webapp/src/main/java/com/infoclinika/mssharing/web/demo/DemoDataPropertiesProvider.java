package com.infoclinika.mssharing.web.demo;

import com.infoclinika.mssharing.propertiesprovider.AmazonPropertiesProvider;
import com.infoclinika.mssharing.propertiesprovider.ChorusPropertiesProvider;
import com.infoclinika.mssharing.propertiesprovider.MailPropertiesProvider;
import com.infoclinika.mssharing.propertiesprovider.RabbitPropertiesProvider;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Vladislav Kovchug
 */

@Service
public class DemoDataPropertiesProvider {
    private final AmazonPropertiesProvider amazonPropertiesProvider;
    private final ChorusPropertiesProvider chorusPropertiesProvider;
    private final RabbitPropertiesProvider rabbitPropertiesProvider;
    private final MailPropertiesProvider mailPropertiesProvider;

    @Inject
    public DemoDataPropertiesProvider(AmazonPropertiesProvider amazonPropertiesProvider,
                                      ChorusPropertiesProvider chorusPropertiesProvider,
                                      RabbitPropertiesProvider rabbitPropertiesProvider,
                                      MailPropertiesProvider mailPropertiesProvider) {
        this.amazonPropertiesProvider = amazonPropertiesProvider;
        this.chorusPropertiesProvider = chorusPropertiesProvider;
        this.rabbitPropertiesProvider = rabbitPropertiesProvider;
        this.mailPropertiesProvider = mailPropertiesProvider;
    }

    public boolean isCreateDemoData() {
        return chorusPropertiesProvider.isDatabaseCreateDemoData();
    }

    public String getAdminEmail() {
        return chorusPropertiesProvider.getDatabaseAdminEmail();
    }

    public String getAdminPassword() {
        return chorusPropertiesProvider.getDatabaseAdminPassword();
    }

    public String getTemplatesBucket() {
        return amazonPropertiesProvider.getTemplatesBucket();
    }

    public String getMailingImageTemplatesLocation() {
        return mailPropertiesProvider.getMailingImageTemplatesLocation();
    }

    public int getRabbitTimeout() {
        return rabbitPropertiesProvider.getRabbitTimeout();
    }

    public String getByonicRabbitUsername() {
        return rabbitPropertiesProvider.getByonicRabbitUsername();
    }

    public String getByonicRabbitPwd() {
        return rabbitPropertiesProvider.getByonicRabbitPassword();
    }
}
