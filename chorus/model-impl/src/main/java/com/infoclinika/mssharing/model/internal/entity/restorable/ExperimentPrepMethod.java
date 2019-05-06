package com.infoclinika.mssharing.model.internal.entity.restorable;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author Vitalii Petkanych
 */
@Entity
@Table(name = "dict_experiment_prep_method")
public class ExperimentPrepMethod extends AbstractPersistable<Integer> {

    private String title;

    public ExperimentPrepMethod() {
    }

    public ExperimentPrepMethod(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
