package com.infoclinika.mssharing.platform.model.testing.helper;

import com.infoclinika.mssharing.platform.entity.UserLabMembershipRequestTemplate;
import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate;
import com.infoclinika.mssharing.platform.repository.UserLabMembershipRequestRepositoryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * @author Herman Zamula
 */
@Component
@Transactional
public class UserManagementHelper {

    @Inject
    private UserManagementTemplate userManagement;
    @Inject
    private UserLabMembershipRequestRepositoryTemplate userLabMembershipRequestRepository;

    public long createPersonAndApproveMembership(UserManagementTemplate.PersonInfo user, String password, Long lab, String emailVerificationUrl) {
        return userManagement.createPersonAndApproveMembership(user, password, newHashSet(lab), emailVerificationUrl);
    }

    public void updatePersonAndApproveMembership(long userId, UserManagementTemplate.PersonInfo user, Set<Long> labs) {
        userManagement.updatePerson(userId, user, labs);
        //noinspection unchecked
        final List<UserLabMembershipRequestTemplate> requests = userLabMembershipRequestRepository.findPendingByUser(userId);
        for (UserLabMembershipRequestTemplate request : requests) {
            userManagement.approveLabMembershipRequest(request.getLab().getHead().getId(), request.getId());
        }
    }

}
