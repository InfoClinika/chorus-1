package com.infoclinika.mssharing.integration.test.utils;

import com.infoclinika.mssharing.integration.test.data.EmailFolder;
import com.infoclinika.mssharing.integration.test.stepdefinitions.LoginPageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.RegistrationPageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.ResetPasswordPageSteps;

/**
 * @author Alexander Orlov
 *
 */
public class NavigationManager {

    public static LoginPageSteps navigateByActivationLink() {
        String activationLink = EmailService.getLinkFromEmail(EmailService.connectAndGetAllEmailsFromFolder(EmailFolder.VERIFY_EMAIL).get(0));
        ConfigurationManager.navigateTo(activationLink);
        return new LoginPageSteps();
    }

    public static ResetPasswordPageSteps navigateByLinkFromResetPasswordEmail() {
        String linkFromEmail = EmailService.getLinkFromEmail(EmailService.connectAndGetAllEmailsFromFolder(EmailFolder.RESET_PASSWORD).get(0));
        ConfigurationManager.navigateTo(linkFromEmail);
        return new ResetPasswordPageSteps();
    }

    public static RegistrationPageSteps navigateByInviteLink() {
        String inviteLink = EmailService.getLinkFromEmail(EmailService.connectAndGetAllEmailsFromFolder(EmailFolder.INVITATION).get(0));
        ConfigurationManager.navigateTo(inviteLink);
        return new RegistrationPageSteps();
    }
}
