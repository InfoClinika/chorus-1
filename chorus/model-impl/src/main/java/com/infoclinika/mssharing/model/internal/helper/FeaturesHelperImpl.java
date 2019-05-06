package com.infoclinika.mssharing.model.internal.helper;

import com.infoclinika.mssharing.model.features.ApplicationFeature;
import com.infoclinika.mssharing.model.helper.FeaturesHelper;
import com.infoclinika.mssharing.model.internal.entity.Feature;
import com.infoclinika.mssharing.model.internal.entity.Lab;
import com.infoclinika.mssharing.model.internal.repository.FeaturesRepository;
import com.infoclinika.mssharing.model.internal.repository.LabRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.infoclinika.mssharing.model.internal.entity.Feature.FeatureState.*;

/**
 * @author timofei.kasianov 1/31/17
 */
@Component
public class FeaturesHelperImpl implements FeaturesHelper {

    @Inject
    private FeaturesRepository featuresRepository;
    @Inject
    private LabRepository labRepository;

    @Override
    public boolean isEnabled(ApplicationFeature feature) {
        final Feature entity = featuresRepository.findOne(feature.getFeatureName());
        return entity != null && entity.getEnabledState() != DISABLED;
    }

    @Override
    public boolean isEnabledForLab(ApplicationFeature feature, long lab) {
        return featuresRepository.isEnabledForLab(feature.getFeatureName(), lab);
    }

    @Override
    public Set<ApplicationFeature> allEnabledForLab(long lab) {
        return featuresRepository
            .enabledForLab(lab).stream()
            .map(f -> ApplicationFeature.ofName(f.getName()))
            .collect(Collectors.toSet());
    }

    @Override
    public void set(ApplicationFeature feature, boolean enabled) {

        Feature entity = featuresRepository.findOne(feature.getFeatureName());

        if (entity == null) {
            entity = new Feature(feature.getFeatureName());
        }

        entity.setEnabledState(enabled ? ENABLED : DISABLED);
        featuresRepository.save(entity);
    }

    @Override
    public void setForLabs(ApplicationFeature feature, Set<Long> labs) {

        Feature entity = featuresRepository.findOne(feature.getFeatureName());

        if (entity == null) {
            entity = new Feature(feature.getFeatureName());
        }

        entity.setEnabledState(ENABLED_PER_LAB);

        final List<Lab> labEntities = labRepository.findAll(labs);

        entity.getEnabledLabs().clear();
        entity.getEnabledLabs().addAll(labEntities);
        featuresRepository.save(entity);
    }
}
