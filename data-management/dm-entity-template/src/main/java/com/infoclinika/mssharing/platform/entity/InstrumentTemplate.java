package com.infoclinika.mssharing.platform.entity;

import org.hibernate.annotations.Index;

import javax.persistence.*;

/**
 * @author Herman Zamula
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class InstrumentTemplate<U extends UserTemplate<?>, L extends LabTemplate<?>>
    extends AbstractAggregate {

    @Index(name = "INSTRUMENT_NAME_IDX")
    private String name;

    @ManyToOne(optional = false)
    private U creator;

    @ManyToOne(optional = false)
    private InstrumentModel model;

    @Basic(optional = false)
    private String serialNumber;

    private String peripherals;

    @ManyToOne
    private L lab;

    public InstrumentTemplate() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public U getCreator() {
        return creator;
    }

    public void setCreator(U creator) {
        this.creator = creator;
    }

    public InstrumentModel getModel() {
        return model;
    }

    public void setModel(InstrumentModel model) {
        this.model = model;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getPeripherals() {
        return peripherals;
    }

    public void setPeripherals(String peripherals) {
        this.peripherals = peripherals;
    }

    public L getLab() {
        return lab;
    }

    public void setLab(L lab) {
        this.lab = lab;
    }
}
