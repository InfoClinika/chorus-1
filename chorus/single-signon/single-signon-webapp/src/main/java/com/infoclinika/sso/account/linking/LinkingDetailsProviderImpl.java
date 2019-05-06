package com.infoclinika.sso.account.linking;

import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import com.infoclinika.sso.UserCredentialInForm;
import com.infoclinika.sso.model.ApplicationType;
import com.infoclinika.sso.model.read.UserDetailsReader;
import com.infoclinika.sso.model.read.UserDetailsReader.UserDetails;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

/**
 * @author andrii.loboda
 */
@Service
public class LinkingDetailsProviderImpl implements LinkingDetailsProvider {
    private static final String USER_NOT_FOUND_MESSAGE =
        "User is not saved in system(type: %s, username: %s), it is illegal state. Please debug.";
    @Inject
    private UserDetailsReader userDetailsReader;

    @Override
    public boolean isAccountLinked(UserCredentialInForm credentials) {
        final Optional<UserDetails> details =
            userDetailsReader.getDetails(credentials.getApplicationType(), credentials.getUsername());

        checkState(details.isPresent(), USER_NOT_FOUND_MESSAGE,
            credentials.getUsername(), credentials.getApplicationType()
        );

        final UserDetails user = details.get();
        return user.linked;
    }

    @Override
    public LinkingUserDetails getUserDetailsForLinking(UserCredentialInForm credentials) {
        final Optional<UserDetails> details =
            userDetailsReader.getDetails(credentials.getApplicationType(), credentials.getUsername());

        checkState(details.isPresent(), USER_NOT_FOUND_MESSAGE,
            credentials.getUsername(), credentials.getApplicationType()
        );

        final UserDetails user = details.get();

        final List<ApplicationType> applicationsToLink = extractNotLinkedApplications(user);
        return new LinkingUserDetails(user.ID, applicationsToLink);
    }

    private static List<ApplicationType> extractNotLinkedApplications(UserDetails user) {
        final Set<ApplicationType> linkedApplications = newHashSet(Collections2
            .transform(
                user.credentials, i -> Objects.requireNonNull(i).applicationType
            ));
        final List<ApplicationType> applicationsToLink =
            newArrayList(Sets.difference(newHashSet(ApplicationType.values()), linkedApplications));
        Collections.sort(applicationsToLink);
        return applicationsToLink;
    }


}
