package com.infoclinika.sso.chorus;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.infoclinika.auth.ChorusAuthenticationService;
import com.infoclinika.auth.ChorusAuthenticationService.GetAttributesRequest;
import com.infoclinika.auth.ChorusAuthenticationService.GetAttributesResponse;
import com.infoclinika.auth.ChorusAuthenticationService.UserSecretKey;
import com.infoclinika.sso.UserCredentialInForm;
import com.infoclinika.sso.model.ApplicationType;
import com.infoclinika.sso.model.read.UserDetailsReader;
import com.infoclinika.sso.model.read.UserDetailsReader.UserDetails;
import com.infoclinika.sso.model.read.UserDetailsReader.UserDetails.ApplicationCredential;
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
public class ChorusAttributesPopulator implements AuthenticationMetaDataPopulator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChorusAttributesPopulator.class);

    @Inject
    private UserDetailsReader userDetailsReader;
    @Resource(name = "chorusAuthenticationService")
    private ChorusAuthenticationService chorusAuthenticationService;

    @Override
    public void populateAttributes(AuthenticationBuilder builder, Credential credential) {
        final UserCredentialInForm userCredentials = (UserCredentialInForm) credential;
        final Optional<UserDetails> details =
            userDetailsReader.getDetails(userCredentials.getApplicationType(), userCredentials.getUsername());
        checkState(
            details.isPresent(), "User is not present in the system, applicationType: %s, username: %s",
            userCredentials.getApplicationType(), userCredentials.getUsername()
        );

        final UserDetails userDetails = details.get();
        /*Prepares attributes only when user account is fully linked(it is redundant while they are not linked). */
        if (userDetails.linked) {
            specifyChorusAttributes(builder, userDetails);
        }
    }


    @Override
    public boolean supports(Credential credential) {
        return true;
    }

    private void specifyChorusAttributes(AuthenticationBuilder builder, UserDetails userDetails) {
        try {
            final ApplicationCredential chorusCredential = findChorusCredential(userDetails);
            final GetAttributesRequest request =
                new GetAttributesRequest(chorusCredential.username, new UserSecretKey(chorusCredential.secretToken));

            final GetAttributesResponse response = chorusAuthenticationService.getAttributes(request);

            for (Map.Entry<String, Object> attributeToAdd : response.attributes.attributes.entrySet()) {
                builder.addAttribute(attributeToAdd.getKey(), String.valueOf(attributeToAdd.getValue()));
            }
        } catch (RuntimeException e) {
            LOGGER.error(
                "Exception occurred while retrieving attributes from Chorus endpoint, message: {}",
                e.getMessage(),
                e
            );
            builder.getSuccesses().clear();
            builder.addFailure(this.getClass().getName(), e.getClass());
        }
    }

    private static ApplicationCredential findChorusCredential(UserDetails userDetails) {
        return Iterables.find(userDetails.credentials,
            credential -> credential.applicationType == ApplicationType.CHORUS
        );
    }
}
