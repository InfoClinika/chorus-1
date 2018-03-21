package com.infoclinika.mssharing.integration.test.test.laboratorycreation;

import com.infoclinika.mssharing.integration.test.data.LaboratoryData;
import com.infoclinika.mssharing.integration.test.data.UserData;
import com.infoclinika.mssharing.integration.test.testdata.AbstractDataProvider;
import org.testng.annotations.DataProvider;

import static com.infoclinika.mssharing.integration.test.utils.Strings.randomInt;
import static com.infoclinika.mssharing.integration.test.utils.Strings.randomizeName;

/**
 * @author Alexander Orlov
 */
public class LaboratoryCreationRequestDataProvider extends AbstractDataProvider {

    @DataProvider
    public static Object[][] labCreationRequestApprovingWithAlreadyRegisteredLabHead(){
        UserData user = new UserData.Builder()
                .email("chorus.tester+" + randomInt() + "@gmail.com")
                .password("pwd")
                .firstName("Chorus")
                .lastName(randomizeName("Tester"))
                .build();
        LaboratoryData laboratoryData = new LaboratoryData.Builder()
                .institutionUrl("http://test.lab")
                .laboratoryName(randomizeName("Test Lab"))
                .contactEmail(user.getEmail())
                .labHeadFirstName(user.getFirstName())
                .labHeadLastName(user.getLastName())
                .labHeadEmail(user.getEmail()).build();
        String successAlert = "Your request has been sent";
        return new Object[][]{{laboratoryData, environmentSpecificData.admin, user, successAlert}};
    }

    @DataProvider
    public static Object[][] labCreationRequestApprovingWithNotRegisteredLabHead(){
        UserData user = new UserData.Builder()
                .email("chorus.tester+" + randomInt() + "@gmail.com")
                .password("pwd")
                .firstName("Chorus")
                .lastName(randomizeName("Tester"))
                .build();
        LaboratoryData laboratoryData = new LaboratoryData.Builder()
                .institutionUrl("http://test.lab")
                .laboratoryName(randomizeName("Test Lab"))
                .contactEmail(user.getEmail())
                .labHeadFirstName(user.getFirstName())
                .labHeadLastName(user.getLastName())
                .labHeadEmail(user.getEmail()).build();
        String successAlert = "Your request has been sent";
        return new Object[][]{{laboratoryData, environmentSpecificData.admin, user, successAlert}};
    }

    @DataProvider
    public static Object[][] laboratoryCreationRequestRefusing(){
        UserData user = new UserData.Builder()
                .email("chorus.tester+" + randomInt() + "@gmail.com")
                .password("pwd")
                .firstName("Chorus")
                .lastName(randomizeName("Tester"))
                .build();
        LaboratoryData laboratoryData = new LaboratoryData.Builder()
                .institutionUrl("http://test.lab")
                .laboratoryName(randomizeName("Test Lab"))
                .contactEmail(user.getEmail())
                .labHeadFirstName(user.getFirstName())
                .labHeadLastName(user.getLastName())
                .labHeadEmail(user.getEmail()).build();
        String successAlert = "Your request has been sent";
        return new Object[][]{{laboratoryData, environmentSpecificData.admin, user, successAlert}};
    }

    @DataProvider
    public static Object[][] shouldNotAllowToCreateLabWithoutInstitutionURL(){
        LaboratoryData laboratoryData = new LaboratoryData.Builder()
                .institutionUrl("http://test.lab")
                .laboratoryName(randomizeName("Test Lab"))
                .contactEmail("chorus.tester+" + randomInt() + "@gmail.com")
                .labHeadFirstName("Chorus")
                .labHeadLastName(randomizeName("Tester"))
                .labHeadEmail("chorus.tester+" + randomInt() + "@gmail.com").build();
        String alert = "Laboratory URL is required";
        return new Object[][]{{laboratoryData, alert}};
    }

    @DataProvider
    public static Object[][] shouldNotAllowToCreateLabWithoutLabName(){
        LaboratoryData laboratoryData = new LaboratoryData.Builder()
                .institutionUrl("http://test.lab")
                .laboratoryName(randomizeName("Test Lab"))
                .contactEmail("chorus.tester+" + randomInt() + "@gmail.com")
                .labHeadFirstName("Chorus")
                .labHeadLastName(randomizeName("Tester"))
                .labHeadEmail("chorus.tester+" + randomInt() + "@gmail.com").build();
        String alert = "Laboratory Name is required";
        return new Object[][]{{laboratoryData, alert}};
    }

    @DataProvider
    public static Object[][] shouldNotAllowToCreateLabWithoutContactEmail(){
        LaboratoryData laboratoryData = new LaboratoryData.Builder()
                .institutionUrl("http://test.lab")
                .laboratoryName(randomizeName("Test Lab"))
                .contactEmail("chorus.tester+" + randomInt() + "@gmail.com")
                .labHeadFirstName("Chorus")
                .labHeadLastName(randomizeName("Tester"))
                .labHeadEmail("chorus.tester+" + randomInt() + "@gmail.com").build();
        String alert = "Your Email is invalid";
        return new Object[][]{{laboratoryData, alert}};
    }

    @DataProvider
    public static Object[][] shouldNotAllowToCreateLabWithoutRequiredFieldsFilledIn(){
        LaboratoryData laboratoryData = new LaboratoryData.Builder()
                .institutionUrl("http://test.lab")
                .laboratoryName(randomizeName("Test Lab"))
                .contactEmail("chorus.tester+" + randomInt() + "@gmail.com")
                .labHeadFirstName("Chorus")
                .labHeadLastName(randomizeName("Tester"))
                .labHeadEmail("chorus.tester+" + randomInt() + "@gmail.com").build();
        String alert = "First Name is required";
        return new Object[][]{{laboratoryData, alert}};
    }

    @DataProvider
    public static Object[][] shouldNotAllowToCreateLabWithoutLabHeadFirstName(){
        LaboratoryData laboratoryData = new LaboratoryData.Builder()
                .institutionUrl("http://test.lab")
                .laboratoryName(randomizeName("Test Lab"))
                .contactEmail("chorus.tester+" + randomInt() + "@gmail.com")
                .labHeadFirstName("Chorus")
                .labHeadLastName(randomizeName("Tester"))
                .labHeadEmail("chorus.tester+" + randomInt() + "@gmail.com").build();
        String alert = "First Name is required";
        return new Object[][]{{laboratoryData, alert}};
    }

    @DataProvider
    public static Object[][] shouldNotAllowToCreateLabWithoutLabHeadLastName(){
        LaboratoryData laboratoryData = new LaboratoryData.Builder()
                .institutionUrl("http://test.lab")
                .laboratoryName(randomizeName("Test Lab"))
                .contactEmail("chorus.tester+" + randomInt() + "@gmail.com")
                .labHeadFirstName("Chorus")
                .labHeadLastName(randomizeName("Tester"))
                .labHeadEmail("chorus.tester+" + randomInt() + "@gmail.com").build();
        String alert = "Last Name is required";
        return new Object[][]{{laboratoryData, alert}};
    }

    @DataProvider
    public static Object[][] shouldNotAllowToCreateLabWithoutLabHeadEmail(){
        LaboratoryData laboratoryData = new LaboratoryData.Builder()
                .institutionUrl("http://test.lab")
                .laboratoryName(randomizeName("Test Lab"))
                .contactEmail("chorus.tester+" + randomInt() + "@gmail.com")
                .labHeadFirstName("Chorus")
                .labHeadLastName(randomizeName("Tester"))
                .labHeadEmail("chorus.tester+" + randomInt() + "@gmail.com").build();
        String alert = "Email is invalid";
        return new Object[][]{{laboratoryData, alert}};
    }


}
