package com.infoclinika.mssharing.platform.model.impl.read.adapters;

import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.model.helper.read.UserReaderHelper;
import com.infoclinika.mssharing.platform.model.impl.read.DefaultUserReader;
import com.infoclinika.mssharing.platform.model.read.UserReaderTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author : Alexander Serebriyan
 */
@Component
public class DefaultUserReaderAdapter extends DefaultUserReader<UserTemplate, UserReaderTemplate.UserLineTemplate> {

    @Inject
    private UserReaderHelper<UserTemplate, UserLineTemplate> userReaderHelper;

    @Override
    public UserLineTemplate transform(UserTemplate userTemplate) {
        return userReaderHelper.getDefaultTransformer().apply(userTemplate);
    }
}
