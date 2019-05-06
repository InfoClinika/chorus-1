package com.infoclinika.mssharing.platform.model.impl.helper;

import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.platform.entity.ChangeEmailRequest;
import com.infoclinika.mssharing.platform.entity.EntityUtil;
import com.infoclinika.mssharing.platform.entity.UserInvitationLink;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.model.helper.SecurityHelperTemplate;
import com.infoclinika.mssharing.platform.model.impl.DefaultTransformingTemplate;
import com.infoclinika.mssharing.platform.repository.UserInvitationLinkRepository;
import com.infoclinika.mssharing.platform.repository.UserRepositoryTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static com.google.common.collect.FluentIterable.from;

/**
 * @author : Alexander Serebriyan
 */
@Transactional(readOnly = true)
public abstract class DefaultSecurityHelper<USER extends UserTemplate,
    USER_DETAILS extends SecurityHelperTemplate.UserDetails>
    implements SecurityHelperTemplate<USER_DETAILS>, DefaultTransformingTemplate<USER, USER_DETAILS> {

    @Inject
    private UserRepositoryTemplate<USER> userRepository;
    @Inject
    private UserInvitationLinkRepository userInvitationLinkRepository;

    protected USER_DETAILS toUserDetails(USER user) {
        return transform(user);
    }

    protected UserDetails transformDefault(USER user) {

        if (user == null) {
            return null;
        }
        final ChangeEmailRequest changeEmailRequest = user.getChangeEmailRequest();
        String emailRequest = changeEmailRequest == null ? null : changeEmailRequest.getEmail();
        //noinspection unchecked
        final ImmutableSet<Long> labIds = from(user.getLabs()).transform(EntityUtil.ENTITY_TO_ID).toSet();
        return new UserDetails(
            user.getId(),
            user.getPersonData().getFirstName(),
            user.getPersonData().getLastName(),
            user.getEmail(),
            user.getPasswordHash(),
            user.isAdmin(),
            user.isEmailVerified(),
            user.getLastModification(),
            labIds,
            emailRequest
        );

    }

    @Nullable
    @Override
    public USER_DETAILS getUserDetailsByEmail(String email) {
        USER user = userRepository.findByEmail(email);
        return toUserDetails(user);
    }

    @Override
    public USER_DETAILS getUserDetails(long id) {
        final USER user = userRepository.findOne(id);
        return toUserDetails(user);
    }

    @Override
    public USER_DETAILS getUserDetailsByInvitationLink(String link) {
        UserInvitationLink invitationLink = userInvitationLinkRepository.findByLink(link);
        if (invitationLink == null) {
            return null;
        }
        //noinspection unchecked
        final USER user = (USER) invitationLink.getUser();
        return toUserDetails(user);
    }
}
