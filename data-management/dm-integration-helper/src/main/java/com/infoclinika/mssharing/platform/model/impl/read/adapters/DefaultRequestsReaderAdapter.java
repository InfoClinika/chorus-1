package com.infoclinika.mssharing.platform.model.impl.read.adapters;

import com.infoclinika.mssharing.platform.entity.*;
import com.infoclinika.mssharing.platform.model.impl.read.DefaultRequestsReader;
import org.springframework.stereotype.Component;

/**
 * @author Herman Zamula
 */
@Component
public class DefaultRequestsReaderAdapter extends DefaultRequestsReader<InstrumentTemplate,
        ProjectSharingRequestTemplate,
        UserLabMembershipRequestTemplate,
        LabCreationRequestTemplate,
        InstrumentCreationRequestTemplate> {

}
