package com.infoclinika.mssharing.platform.model.impl.helper.adapters;

import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.model.impl.helper.DefaultInstrumentCreationHelper;
import org.springframework.stereotype.Component;

/**
 * @author Herman Zamula
 */
@Component
public class DefaultInstrumentCreationHelperAdapter extends DefaultInstrumentCreationHelper {
    @Override
    protected PotentialOperator transformOperator(UserTemplate input) {
        return new PotentialOperator(input.getId(), input.getFullName(), input.getEmail());
    }
}
