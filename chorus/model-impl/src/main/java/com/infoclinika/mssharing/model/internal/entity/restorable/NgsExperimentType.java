package com.infoclinika.mssharing.model.internal.entity.restorable;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.List;

/**
 * @author : Alexander Serebriyan
 */
@Entity
@Table(name = "dict_experiment_type")
public class NgsExperimentType extends AbstractPersistable<Integer> {
    private String title;
    @ManyToMany
    @JoinTable(name = "dict_experiment_type_to_prep_method",
        joinColumns = @JoinColumn(name = "exp_type_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "exp_prep_method_id", referencedColumnName = "id"))
    private List<ExperimentPrepMethod> prepMethods;

    public NgsExperimentType() {
    }

    public NgsExperimentType(String title, List<ExperimentPrepMethod> prepMethods) {
        this.title = title;
        this.prepMethods = prepMethods;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<ExperimentPrepMethod> getPrepMethods() {
        return prepMethods;
    }

    public void setPrepMethods(List<ExperimentPrepMethod> prepMethods) {
        this.prepMethods = prepMethods;
    }
}
