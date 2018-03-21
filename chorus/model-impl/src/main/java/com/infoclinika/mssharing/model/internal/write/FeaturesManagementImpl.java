package com.infoclinika.mssharing.model.internal.write;

import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.model.internal.entity.Feature;
import com.infoclinika.mssharing.model.internal.repository.FeaturesRepository;
import com.infoclinika.mssharing.model.write.FeaturesManagement;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import static com.google.common.collect.FluentIterable.from;
import static com.infoclinika.mssharing.model.internal.entity.Feature.FeatureState.DISABLED;
import static com.infoclinika.mssharing.model.internal.entity.Feature.FeatureState.ENABLED_PER_LAB;
import static com.infoclinika.mssharing.model.internal.entity.Util.LAB_FROM_ID;

/**
 * @author andrii.loboda
 */
@Service
public class FeaturesManagementImpl implements FeaturesManagement {
    @Inject
    private FeaturesRepository featuresRepository;

    @Override
    public void add(String featureName, boolean enabled) {
        featuresRepository.set(featureName, enabled ? Feature.FeatureState.ENABLED : DISABLED);
    }

    @Override
    public void add(String featureName, boolean enabled, ImmutableSet<Long> labs) {
        featuresRepository.set(featureName, enabled ? ENABLED_PER_LAB : DISABLED, from(labs).transform(LAB_FROM_ID).toSet());
    }
}
