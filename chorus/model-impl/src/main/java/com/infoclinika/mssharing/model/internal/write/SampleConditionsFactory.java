package com.infoclinika.mssharing.model.internal.write;

import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.infoclinika.mssharing.model.internal.entity.ExperimentSample;
import com.infoclinika.mssharing.model.internal.entity.Factor;
import com.infoclinika.mssharing.model.internal.entity.Level;
import com.infoclinika.mssharing.model.internal.entity.SampleCondition;
import com.infoclinika.mssharing.model.internal.entity.restorable.AbstractExperiment;
import com.infoclinika.mssharing.platform.entity.LevelTemplate;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;

/**
 * @author andrii.loboda
 */
class SampleConditionsFactory {
    private final List<Factor> factors;
    private final AbstractExperiment experiment;
    private final List<SampleCondition> conditions;
    private final Iterable<ExperimentSample> samples;
    private final List<Object[]> factorValues;

    SampleConditionsFactory(AbstractExperiment experiment, List<Factor> factors, Iterable<ExperimentSample> samples) {
        this.experiment = experiment;
        this.factors = factors;
        this.conditions = newArrayList();
        this.samples = samples;
        this.factorValues = composeFactorValues(samples, factors.size());
    }

    List<SampleCondition> create() {
        if (factors.size() > 0) {
            for (int i = 0; i < factorValues.get(0).length; i++) {
                final List<Level> levels = new ArrayList<>();
                for (int j = 0; j < factors.size(); j++) {
                    final Factor factor = factors.get(j);
                    final Object[] factorValue = factorValues.get(j);

                    final String levelName = factorValue[i].toString();
                    addLevelToLevels(levels, factor, levelName);
                }


                addCondition(levels);
            }
        }
        return conditions;
    }

    private static void addLevelToLevels(List<Level> levels, Factor factor, String levelName) {
        for (Level level : factor.getLevels()) {
            if (level.getName().equals(levelName)) {
                levels.add(level);
                return;
            }
        }
    }


    private void addCondition(List<Level> levelUp) {
        final Iterable<ExperimentSample> rawFiles = getSamplesByLevels(levelUp);
        final SampleCondition condition = createCondition(experiment, levelUp, rawFiles);
        for (Level l : levelUp) {
            l.getSampleConditions().add(condition);
        }
        conditions.add(condition);
    }

    private Iterable<ExperimentSample> getSamplesByLevels(final List<Level> levels) {
        return filter(samples, new Predicate<ExperimentSample>() {
            @Override
            public boolean apply(ExperimentSample sample) {
                final List<String> factorValues = sample.getFactorValues();
                final Iterator<Level> levelIterator = levels.iterator();
                for (String factorValue : factorValues) {
                    final Level level = levelIterator.next();
                    if (!factorValue.equals(level.getName())) {
                        return false;
                    }
                }
                return true;
            }
        });
    }

    private static List<Object[]> composeFactorValues(Iterable<ExperimentSample> samples, int factorsCount) {
        final ListMultimap<Integer, String> factorIndexToFactorValues = asFactorIndexToFactorValuesMap(samples, factorsCount);

        return transformToFactorValuesList(factorIndexToFactorValues, factorsCount);
    }

    private static List<Object[]> transformToFactorValuesList(ListMultimap<Integer, String> factorIndexToFactorValues, int factorsCount) {
        final List<Object[]> factorsValuesList = newArrayList();
        for (int factorIndex = 0; factorIndex < factorsCount; factorIndex++) {
            final List<String> factorValues = factorIndexToFactorValues.get(factorIndex);
            factorsValuesList.add(factorValues.toArray());

        }

        return factorsValuesList;
    }

    private static ListMultimap<Integer, String> asFactorIndexToFactorValuesMap(Iterable<ExperimentSample> samples, int factorsCount) {
        final ArrayListMultimap<Integer, String> factorIndexToFactorValues = ArrayListMultimap.create();
        for (ExperimentSample sample : samples) {
            final List<String> sampleFactorValues = sample.getFactorValues();
            final int sampleFactorValuesCount = sampleFactorValues.size();
            checkState(factorsCount == sampleFactorValuesCount, "Number of factor values doesn't match with factors specified.");
            for (int factorIndex = 0; factorIndex < sampleFactorValuesCount; factorIndex++) {
                factorIndexToFactorValues.put(factorIndex, sampleFactorValues.get(factorIndex));

            }
        }
        return factorIndexToFactorValues;
    }

    @Transient
    private static SampleCondition createCondition(AbstractExperiment experiment, List<Level> levels, Iterable<ExperimentSample> samples) {
        return new SampleCondition(false, getNameFromLevels(levels), experiment, levels, newArrayList(samples));
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
}
