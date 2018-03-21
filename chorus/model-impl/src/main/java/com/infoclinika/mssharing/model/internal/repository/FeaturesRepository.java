/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.internal.repository;

import com.google.common.base.Predicate;
import com.infoclinika.mssharing.model.internal.entity.Feature;
import com.infoclinika.mssharing.model.internal.entity.Lab;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.FluentIterable.from;


/**
 * @author pavel.kaplin, andrii.loboda, herman.zamula
 */
@Repository
@Transactional(readOnly = true)
public class FeaturesRepository {

    private static final int DISABLED_STATED = Feature.FeatureState.DISABLED.ordinal();

    @PersistenceContext(unitName = "mssharing")
    protected EntityManager em;

    public Map<String, Feature> get() {
        List<Feature> features = em.createQuery("from Feature", Feature.class).getResultList();
        Map<String, Feature> result = new HashMap<>();
        for (Feature feature : features) {
            result.put(feature.getName(), feature);
        }
        return result;
    }

    public Set<String> allEnabledForLab(long lab) {
        return get().values()
                .stream()
                .filter(f -> {
                    final Feature.FeatureState enabledState = f.getEnabledState();
                    return enabledState == Feature.FeatureState.ENABLED ||
                            enabledState == Feature.FeatureState.ENABLED_PER_LAB && f.getEnabledLabs().contains(new Lab(lab));
                })
                .map(Feature::getName)
                .collect(Collectors.toSet());
    }

    public boolean enabledForLab(String name, final long lab) {
        final Feature feature = get().get(name);
        switch (feature.getEnabledState()) {
            case ENABLED:
                return true;
            case ENABLED_PER_LAB:
                return from(feature.getEnabledLabs()).anyMatch(new Predicate<Lab>() {
                    @Override
                    public boolean apply(Lab input) {
                        return input.getId().equals(lab);
                    }
                });
            case DISABLED:
            default:
                return false;
        }
    }

    @Transactional
    public void set(String name, Feature.FeatureState enabled) {
        Feature feature = em.find(Feature.class, name);
        if (feature == null) {
            feature = new Feature(name);
        }
        feature.setEnabledState(enabled);
        feature.getEnabledLabs().clear();
        em.persist(feature);
    }

    @Transactional
    public void set(String name, Feature.FeatureState enabled, Set<Lab> labs) {
        Feature feature = em.find(Feature.class, name);
        if (feature == null) {
            feature = new Feature(name);
        }
        feature.setEnabledState(enabled);
        feature.getEnabledLabs().clear();
        feature.getEnabledLabs().addAll(labs);
        em.persist(feature);
    }

    @Transactional
    public void delete(String name) {
        em.remove(em.find(Feature.class, name));
    }
}
