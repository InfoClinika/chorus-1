package com.infoclinika.mssharing.platform.entity;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author Pavel Kaplin
 */
@Entity
@AttributeOverride(name = "name", column = @Column(unique = true))
@Table(name = "Specie")
public class Species extends Dictionary {
    public Species(String name) {
        super(name);
    }

    public Species() {
    }
}
