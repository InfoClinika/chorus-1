package com.infoclinika.sso.model.internal.read;

import com.google.common.base.Optional;
import com.infoclinika.sso.model.ApplicationType;
import com.infoclinika.sso.model.internal.entity.User;
import com.infoclinika.sso.model.internal.repository.UserRepository;
import com.infoclinika.sso.model.internal.write.application.ApplicationTypeManagement;
import com.infoclinika.sso.model.internal.write.application.ApplicationTypeManagementFactory;
import com.infoclinika.sso.model.read.UserDetailsReader;
import com.infoclinika.sso.model.read.UserDetailsReader.UserDetails.ApplicationCredential;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * @author andrii.loboda
 */
@Service
@Transactional(readOnly = true)
public class UserDetailsReaderImpl implements UserDetailsReader {
    private static final int APPLICATIONS_SUPPORTED_COUNT = 2;
    @Inject
    private UserRepository userRepository;
    @Inject
    private ApplicationTypeManagementFactory appTypeManagementFactory;

    @Override
    public Optional<UserDetails> getDetails(ApplicationType applicationType, String username) {
        final ApplicationTypeManagement userManagement = appTypeManagementFactory.getInstance(applicationType);
        final User user = userManagement.findByUsername(username);
        return asUserDetails(user);
    }


    @Override
    public Optional<UserDetails> getDetails(long userID) {
        final User user = userRepository.findOne(userID);
        return asUserDetails(user);
    }

    private static Optional<UserDetails> asUserDetails(User user) {
        if (user == null) {
            return Optional.absent();
        }
        final Set<ApplicationCredential> appCredentials = newHashSet();
        if (user.getChorusSecretKey() != null) {
            appCredentials.add(new ApplicationCredential(ApplicationType.CHORUS, user.getChorusUsername(), user.getChorusSecretKey()));
        }
        if (user.getPanoramaSecretKey() != null) {
            appCredentials.add(new ApplicationCredential(ApplicationType.PANORAMA, user.getPanoramaUsername(), user.getPanoramaSecretKey()));
        }
        final boolean linked = appCredentials.size() == APPLICATIONS_SUPPORTED_COUNT;
        final UserDetails userLinkingDetails = new UserDetails(user.getId(), linked, appCredentials);
        return Optional.of(userLinkingDetails);
    }
}
