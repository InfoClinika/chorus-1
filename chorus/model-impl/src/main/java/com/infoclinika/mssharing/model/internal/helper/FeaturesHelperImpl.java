package com.infoclinika.mssharing.model.internal.helper;

import com.infoclinika.mssharing.model.features.ApplicationFeature;
import com.infoclinika.mssharing.model.helper.FeaturesHelper;
import com.infoclinika.mssharing.model.internal.repository.FeaturesRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author timofei.kasianov 1/31/17
 */
@Component
public class FeaturesHelperImpl implements FeaturesHelper {

    @Inject
    private FeaturesRepository featuresRepository;

    @Override
    public boolean isEnabledForLab(ApplicationFeature feature, long lab) {
        return featuresRepository.enabledForLab(feature.getFeatureName(), lab);
    }

    @Override
    public Set<ApplicationFeature> allEnabledForLab(long lab) {
        return featuresRepository
                .allEnabledForLab(lab).stream()
                .map(ApplicationFeature::ofName)
                .collect(Collectors.toSet());
    }
}
