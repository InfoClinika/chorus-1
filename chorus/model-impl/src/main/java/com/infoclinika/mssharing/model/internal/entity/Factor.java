/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika,
 * Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use,
 * duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.internal.entity;

import com.google.common.collect.Sets;
import com.infoclinika.mssharing.model.internal.entity.restorable.AbstractExperiment;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveExperiment;
import com.infoclinika.mssharing.platform.entity.FactorTemplate;

import javax.persistence.Entity;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.infoclinika.mssharing.platform.entity.FactorTemplate.Type.INTEGER;
import static com.infoclinika.mssharing.platform.entity.FactorTemplate.Type.STRING;

/**
 * @author Stanislav Kurilin
 */
@Entity
public class Factor extends FactorTemplate<Level, AbstractExperiment> {

    public Factor() {
    }

    Factor(
        String name,
        Type type,
        String units,
        ActiveExperiment experiment,
        boolean isDefaultFactor,
        Set<Level> levelValues) {
        setName(name);
        setType(type);
        setUnits(units);
        setExperiment(experiment);
        setDefault(isDefaultFactor);
        getLevels().addAll(levelValues);
    }

    public Factor(String name, Type type, String units, AbstractExperiment experiment, boolean defaultFactor) {
        setName(name);
        setType(type);
        setUnits(units);
        setExperiment(experiment);
        setDefault(defaultFactor);
    }

    public static Factor stringFactor(String name, ActiveExperiment experiment, Set<String> levelValues) {
        return createFactorWithLevelTransformation(name, STRING, "", experiment, false, levelValues);
    }

    public static Factor stringFactor(
        String name,
        ActiveExperiment experiment,
        boolean isDefaultFactor,
        Set<String> levelValues
    ) {
        return createFactorWithLevelTransformation(name, STRING, "", experiment, isDefaultFactor, levelValues);
    }

    public static Factor integerFactor(
        String name,
        String units,
        ActiveExperiment experiment,
        Set<String> levelValues
    ) {
        return createFactorWithLevelTransformation(name, INTEGER, units, experiment, false, levelValues);
    }

    public static Factor integerFactor(
        String name,
        String units,
        ActiveExperiment experiment,
        boolean isDefaultFactor,
        Set<String> levels
    ) {
        return createFactorWithLevelTransformation(name, INTEGER, units, experiment, isDefaultFactor, levels);
    }

    public static Factor createCopy(Factor f, AbstractExperiment experiment) {
        final Factor factor = new Factor(f.getName(), f.getType(), f.getUnits(), experiment, f.isDefault());
        factor.getLevels().addAll(
            f.getLevels()
                .stream()
                .map(input -> new Level(input.getName(), factor))
                .collect(Collectors.toSet()));

        return factor;
    }


    private static Factor createFactorWithLevelTransformation(
        String name,
        Type type,
        String unit,
        ActiveExperiment experiment,
        boolean isDefaultFactor,
        Set<String> levelValues
    ) {
        Factor factor = new Factor(name, type, unit, experiment, isDefaultFactor, Sets.newHashSet());
        factor.getLevels().addAll(transformToLevel(levelValues, factor));

        return factor;
    }

    private static Set<Level> transformToLevel(Collection<String> levelValues, Factor factor) {
        Set<Level> levels = new HashSet<>();
        for (String s : levelValues) {
            levels.add(new Level(s, factor));
        }

        return levels;
    }

}
