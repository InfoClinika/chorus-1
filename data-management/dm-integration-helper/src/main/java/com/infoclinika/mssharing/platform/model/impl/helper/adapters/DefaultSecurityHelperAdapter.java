package com.infoclinika.mssharing.platform.model.impl.helper.adapters;

import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.model.helper.SecurityHelperTemplate;
import com.infoclinika.mssharing.platform.model.impl.helper.DefaultSecurityHelper;
import org.springframework.stereotype.Component;

/**
 * @author Herman Zamula
 */
@Component
public class DefaultSecurityHelperAdapter extends DefaultSecurityHelper<UserTemplate, SecurityHelperTemplate.UserDetails> {
    @Override
    public UserDetails transform(UserTemplate userTemplate) {
        return transformDefault(userTemplate);
    }
}
