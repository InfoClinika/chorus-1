package com.infoclinika.sso.model.test;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.infoclinika.sso.model.exception.AccountIsAlreadyLinkedException;
import com.infoclinika.sso.model.read.UserDetailsReader;
import com.infoclinika.sso.model.read.UserDetailsReader.UserDetails;
import com.infoclinika.sso.model.read.UserDetailsReader.UserDetails.ApplicationCredential;
import com.infoclinika.sso.model.test.common.AbstractTest;
import com.infoclinika.sso.model.write.UserManagement;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static com.infoclinika.sso.model.ApplicationType.CHORUS;
import static com.infoclinika.sso.model.ApplicationType.PANORAMA;
import static org.testng.Assert.*;

/**
 * @author andrii.loboda
 */
@SuppressWarnings({"InstanceMethodNamingConvention", "DuplicateStringLiteralInspection"})
public class UserManagementShouldTest extends AbstractTest {

    @Inject
    private UserManagement userManagement;

    @Inject
    private UserDetailsReader userDetailsReader;

    @Test
    public void create_user_and_add_chorus_application() {
        final String username = "andrii.loboda@fakechorus.com";
        final String secretKey = "fakechorus_secret_key_for_andrey";

        final long userID = userManagement.addApplicationForUser(Optional.<Long>absent(), CHORUS, username, secretKey);


        final Optional<UserDetails> detailsOpt = userDetailsReader.getDetails(CHORUS, username);
        assertTrue(detailsOpt.isPresent(), "User with '" + username + "' name is not present");
        final UserDetails userDetails = detailsOpt.get();
        assertEquals(userDetails.ID, userID);
        assertFalse(userDetails.linked);
        assertEquals(userDetails.credentials.size(), 1);

        final ApplicationCredential chorusCredential = findChorusCredential(userDetails.credentials);
        assertEquals(chorusCredential.applicationType, CHORUS);
        assertEquals(chorusCredential.username, username);
        assertEquals(chorusCredential.secretToken, secretKey);
    }

    @Test
    public void link_user_with_panorama_application() {
        final String panoramaUsername = "andrii.loboda@fakepanorama.com";
        final String panoramaSecretKey = "fakepanorama_secret_key_for_andrii.loboda";

        final String chorusUsername = "andrii.loboda@fakechorus.com";
        final String chorusSecretKey = "fakechorus_secret_key_for_andrey";

        final long userID = userManagement.addApplicationForUser(
            Optional.absent(),
            CHORUS,
            chorusUsername,
            chorusSecretKey
        );
        final long userIDAfterLinking =
            userManagement.addApplicationForUser(Optional.of(userID), PANORAMA, panoramaUsername, panoramaSecretKey);

        assertEquals(userID, userIDAfterLinking, "ID of user shouldn't change after linking.");
        final Optional<UserDetails> userDetailsOpt = userDetailsReader.getDetails(userIDAfterLinking);
        final UserDetails userDetails = userDetailsOpt.get();
        assertEquals(userDetails.ID, userID);
        assertTrue(userDetails.linked);
        assertEquals(userDetails.credentials.size(), 2);
    }

    @Test
    public void create_only_one_account_when_user_is_added_twice_of_one_application() {
        final String chorusUsername = "andrii.loboda@fakechorus.com";
        final String chorusSecretKey = "fakechorus_secret_key_for_andrey";

        final long userID = userManagement.addApplicationForUser(
            Optional.<Long>absent(),
            CHORUS,
            chorusUsername,
            chorusSecretKey
        );

        final long userIDAfterSecond = userManagement.addApplicationForUser(
            Optional.absent(),
            CHORUS,
            chorusUsername,
            "new secret key"
        );


        assertEquals(userID, userIDAfterSecond, "ID of user shouldn't change after linking.");
        final Optional<UserDetails> userDetailsOpt = userDetailsReader.getDetails(userID);
        final UserDetails userDetails = userDetailsOpt.get();
        assertEquals(userDetails.ID, userID);
        assertFalse(userDetails.linked);
        assertEquals(userDetails.credentials.size(), 1);

        final ApplicationCredential chorusCredential = findChorusCredential(userDetails.credentials);
        assertEquals(chorusCredential.username, chorusUsername);
        assertEquals(chorusCredential.secretToken, chorusSecretKey);
        assertEquals(chorusCredential.applicationType, CHORUS);
    }

    @Test(expectedExceptions = AccountIsAlreadyLinkedException.class)
    public void throw_exception_when_user_links_to_already_linked_chorus_account() {
        final String panoramaUsername = "andrii.loboda@fakepanorama.com";
        final String panoramaSecretKey = "fakepanorama_secret_key_for_andrii.loboda";

        final String chorusUsername = "andrii.loboda@fakechorus.com";
        final String chorusSecretKey = "fakechorus_secret_key_for_andrey";

        final long andriiID = userManagement.addApplicationForUser(
            Optional.<Long>absent(),
            CHORUS,
            chorusUsername,
            chorusSecretKey
        );
        userManagement.addApplicationForUser(Optional.of(andriiID), PANORAMA, panoramaUsername, panoramaSecretKey);

        final long yevhenID = userManagement
            .addApplicationForUser(Optional.<Long>absent(), PANORAMA, "yevhen.panko@fakepanorama.com",
                "fakepanorama_secret_key_for_yevhen.panko"
            );
        userManagement.addApplicationForUser(Optional.of(yevhenID), CHORUS, chorusUsername, chorusSecretKey);
    }

    @Test(expectedExceptions = AccountIsAlreadyLinkedException.class)
    public void throw_exception_when_user_links_to_already_linked_panorama_account() {
        final String panoramaUsername = "andrii.loboda@fakepanorama.com";
        final String panoramaSecretKey = "fakepanorama_secret_key_for_andrii.loboda";

        final String chorusUsername = "andrii.loboda@fakechorus.com";
        final String chorusSecretKey = "fakechorus_secret_key_for_andrey";

        final long andriiID = userManagement.addApplicationForUser(
            Optional.<Long>absent(),
            CHORUS,
            chorusUsername,
            chorusSecretKey
        );
        userManagement.addApplicationForUser(Optional.of(andriiID), PANORAMA, panoramaUsername, panoramaSecretKey);

        final long yevhenID = userManagement
            .addApplicationForUser(Optional.<Long>absent(), CHORUS, "yevhen.panko@fakechorus.com",
                "fakechorus_secret_key_for_yevhen.panko"
            );
        userManagement.addApplicationForUser(Optional.of(yevhenID), PANORAMA, panoramaUsername, panoramaSecretKey);
    }

    @Test
    public void get_absent_if_user_is_not_present_with_specified_id() {
        final long fakeUserID = 42L;
        final Optional<UserDetails> userDetails = userDetailsReader.getDetails(fakeUserID);
        assertTrue(!userDetails.isPresent());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void throw_exception_when_creating_user_with_null_app_type() {
        userManagement.addApplicationForUser(Optional.<Long>absent(), null, "yevhen.panko@fakepanorama.com",
            "akepanorama_secret_key_for_yevhen.panko"
        );
    }

    private static ApplicationCredential findChorusCredential(Iterable<ApplicationCredential> credentials) {
        return Iterables.find(credentials, input -> input.applicationType == CHORUS);
    }
}
