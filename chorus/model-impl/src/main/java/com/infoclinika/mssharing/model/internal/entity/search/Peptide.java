package com.infoclinika.mssharing.model.internal.entity.search;

import com.infoclinika.mssharing.model.internal.entity.AbstractAggregate;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author andrii.loboda
 */
@Entity
@Table(name = "s_Peptide")
public class Peptide extends AbstractAggregate {
    private String sequence;

    Peptide() {
    }

    public Peptide(String sequence) {
        this.sequence = sequence;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

}
