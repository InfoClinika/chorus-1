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
package com.infoclinika.mssharing.platform.entity;


import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author andrii.loboda
 */
@Entity
@Table(name = "condition_")
public class Condition<E extends ExperimentTemplate<?, ?, ?, ?, ?, ?>, L extends LevelTemplate<?>,
    F extends ExperimentFileTemplate<?, ?, ?>>
    extends AbstractPersistable {

    public static final String UNDEFINED_CONDITION_NAME = "undefined";
    public static final long UNDEFINED_CONDITION_ID = -1L;
    private static final int LONG_STRING = 4000;

    @Column(name = "baseline_flag")
    private boolean baseLineFlag;

    @Column(name = "condition_name", length = LONG_STRING)
    private String name;

    @ManyToOne(targetEntity = ExperimentTemplate.class)
    @JoinColumns({@JoinColumn(name = "experiment_id")})
    private E experiment;

    @ManyToMany(targetEntity = LevelTemplate.class)
    @JoinTable(name = "condition_to_level",
        joinColumns =
        @JoinColumn(name = "condition_id", referencedColumnName = "id", nullable = false),
        inverseJoinColumns =
        @JoinColumn(name = "level_id", referencedColumnName = "id", nullable = false)
    )
    private List<L> levels = new ArrayList<>();

    @ManyToMany(targetEntity = ExperimentFileTemplate.class)
    @JoinTable(name = "condition_to_lane",
        joinColumns =
        @JoinColumn(name = "condition_id", referencedColumnName = "id", nullable = false),
        inverseJoinColumns =
        @JoinColumn(name = "lane_id", referencedColumnName = "id", nullable = false)
    )
    private List<F> files = new ArrayList<>();

    public Condition() {
    }

    public Condition(boolean baseLineFlag, String name, E experiment, List<L> levels, List<F> files) {
        this.baseLineFlag = baseLineFlag;
        this.name = name;
        this.experiment = experiment;
        this.levels = levels;
        this.files = files;
    }

    @Transient
    public static <
        E extends ExperimentTemplate<?, ?, ?, ?, ?, ?>,
        L extends LevelTemplate<?>,
        F extends ExperimentFileTemplate<?, E, ?>> Condition createCondition(E experiment,
                                                                             List<L> levels,
                                                                             Iterable<F> files) {
        return new Condition<>(false, getNameFromLevels(levels), experiment, levels, newArrayList(files));
    }

    @Transient
    public static <
        E extends ExperimentTemplate<?, ?, ?, ?, ?, ?>,
        F extends ExperimentFileTemplate<?, ?, ?>> Condition createUndefinedCondition(E experiment,
                                                                                      Iterable<F> files) {
        final Condition undefined =
            new Condition<>(false, UNDEFINED_CONDITION_NAME, experiment, null, newArrayList(files));
        undefined.setId(UNDEFINED_CONDITION_ID);
        return undefined;
    }

    @Transient
    private static String getNameFromLevels(List<? extends LevelTemplate<?>> levels) {
        final StringBuilder sb = new StringBuilder();
        final Iterator<? extends LevelTemplate<?>> iterator = levels.iterator();
        while (iterator.hasNext()) {
            final LevelTemplate l = iterator.next();

            String units = l.getFactor().getUnits();
            sb.append(l.getFactor().getName()).append(":").append(l.getName());
            if (!StringUtils.isBlank(units)) {
                sb.append("(").append(units).append(")");
            }
            if (iterator.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public boolean isBaseLineFlag() {
        return baseLineFlag;
    }

    public String getName() {
        return name;
    }

    public E getExperiment() {
        return experiment;
    }

    public List<L> getLevels() {
        return levels;
    }

    public List<F> getFiles() {
        return files;
    }
}
