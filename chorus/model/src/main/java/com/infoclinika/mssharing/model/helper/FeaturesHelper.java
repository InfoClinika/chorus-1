package com.infoclinika.mssharing.model.helper;

import com.infoclinika.mssharing.model.features.ApplicationFeature;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * @author timofei.kasianov 1/31/17
 */
@Transactional
public interface FeaturesHelper {

    boolean isEnabled(ApplicationFeature feature);

    boolean isEnabledForLab(ApplicationFeature feature, long lab);

    Set<ApplicationFeature> allEnabledForLab(long lab);

    void set(ApplicationFeature feature, boolean enabled);

    void setForLabs(ApplicationFeature feature, Set<Long> labs);

}
