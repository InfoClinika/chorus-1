package com.infoclinika.mssharing.integration.test.test.login;

import com.infoclinika.mssharing.integration.test.data.UserData;
import com.infoclinika.mssharing.integration.test.testdata.AbstractDataProvider;
import org.testng.annotations.DataProvider;

import static com.infoclinika.mssharing.integration.test.utils.Strings.randomInt;

/**
 * @author Alexander Orlov
 */
public class LoginDataProvider extends AbstractDataProvider {

    @DataProvider(name = "Login as Admin")
    public static Object[][] loginAsAdmin() {
        return new Object[][]{{environmentSpecificData.admin}};
    }

    @DataProvider(name = "Login as regular User")
    public static Object[][] loginAsRegularUser() {
        return new Object[][]{{environmentSpecificData.pavelKaplinAtGmail}};
    }

    @DataProvider(name = "Login as User, that has invalid credentials")
    public static Object[][] loginWithIncorrectCredentials() {
        return new Object[][]{{new UserData.Builder().email("pavel.123@teamdev.com").password("123").build()}};
    }

    @DataProvider(name = "Login as User, that has valid login, but invalid password")
    public static Object[][] loginWithPartiallyCorrectCredentials() {
        UserData pavelKaplinAtGmail = environmentSpecificData.pavelKaplinAtGmail;
        return new Object[][]{{new UserData.Builder().email(pavelKaplinAtGmail.getEmail()).password(randomInt()).build()}};
    }

    @DataProvider(name = "Login as not activated User")
    public static Object[][] loginWithNotActivatedUser() {
        UserData chorusTesterAtGmail = new UserData.Builder()
                .email("chorus.tester+" + randomInt() + "@gmail.com")
                .password("pwd")
                .firstName("Chorus")
                .lastName("Tester").build();
        return new Object[][]{{chorusTesterAtGmail}};
    }


}
