package com.infoclinika.mssharing.model.internal.features;

import com.infoclinika.mssharing.model.features.ApplicationFeature;
import com.infoclinika.mssharing.model.internal.entity.ApplicationSettings;
import com.infoclinika.mssharing.model.internal.entity.Feature;
import com.infoclinika.mssharing.model.internal.entity.Feature.FeatureState;
import com.infoclinika.mssharing.model.internal.repository.ApplicationSettingsRepository;
import com.infoclinika.mssharing.model.internal.repository.FeaturesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Map;

import static com.infoclinika.mssharing.model.features.ApplicationFeature.*;
import static com.infoclinika.mssharing.model.internal.entity.Feature.FeatureState.*;

/**
 * @author Andrii Loboda, Herman Zamula
 */
@Service
public class FeaturesInitializer {
    public static final int MB_100 = 104857600;
    public static final int MB_10 = 10485760;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${billing.enabled}")
    private boolean billingFeatureEnabled;
    @Inject
    private FeaturesRepository featuresRepository;
    @Inject
    private ApplicationSettingsRepository applicationSettingsRepository;

    public void initializeFeatures() {
        Map<String, Feature> existing = featuresRepository.get();

        addFeatureIfAbsent(PROTEIN_ID_SEARCH, ENABLED_PER_LAB, existing);
        addFeatureIfAbsent(PROTEIN_ID_SEARCH_RESULTS, ENABLED_PER_LAB, existing);
        addFeatureIfAbsent(BLOG, ENABLED, existing);
        addFeatureIfAbsent(EDITABLE_COLUMNS, DISABLED, existing);
        addFeatureIfAbsent(SUBSCRIBE, DISABLED, existing);
        addFeatureIfAbsent(GLACIER, DISABLED, existing);
        addFeatureIfAbsent(BILLING, billingFeatureEnabled ? ENABLED : DISABLED, existing);
        addFeatureIfAbsent(MICROARRAYS, DISABLED, existing);
        addFeatureIfAbsent(ISA_TAB_EXPORT, ENABLED, existing);

        createSetting(MB_10, ApplicationSettingsRepository.MAX_FILE_SIZE_SETTING);
        createSetting(MB_100, ApplicationSettingsRepository.MAX_PROTEIN_DB_SIZE_SETTING);
        createSetting(168, ApplicationSettingsRepository.HOURS_TO_STORE_IN_TRASH);
    }

    public void createSetting(long size, String name) {
        ApplicationSettings settings = applicationSettingsRepository.findByName(name);
        if (settings == null) {
            settings = new ApplicationSettings(size, name);
        }
        settings.value = size;
        applicationSettingsRepository.save(settings);
    }

    private void addFeatureIfAbsent(ApplicationFeature feature, FeatureState enabled, Map<String, Feature> existing) {
        final String featureName = feature.getFeatureName();
        if (existing.containsKey(featureName)) {
            log.info("Feature " + featureName + " already exists, skipping");
            return;
        }
        featuresRepository.set(featureName, enabled);
    }
}
