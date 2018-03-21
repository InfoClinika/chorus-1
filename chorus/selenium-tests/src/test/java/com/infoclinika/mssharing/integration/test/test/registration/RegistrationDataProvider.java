package com.infoclinika.mssharing.integration.test.test.registration;

import com.infoclinika.mssharing.integration.test.data.SampleData;
import com.infoclinika.mssharing.integration.test.data.UserData;
import com.infoclinika.mssharing.integration.test.testdata.AbstractDataProvider;
import org.testng.annotations.DataProvider;

import static com.infoclinika.mssharing.integration.test.utils.Strings.randomInt;
import static com.infoclinika.mssharing.integration.test.utils.Strings.randomizeName;

/**
 * @author Alexander Orlov
 */
public class RegistrationDataProvider extends AbstractDataProvider {

    @DataProvider
    public static Object[][] registerNewUser() {
        UserData userData = new UserData.Builder()
                .email("chorus.tester+" + randomInt() + "@gmail.com")
                .password("pwd")
                .firstName("Chorus")
                .lastName(randomizeName("Tester"))
                .build();
        String successAlertText = "Your email was verified. Please log in.";
        return new Object[][]{{userData, successAlertText}};
    }

    @DataProvider
    public static Object[][] registerNewUserWithLab() {
        UserData userData = new UserData.Builder()
                .email("chorus.tester+" + randomInt() + "@gmail.com")
                .password("pwd")
                .firstName("Chorus")
                .lastName(randomizeName("Tester"))
                .build();
        String laboratory = SampleData.LAB_FIRST_CHORUS;
        String successAlertText = "Your email was verified. Please log in.";
        return new Object[][]{{userData, laboratory, successAlertText, environmentSpecificData.pavelKaplinAtGmail}};
    }

    @DataProvider
    public static Object[][] shouldNotAllowToCreateAccountWithoutFirstName() {
        UserData userData = new UserData.Builder()
                .email("chorus.tester+" + randomInt() + "@gmail.com")
                .password("pwd")
                .firstName("")
                .lastName(randomizeName("Tester"))
                .build();
        String alertText = "First Name is required";
        return new Object[][]{{userData, alertText}};
    }

    @DataProvider
    public static Object[][] shouldNotAllowToCreateAccountWithoutLastName() {
        UserData userData = new UserData.Builder()
                .email("chorus.tester+" + randomInt() + "@gmail.com")
                .password("pwd")
                .firstName("Chorus")
                .lastName("")
                .build();
        String alertText = "Last Name is required";
        return new Object[][]{{userData, alertText}};
    }

    @DataProvider
    public static Object[][] shouldNotAllowToCreateAccountWithoutEmail() {
        UserData userData = new UserData.Builder()
                .email("")
                .password("pwd")
                .firstName("Chorus")
                .lastName(randomizeName("Tester"))
                .build();
        String alertText = "Email is required";
        return new Object[][]{{userData, alertText}};
    }

    @DataProvider
    public static Object[][] shouldNotAllowToCreateAccountWithoutEmailConfirmation() {
        UserData userData = new UserData.Builder()
                .email("chorus.tester+" + randomInt() + "@gmail.com")
                .password("pwd")
                .firstName("Chorus")
                .lastName("Tester")
                .build();
        String alertText = "Confirm Email is required";
        return new Object[][]{{userData, alertText}};
    }

    @DataProvider
    public static Object[][] shouldNotAllowToCreateAccountWithoutPassword() {
        UserData userData = new UserData.Builder()
                .email("chorus.tester+" + randomInt() + "@gmail.com")
                .password("pwd")
                .firstName("Chorus")
                .lastName("Tester")
                .build();
        String alertText = "Password is required";
        return new Object[][]{{userData, alertText}};
    }

    @DataProvider
    public static Object[][] shouldNotAllowToCreateAccountWithoutPasswordConfirmation() {
        UserData userData = new UserData.Builder()
                .email("chorus.tester+" + randomInt() + "@gmail.com")
                .password("pwd")
                .firstName("Chorus")
                .lastName("Tester")
                .build();
        String alertText = "Confirm Password is required";
        return new Object[][]{{userData, alertText}};
    }

    @DataProvider
    public static Object[][] shouldNotAllowToCreateAccountWithDifferentEmailAndEmailConfirmation() {
        String email = "chorus.tester+" + randomInt() + "@gmail.com";
        String confirmEmail = "chorus.tester+" + randomInt() + "@gmail.com";
        UserData userData = new UserData.Builder()
                .email(email)
                .emailConfirmation(confirmEmail)
                .password("pwd")
                .firstName("Chorus")
                .lastName(randomizeName("Tester"))
                .build();
        String alertText = "Confirm Email and email addresses should match";
        return new Object[][]{{userData, alertText}};
    }

    @DataProvider
    public static Object[][] shouldNotAllowToCreateAccountWithDifferentPasswordAndPasswordConfirmation() {
        String email = "chorus.tester+" + randomInt() + "@gmail.com";
        UserData userData = new UserData.Builder()
                .email(email)
                .password("pwd")
                .passwordConfirmation("another-pwd")
                .firstName("Chorus")
                .lastName(randomizeName("Tester"))
                .build();
        String alertText = "Confirm Password and password don't match";
        return new Object[][]{{userData, alertText}};
    }

    @DataProvider
    public static Object[][] shouldNotAllowToCreateAccountWithAlreadyRegisteredEmail() {
        String alertText = "Email is already registered";
        return new Object[][]{{environmentSpecificData.pavelKaplinAtGmail, alertText}};
    }
}
