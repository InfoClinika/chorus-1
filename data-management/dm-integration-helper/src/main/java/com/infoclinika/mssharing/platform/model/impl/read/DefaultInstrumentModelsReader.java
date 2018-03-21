package com.infoclinika.mssharing.platform.model.impl.read;

import com.infoclinika.mssharing.platform.entity.InstrumentModel;
import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import com.infoclinika.mssharing.platform.model.RuleValidator;
import com.infoclinika.mssharing.platform.model.helper.read.InstrumentModelReaderHelper;
import com.infoclinika.mssharing.platform.model.impl.DefaultTransformingTemplate;
import com.infoclinika.mssharing.platform.model.read.InstrumentModelReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.InstrumentModelReaderTemplate.InstrumentModelLineTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Set;

@SuppressWarnings({"unused", "WeakerAccess"})
@Transactional(readOnly = true)
public abstract class DefaultInstrumentModelsReader<MODEL extends InstrumentModel, MODEL_LINE extends InstrumentModelLineTemplate>
        implements InstrumentModelReaderTemplate<MODEL_LINE>, DefaultTransformingTemplate<MODEL, MODEL_LINE> {

    @Inject
    protected InstrumentModelReaderHelper<MODEL, MODEL_LINE> instrumentModelReaderHelper;
    @Inject
    protected RuleValidator ruleValidator;

    @PostConstruct
    private void init() {
        instrumentModelReaderHelper.setTransformer(this::transform);
    }

    @Override
    public MODEL_LINE readById(long actor, long modelId) {
        beforeReadInstrumentModels(actor);
        return instrumentModelReaderHelper.readById(modelId).transform();
    }

    @Override
    public Set<MODEL_LINE> readByVendor(long actor, long vendorId) {
        beforeReadInstrumentModels(actor);
        return instrumentModelReaderHelper.readByVendor(vendorId).transform().toSet();
    }

    @Override
    public Set<MODEL_LINE> readByStudyType(long actor, long typeId) {
        beforeReadInstrumentModels(actor);
        return instrumentModelReaderHelper.readByTechnologyType(typeId).transform().toSet();
    }

    @Override
    public Set<MODEL_LINE> readByStudyTypeAndVendor(long actor, long typeId, long vendorId) {
        beforeReadInstrumentModels(actor);
        return instrumentModelReaderHelper.readByStudyTypeAndVendor(typeId, vendorId).transform().toSet();
    }

    @Override
    public PagedItem<MODEL_LINE> readInstrumentModels(long actor, PagedItemInfo pagedItem) {
        beforeReadInstrumentModels(actor);
        return instrumentModelReaderHelper.readPaged(pagedItem).transform();
    }

    protected void beforeReadInstrumentModels(long actor) {
        ruleValidator.canUserReadInstrumentModels(actor);
    }
}
