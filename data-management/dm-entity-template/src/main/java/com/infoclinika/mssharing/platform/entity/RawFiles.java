package com.infoclinika.mssharing.platform.entity;

import com.google.common.collect.ImmutableList;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

/**
 * @author Herman Zamula
 */
@Embeddable
public class RawFiles<FACTOR extends FactorTemplate<?, ?>, EXPERIMENT_FILE extends ExperimentFileTemplate<?, ?, ?>> {
    @Transient
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Fetch(value = FetchMode.SELECT)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "experiment_id")
    private List<EXPERIMENT_FILE> data = newArrayList();

    @Fetch(value = FetchMode.SELECT)
    @OneToMany(mappedBy = "experiment", orphanRemoval = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL, targetEntity = FactorTemplate.class)
    private List<FACTOR> factors = newArrayList();

    public RawFiles() {
    }

    @Transient
    public int numberOfFiles() {
        return getData().size();
    }

    public List<FACTOR> getFactors() {
        return factors;
    }

    @Transient
    public List<FACTOR> getFilteredFactors() {
        //for unknown reasons, sometimes hibernate duplicates same entries. So We just filter out.
        final HashSet<FACTOR> added = newHashSet();
        final ImmutableList.Builder<FACTOR> res = ImmutableList.builder();
        for (FACTOR factor : factors) {
            if (added.contains(factor)) {
                //these should newer happen. but it does.
                log.warn("Duplicate entity issue");
                continue;
            }
            res.add(factor);
            added.add(factor);

        }
        return res.build();
    }

    public List<EXPERIMENT_FILE> getData() {
        return data;
    }
}
