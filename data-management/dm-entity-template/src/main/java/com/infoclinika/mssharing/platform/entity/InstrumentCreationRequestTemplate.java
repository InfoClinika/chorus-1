package com.infoclinika.mssharing.platform.entity;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * @author Herman Zamula
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class InstrumentCreationRequestTemplate<U extends UserTemplate<?>, L extends LabTemplate<?>> extends AbstractAggregate {
    private String name;
    @ManyToOne(targetEntity = UserTemplate.class)
    private U requester;
    @ManyToOne(targetEntity = InstrumentModel.class)
    private InstrumentModel model;
    private String serialNumber;
    private String peripherals;
    @ManyToOne(targetEntity = LabTemplate.class)
    private L lab;
    private Date requestDate;
    @ManyToMany(fetch = FetchType.EAGER, targetEntity = UserTemplate.class)
    private Set<U> operators = newHashSet();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public Set<U> getOperators() {
        return operators;
    }

    public void setOperators(Set<U> operators) {
        this.operators = operators;
    }

    public L getLab() {
        return lab;
    }

    public void setLab(L lab) {
        this.lab = lab;
    }

    public U getRequester() {
        return requester;
    }

    public void setRequester(U requester) {
        this.requester = requester;
    }
}
