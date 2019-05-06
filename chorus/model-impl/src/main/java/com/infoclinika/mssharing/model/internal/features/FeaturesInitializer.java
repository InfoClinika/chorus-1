package com.infoclinika.mssharing.model.internal.features;

import com.infoclinika.mssharing.model.features.ApplicationFeature;
import com.infoclinika.mssharing.model.internal.entity.ApplicationSettings;
import com.infoclinika.mssharing.model.internal.entity.Feature;
import com.infoclinika.mssharing.model.internal.entity.Feature.FeatureState;
import com.infoclinika.mssharing.model.internal.repository.ApplicationSettingsRepository;
import com.infoclinika.mssharing.model.internal.repository.FeaturesRepository;
import com.infoclinika.mssharing.propertiesprovider.BillingPropertiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static com.infoclinika.mssharing.model.features.ApplicationFeature.*;
import static com.infoclinika.mssharing.model.internal.entity.Feature.FeatureState.DISABLED;
import static com.infoclinika.mssharing.model.internal.entity.Feature.FeatureState.ENABLED;

/**
 * @author Andrii Loboda, Herman Zamula
 */
@Service
public class FeaturesInitializer {
    private static final int MB_100 = 104857600;
    private static final int MB_10 = 10485760;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Inject
    private FeaturesRepository featuresRepository;

    @Inject
    private ApplicationSettingsRepository applicationSettingsRepository;

    @Inject
    private BillingPropertiesProvider billingPropertiesProvider;

    public void initializeFeatures() {

        deleteFeaturesNotSpecifiedInApplicationFeature();

        addFeatureIfAbsent(BLOG, ENABLED);
        addFeatureIfAbsent(EDITABLE_COLUMNS, DISABLED);
        addFeatureIfAbsent(SUBSCRIBE, DISABLED);
        addFeatureIfAbsent(GLACIER, DISABLED);
        addFeatureIfAbsent(BILLING, billingPropertiesProvider.isBillingEnabled() ? ENABLED : DISABLED);
        addFeatureIfAbsent(MICROARRAYS, DISABLED);
        addFeatureIfAbsent(ISA_TAB_EXPORT, ENABLED);
        addFeatureIfAbsent(ALIS, DISABLED);
        addFeatureIfAbsent(LTQ, DISABLED);
        addFeatureIfAbsent(PARSE_RULES_SELECTION, DISABLED);

        createSetting(MB_10, ApplicationSettingsRepository.MAX_FILE_SIZE_SETTING);
        createSetting(MB_100, ApplicationSettingsRepository.MAX_PROTEIN_DB_SIZE_SETTING);
        createSetting(168, ApplicationSettingsRepository.HOURS_TO_STORE_IN_TRASH);
    }

    private void createSetting(long size, String name) {
        ApplicationSettings settings = applicationSettingsRepository.findByName(name);
        if (settings == null) {
            settings = new ApplicationSettings(size, name);
        }
        settings.setValue(size);
        applicationSettingsRepository.save(settings);
    }

    private void addFeatureIfAbsent(ApplicationFeature feature, FeatureState enabled) {

        final String featureName = feature.getFeatureName();

        if (featuresRepository.exists(featureName)) {
            log.info("Feature {} already exists, skipping", featureName);
            return;
        }

        final Feature entity = new Feature(featureName);
        entity.setEnabledState(enabled);
        featuresRepository.save(entity);
    }

    private void deleteFeaturesNotSpecifiedInApplicationFeature() {

        final Set<String> applicationFeatures = Arrays
            .stream(ApplicationFeature.values())
            .map(ApplicationFeature::getFeatureName)
            .collect(Collectors.toSet());

        featuresRepository.findAll().forEach(featureEntity -> {
            final String featureName = featureEntity.getName();
            if (!applicationFeatures.contains(featureName)) {
                log.info("Feature {} is not specified in the {} ", featureName, ApplicationFeature.class);
                log.info("Delete feature {}", featureName);
                featuresRepository.delete(featureEntity);
            }
        });
    }
}
