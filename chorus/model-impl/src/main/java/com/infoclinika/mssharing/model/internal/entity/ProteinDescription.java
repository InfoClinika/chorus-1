package com.infoclinika.mssharing.model.internal.entity;

import com.infoclinika.mssharing.model.internal.entity.AbstractAggregate;
import org.hibernate.annotations.Index;

import javax.persistence.*;

/**
 * @author andrii.loboda
 */
@Entity
@Table(name = "s_ProteinDescription")
public class ProteinDescription extends AbstractAggregate{
    @ManyToOne(optional = false)
    private ProteinDatabase database;

    @Basic(optional = false)
    @Index(name = "ProteinByAccession", columnNames = {"accessionNumber"})
    @Column(name = "accessionNumber",length = 500)
    private String proteinId;

    @Basic(optional = false)
    private double molWeight;

    @Basic(optional = false)
    @Column(length = 20)
    @Index(name = "proteinIdSource", columnNames = {"idSource"})
    private String idSource;

    @Basic(optional = true)
    private boolean fromFasta;

    public ProteinDescription(ProteinDatabase database, String proteinId, double molWeight, String idSource, boolean fromFasta) {
        this.database = database;
        this.proteinId = proteinId;
        this.molWeight = molWeight;
        this.idSource = idSource;
        this.fromFasta = fromFasta;
    }

    ProteinDescription(){}

    public ProteinDatabase getDatabase() {
        return database;
    }

    public void setDatabase(ProteinDatabase database) {
        this.database = database;
    }

    public String getProteinId() {
        return proteinId;
    }

    public void setProteinId(String proteinId) {
        this.proteinId = proteinId;
    }

    public double getMolWeight() {
        return molWeight;
    }

    public void setMolWeight(double molWeight) {
        this.molWeight = molWeight;
    }

    public String  getIdSource() {
        return idSource;
    }

    public void setIdSource(String  idSource) {
        this.idSource = idSource;
    }

    public boolean isFromFasta() {
        return fromFasta;
    }

    public void setFromFasta(boolean fromFasta) {
        this.fromFasta = fromFasta;
    }



}
