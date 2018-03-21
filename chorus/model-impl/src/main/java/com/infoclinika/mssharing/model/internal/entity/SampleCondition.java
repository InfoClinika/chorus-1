package com.infoclinika.mssharing.model.internal.entity;

import com.infoclinika.mssharing.model.internal.entity.restorable.AbstractExperiment;
import com.infoclinika.mssharing.platform.entity.ExperimentFileTemplate;
import com.infoclinika.mssharing.platform.entity.LevelTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author andrii.loboda
 */
@Entity
@Table(name = "sample_condition_")
public class SampleCondition extends AbstractPersistable<Long> {

    public static final String UNDEFINED_CONDITION_NAME = "undefined";
    public static final long UNDEFINED_CONDITION_ID = -1L;

    @Column(name = "baseline_flag")
    private boolean baseLineFlag;

    @Column(name = "condition_name", length = 4000)
    private String name;

    @ManyToOne(targetEntity = AbstractExperiment.class)
    @JoinColumns({@JoinColumn(name = "experiment_id")})
    private AbstractExperiment experiment;

    @ManyToMany(targetEntity = Level.class)
    @JoinTable(name = "sample_condition_to_level",
            joinColumns = @JoinColumn(name = "condition_id", referencedColumnName = "id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "level_id", referencedColumnName = "id", nullable = false)
    )
    private List<Level> levels = new ArrayList<>();

    @ManyToMany(targetEntity = ExperimentSample.class)
    @JoinTable(name = "sample_condition_to_sample",
            joinColumns = @JoinColumn(name = "condition_id", referencedColumnName = "id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "sample_id", referencedColumnName = "id", nullable = false)
    )
    private List<ExperimentSample> samples = new ArrayList<>();


    public SampleCondition() {
    }

    public SampleCondition(boolean baseLineFlag, String name, AbstractExperiment experiment, List<Level> levels, List<ExperimentSample> samples) {
        this.baseLineFlag = baseLineFlag;
        this.name = name;
        this.experiment = experiment;
        this.levels = levels;
        this.samples = samples;
    }

    @Transient
    public static <E extends ExperimentTemplate<?, ?, ?, ?, ?, ?>, F extends ExperimentFileTemplate<?, ?, ?>>
    SampleCondition createUndefinedCondition(AbstractExperiment experiment, Iterable<ExperimentSample> samples) {
        final SampleCondition undefined = new SampleCondition(false, UNDEFINED_CONDITION_NAME, experiment, null, newArrayList(samples));
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
    /*automatically generated getters, setters*/

    public boolean isBaseLineFlag() {
        return baseLineFlag;
    }

    public void setBaseLineFlag(boolean baseLineFlag) {
        this.baseLineFlag = baseLineFlag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AbstractExperiment getExperiment() {
        return experiment;
    }

    public void setExperiment(AbstractExperiment experiment) {
        this.experiment = experiment;
    }

    public List<Level> getLevels() {
        return levels;
    }

    public void setLevels(List<Level> levels) {
        this.levels = levels;
    }

    public List<ExperimentSample> getSamples() {
        return samples;
    }

    public void setSamples(List<ExperimentSample> samples) {
        this.samples = samples;
    }
}
