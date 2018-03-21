package com.infoclinika.mssharing.model.internal.write;

import com.infoclinika.mssharing.model.write.InstrumentModelManagement;
import com.infoclinika.mssharing.platform.entity.InstrumentModel;
import com.infoclinika.mssharing.platform.model.impl.write.DefaultInstrumentModelManagement;
import com.infoclinika.mssharing.platform.model.write.InstrumentModelManagementTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author timofei.kasianov 12/7/16
 */
@Service
@Transactional
public class InstrumentModelManagementImpl
        extends DefaultInstrumentModelManagement<InstrumentModelManagementTemplate.InstrumentModelDetails, InstrumentModel>
        implements InstrumentModelManagement {
}
