package com.infoclinika.mssharing.integration.test.test.myprofile;

import com.infoclinika.mssharing.integration.test.data.UserData;
import com.infoclinika.mssharing.integration.test.testdata.AbstractDataProvider;
import org.testng.annotations.DataProvider;

import static com.infoclinika.mssharing.integration.test.utils.Strings.randomInt;
import static com.infoclinika.mssharing.integration.test.utils.Strings.randomizeName;

/**
 * @author Alexander Orlov
 */
public class MyProfileDataProvider extends AbstractDataProvider {

    @DataProvider
    public static Object[][] changeUserName() {
        UserData userData = new UserData.Builder()
                .email("chorus.tester+" + randomInt() + "@gmail.com")
                .password("Password123456")
                .firstName("Chorus")
                .lastName(randomizeName("Tester"))
                .build();
        UserData changedUserData = new UserData.Builder()
                .email(userData.getEmail())
                .password(userData.getPassword())
                .firstName(randomizeName("First"))
                .lastName(randomizeName("Last"))
                .build();
        String alert = "Name was changed successfully";
        return new Object[][]{{userData, changedUserData, alert}};
    }

    @DataProvider
    public static Object[][] shouldNotAllowToSaveProfileWithoutFirstName() {
        String alert = "First Name is required";
        return new Object[][]{{alert}};
    }

    @DataProvider
    public static Object[][] shouldNotAllowToSaveProfileWithoutLastName() {
        String alert = "Last Name is required";
        return new Object[][]{{alert}};
    }

    @DataProvider
    public static Object[][] changePassword() {
        UserData userData = new UserData.Builder()
                .email("chorus.tester+" + randomInt() + "@gmail.com")
                .password("Password123456")
                .firstName("Chorus")
                .lastName(randomizeName("Tester"))
                .build();
        UserData changedUserData = new UserData.Builder()
                .email(userData.getEmail())
                .password("pwd")
                .firstName(userData.getFirstName())
                .lastName(userData.getLastName())
                .build();
        String alert = "Password was changed successfully";
        return new Object[][]{{userData, changedUserData, alert}};
    }

}
