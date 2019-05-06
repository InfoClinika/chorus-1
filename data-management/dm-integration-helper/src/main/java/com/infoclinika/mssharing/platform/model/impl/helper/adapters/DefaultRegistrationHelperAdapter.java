package com.infoclinika.mssharing.platform.model.impl.helper.adapters;

import com.infoclinika.mssharing.platform.entity.LabTemplate;
import com.infoclinika.mssharing.platform.model.impl.helper.DefaultRegistrationHelperTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Herman Zamula
 */
@Component
public class DefaultRegistrationHelperAdapter extends DefaultRegistrationHelperTemplate {
    @Override
    protected LabItem transformLabItem(LabTemplate input) {
        return new LabItem(input.getId(), input.getName());
    }
}
