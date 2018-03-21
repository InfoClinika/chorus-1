package com.infoclinika.mssharing.platform.model.impl.write;

import com.infoclinika.mssharing.platform.entity.InstrumentModel;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.RuleValidator;
import com.infoclinika.mssharing.platform.model.helper.write.InstrumentModelManager;
import com.infoclinika.mssharing.platform.model.write.InstrumentModelManagementTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * @author timofei.kasianov 12/6/16
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@Transactional
@Component
public class DefaultInstrumentModelManagement<
        MODEL_DETAILS extends InstrumentModelManagementTemplate.InstrumentModelDetails,
        MODEL extends InstrumentModel>
        implements InstrumentModelManagementTemplate<MODEL_DETAILS> {

    @Inject
    protected InstrumentModelManager<MODEL, MODEL_DETAILS> instrumentModelManager;
    @Inject
    protected RuleValidator ruleValidator;

    @Override
    public long create(long actor, MODEL_DETAILS details) {
        beforeCreate(actor, details);
        final MODEL model = onCreate(details);
        return model.getId();
    }

    @Override
    public void update(long actor, long modelId, MODEL_DETAILS details) {
        beforeUpdate(actor, modelId, details);
        onUpdate(modelId, details);
    }

    @Override
    public void delete(long actor, long modelId) {
        beforeDelete(actor, modelId);
        instrumentModelManager.delete(modelId);
    }


    protected void beforeCreate(long actor, MODEL_DETAILS details) {
        if (!ruleValidator.canUserManageInstrumentModels(actor)) {
            throw new AccessDenied("Only admin can create instrument model");
        }
        if (!ruleValidator.canInstrumentModelBeCreatedWithName(details.name, details.vendor)) {
            throw new IllegalStateException("Instrument model with name '" + details.name + "' already exists");
        }
    }

    protected MODEL onCreate(MODEL_DETAILS details) {
        return instrumentModelManager.create(details);
    }

    protected void beforeUpdate(long actor, long modelId, MODEL_DETAILS details) {
        if (!ruleValidator.canUserManageInstrumentModels(actor)) {
            throw new AccessDenied("Only admin can create instrument model");
        }
        if (!instrumentModelManager.exists(modelId)) {
            throw new IllegalStateException("Instrument model with ID: " + modelId + " is not found");
        }
        if (!ruleValidator.canInstrumentModelBeUpdatedWithName(modelId, details.name, details.vendor)) {
            throw new IllegalStateException("Instrument model with name '" + details.name + "' already exists");
        }
    }

    protected void onUpdate(long modelId, MODEL_DETAILS details) {
        instrumentModelManager.update(modelId, details);
    }

    protected void beforeDelete(long actor, long modelId) {
        if (!ruleValidator.canUserManageInstrumentModels(actor)) {
            throw new AccessDenied("Only admin can create instrument model");
        }
        if (!instrumentModelManager.exists(modelId)) {
            throw new IllegalStateException("Instrument model with ID: " + modelId + " is not found");
        }
        if (!ruleValidator.canInstrumentModelBeDeleted(modelId)) {
            throw new IllegalStateException("Instrument model with ID: " + modelId + " couldn't be deleted");
        }
    }

}
