package com.infoclinika.sso.panorama;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.infoclinika.sso.UserCredentialInForm;
import com.infoclinika.sso.model.ApplicationType;
import com.infoclinika.sso.model.read.UserDetailsReader;
import com.infoclinika.sso.model.read.UserDetailsReader.UserDetails;
import com.infoclinika.sso.model.read.UserDetailsReader.UserDetails.ApplicationCredential;
import com.infoclinika.sso.panorama.auth.PanoramaAuthenticationService;
import com.infoclinika.sso.panorama.auth.PanoramaAuthenticationService.AuthenticateUserRequest;
import com.infoclinika.sso.panorama.auth.PanoramaAuthenticationService.AuthenticateUserResponse;
import org.jasig.cas.authentication.AuthenticationBuilder;
import org.jasig.cas.authentication.AuthenticationMetaDataPopulator;
import org.jasig.cas.authentication.Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.inject.Inject;

import java.util.Map;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author andrii.loboda
 */
@Service
public class PanoramaAttributesPopulator implements AuthenticationMetaDataPopulator {
    private static final Logger LOG = LoggerFactory.getLogger(PanoramaAttributesPopulator.class);
    @Inject
    private UserDetailsReader userDetailsReader;
    @Resource(name = "panoramaAuthenticationService")
    private PanoramaAuthenticationService panoramaAuthenticationService;

    @Override
    public void populateAttributes(AuthenticationBuilder builder, Credential credential) {
        final UserCredentialInForm userCredentials = (UserCredentialInForm) credential;
        final Optional<UserDetails> details = userDetailsReader.getDetails(userCredentials.getApplicationType(), userCredentials.getUsername());
        checkState(details.isPresent(), "User is not present in the system, applicationType: %s, username: %s", userCredentials.getApplicationType(), userCredentials.getUsername());

        final UserDetails userDetails = details.get();
        /*Prepares attributes only when user account is fully linked(it is redundant while they are not linked). */
        if (userDetails.linked) {
            specifyPanoramaAttributes(builder, userDetails);
        }
    }


    @Override
    public boolean supports(Credential credential) {
        return true;
    }

    private void specifyPanoramaAttributes(AuthenticationBuilder builder, UserDetails userDetails) {
        try {
            final ApplicationCredential panoramaCredential = findPanoramaCredential(userDetails);

            final AuthenticateUserRequest request = new AuthenticateUserRequest(panoramaCredential.username, panoramaCredential.secretToken);
            final AuthenticateUserResponse response = panoramaAuthenticationService.authenticateUser(request);//replace it with get attributes method call once it available

            for (Map.Entry<String, Object> attributeToAdd : response.user.entrySet()) {
                builder.addAttribute(attributeToAdd.getKey(), String.valueOf(attributeToAdd.getValue()));
            }
        } catch (RuntimeException e) {
            LOG.error("Exception occurred while retrieving attributes from Panorama endpoint, message: " + e.getMessage(), e);
            builder.getSuccesses().clear();
            builder.addFailure(this.getClass().getName(), e.getClass());
        }
    }

    private static ApplicationCredential findPanoramaCredential(UserDetails userDetails) {
        return Iterables.find(userDetails.credentials, new Predicate<ApplicationCredential>() {
            @Override
            public boolean apply(ApplicationCredential credential) {
                return credential.applicationType == ApplicationType.PANORAMA;
            }
        });
    }
}
