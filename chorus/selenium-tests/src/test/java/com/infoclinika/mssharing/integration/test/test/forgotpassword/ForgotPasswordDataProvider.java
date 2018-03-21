package com.infoclinika.mssharing.integration.test.test.forgotpassword;

import com.infoclinika.mssharing.integration.test.data.UserData;
import com.infoclinika.mssharing.integration.test.testdata.AbstractDataProvider;
import org.testng.annotations.DataProvider;

import static com.infoclinika.mssharing.integration.test.utils.Strings.randomInt;
import static com.infoclinika.mssharing.integration.test.utils.Strings.randomizeName;

/**
 * @author Alexander Orlov
 */
public class ForgotPasswordDataProvider extends AbstractDataProvider {

    @DataProvider
    public static Object[][] forgotPassword() {
        UserData userData = new UserData.Builder()
                .email("chorus.tester+" + randomInt() + "@gmail.com")
                .password("Password123456")
                .firstName("Chorus")
                .lastName("Tester")
                .build();
        UserData userWithChangedPassword = new UserData.Builder()
                .email(userData.getEmail())
                .password(randomInt())
                .firstName(userData.getFirstName())
                .lastName(userData.getLastName())
                .build();
        return new Object[][]{{userData, userWithChangedPassword}};
    }

    @DataProvider
    public static Object[][] forgotPasswordForIncorrectCredentials() {
        String email = randomizeName("user").concat("@").concat("smth.eml");
        return new Object[][]{{email}};
    }
}
