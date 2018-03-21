package com.infoclinika.mssharing.model.helper;

import com.infoclinika.mssharing.model.features.ApplicationFeature;

import java.util.Set;

/**
 * @author timofei.kasianov 1/31/17
 */
public interface FeaturesHelper {

    boolean isEnabledForLab(ApplicationFeature feature, long lab);

    Set<ApplicationFeature> allEnabledForLab(long lab);

}
