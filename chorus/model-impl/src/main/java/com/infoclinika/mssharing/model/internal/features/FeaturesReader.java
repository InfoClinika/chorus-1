package com.infoclinika.mssharing.model.internal.features;

import com.infoclinika.mssharing.model.features.ApplicationFeature;
import com.infoclinika.mssharing.model.internal.entity.Feature;
import com.infoclinika.mssharing.model.internal.repository.FeaturesRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.infoclinika.mssharing.model.internal.entity.Feature.FeatureState.ENABLED;
import static com.infoclinika.mssharing.model.internal.entity.Feature.FeatureState.ENABLED_PER_LAB;

/**
 * It is a service which allows to get info whether some particular feature is turned on or not
 *
 * @author Andrii Loboda
 */
@Service
public class FeaturesReader {

    @Inject
    private FeaturesRepository featuresRepository;

    public boolean isFeatureEnabled(ApplicationFeature appFeature) {
        final String featureName = appFeature.getFeatureName();
        final Feature feature = featuresRepository.get().get(featureName);
        if (feature == null) {
            return false;
        }
        final Feature.FeatureState state = feature.getEnabledState();
        return state == ENABLED || state == ENABLED_PER_LAB;
    }

}
