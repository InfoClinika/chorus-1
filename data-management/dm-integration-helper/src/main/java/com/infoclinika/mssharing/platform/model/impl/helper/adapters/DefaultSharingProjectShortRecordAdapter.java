package com.infoclinika.mssharing.platform.model.impl.helper.adapters;

import com.infoclinika.mssharing.platform.entity.GroupTemplate;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.model.impl.helper.DefaultSharingProjectShortRecordHelper;
import com.infoclinika.mssharing.platform.repository.UserRepositoryTemplate.UserShortRecord;
import org.springframework.stereotype.Component;

/**
 * @author Herman Zamula
 */
@Component
public class DefaultSharingProjectShortRecordAdapter
    extends DefaultSharingProjectShortRecordHelper<UserShortRecord, UserTemplate, GroupTemplate> {

    @Override
    protected UserDetails transformUserDetails(UserShortRecord user) {
        return new UserDetails(user.id, user.fullName, user.email);
    }

    @Override
    protected UserDetails transformUser(UserTemplate userTemplate) {
        return new UserDetails(userTemplate.getId(), userTemplate.getFullName(), userTemplate.getEmail());
    }

    @Override
    protected GroupDetails transformGroup(GroupTemplate groupTemplate) {
        return new GroupDetails(groupTemplate.getId(), groupTemplate.getName(), groupTemplate.getNumberOfMembers());
    }
}
