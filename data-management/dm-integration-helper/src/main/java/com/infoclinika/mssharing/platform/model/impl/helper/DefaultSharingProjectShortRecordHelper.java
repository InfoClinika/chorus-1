package com.infoclinika.mssharing.platform.model.impl.helper;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.GroupTemplate;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.repository.UserRepositoryTemplate.UserShortRecord;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.google.common.collect.FluentIterable.from;

/**
 * @author Herman Zamula
 */
@Transactional(readOnly = true)
public abstract class DefaultSharingProjectShortRecordHelper

    <USER_SHORT_RECORD extends UserShortRecord, USER extends UserTemplate, GROUP extends GroupTemplate>

    extends DefaultSharingProjectHelper<USER, GROUP> {

    @Override
    public List<UserDetails> getAvailableUsers() {

        final List<USER_SHORT_RECORD> all = userRepository.findShortRecordsAll();
        return from(all)
            .transform(input -> transformUserDetails(input))
            .toList();

    }

    protected abstract UserDetails transformUserDetails(USER_SHORT_RECORD user);

}
