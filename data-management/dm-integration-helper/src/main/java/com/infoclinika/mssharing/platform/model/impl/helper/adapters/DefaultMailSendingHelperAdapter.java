package com.infoclinika.mssharing.platform.model.impl.helper.adapters;

import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.model.helper.MailSendingHelperTemplate;
import com.infoclinika.mssharing.platform.model.impl.helper.DefaultMailSendingHelper;
import org.springframework.stereotype.Component;

/**
 * @author Herman Zamula
 */
@Component
public class DefaultMailSendingHelperAdapter extends DefaultMailSendingHelper<MailSendingHelperTemplate.UserDetails> {
    @Override
    public UserDetails transform(UserTemplate userTemplate) {
        return transformDefault(userTemplate);
    }
}
