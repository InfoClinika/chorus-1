package com.infoclinika.mssharing.model.write;

import com.google.common.collect.ImmutableSet;

/**
 * @author andrii loboda
 */
public interface FeaturesManagement {

    void add(String featureName, boolean enabled);

    void add(String featureName, boolean enabled, ImmutableSet<Long> labs);
}
