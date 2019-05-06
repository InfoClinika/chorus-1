package com.infoclinika.mssharing.platform.entity;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author timofei.kasianov 11/2/16
 */
@Entity
@Table(name = "instrument_study_type")
@AttributeOverride(name = "name", column = @Column(unique = true))
public class InstrumentStudyType extends Dictionary {

    public InstrumentStudyType() {
    }

    public InstrumentStudyType(String name) {
        super(name);
    }
}
