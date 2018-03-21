package com.infoclinika.mssharing.platform.model.helper.write;

import com.google.common.base.Predicate;
import com.infoclinika.mssharing.platform.entity.Condition;
import com.infoclinika.mssharing.platform.entity.ExperimentFileTemplate;
import com.infoclinika.mssharing.platform.entity.FactorTemplate;
import com.infoclinika.mssharing.platform.entity.LevelTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;

/**
 * @author Herman Zamula
 */
public class ConditionsFactory<E extends ExperimentTemplate<?, ?, ?, ?, FA, ?>, F extends ExperimentFileTemplate<?, E, ?>, L extends LevelTemplate<FA>, FA extends FactorTemplate<L, E>> {
    private final List<FA> factors;
    private final E experiment;
    private final List<Condition> conditions;
    private Iterable<F> rawFilesData;

    public ConditionsFactory(E experiment, List<FA> factors, Iterable<F> rawFilesData) {
        this.experiment = experiment;
        this.factors = factors;
        this.conditions = newArrayList();
        this.rawFilesData = rawFilesData;
    }

    public List<Condition> create() {
        if (factors.size() > 0) {
            // create conditions
            populateConditions(0, new ArrayList<L>());
        }
        return conditions;
    }

    private void populateConditions(int index, List<L> levels) {
        final Set<L> factorLevel = factors.get(index).getLevels();
        for (L level : factorLevel) {
            List<L> levelUp = new ArrayList<>(levels);
            levelUp.add(level);
            if (index < (factors.size() - 1)) {
                populateConditions(index + 1, levelUp);
            } else {
                addCondition(levelUp);
            }
        }
    }

    //TODO:2015-12-03:andrii.loboda: review this code, cover with tests. It is possible that not all conditions app creates really use them
    private void addCondition(List<L> levelUp) {
        final Iterable<F> rawFiles = getRawFilesByLevels(levelUp);
        final Condition<?, ?, ?> condition = Condition.createCondition(experiment, levelUp, rawFiles);
        for (L l : levelUp) {
            l.getConditions().add(condition);
        }
        for (F f : rawFiles) {
            f.getConditions().add(condition);
        }
        conditions.add(condition);
    }

    public Iterable<F> getRawFilesByLevels(final List<L> levels) {
        return filter(rawFilesData, new Predicate<F>() {
            @Override
            public boolean apply(F file) {
                final List<String> factorValues = file.getFactorValues();
                final Iterator<L> levelIterator = levels.iterator();
                for (String factorValue : factorValues) {
                    final L level = levelIterator.next();
                    if (!factorValue.equals(level.getName())) {
                        return false;
                    }
                }
                return true;
            }
        });
    }
}
