package com.infoclinika.mssharing.platform.entity;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * @author Herman Zamula
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class InstrumentTemplate<U extends UserTemplate<?>, L extends LabTemplate<?>> extends AbstractAggregate {

    @Index(name = "INSTRUMENT_NAME_IDX")
    private String name;
    @ManyToOne(optional = false)
    private U creator;
    @ManyToOne(optional = false)
    private InstrumentModel model;
    @Basic(optional = false)
    private String serialNumber;
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<U> operators = newHashSet();
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PendingOperator> pending = newHashSet();
    @ElementCollection
    private Set<String> invitedOperators = newHashSet();
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

    public Set<U> getOperators() {
        return operators;
    }

    public Set<PendingOperator> getPending() {
        return pending;
    }

    public void setPending(Set<PendingOperator> pending) {
        this.pending = pending;
    }

    @Deprecated
    public Set<String> getInvitedOperators() {
        return invitedOperators;
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

    @Transient
    public void addPending(PendingOperator pendingOperator) {
        getPending().add(pendingOperator);
    }

    /**
     * Removes from pending list. Do nothing if it wasn't presented there
     */
    @Transient
    public void removePending(U user) {
        getPending().remove(Iterables.find(getPending(), findUserInPending(user), null));
    }


    @Transient
    public void addOperator(U operator) {
        getOperators().add(operator);
    }

    protected Predicate<PendingOperator> findUserInPending(final U user) {
        return new Predicate<PendingOperator>() {
            @Override
            public boolean apply(PendingOperator input) {
                return input.getUser().equals(user);
            }
        };
    }

    @Transient
    public boolean isOperator(U operator) {
        return getOperators().contains(operator);
    }

    @Deprecated
    @Transient
    public void addInvitation(String email) {
        getInvitedOperators().add(email);
    }

    @Transient
    public boolean isPending(U user) {
        return Iterables.any(getPending(), findUserInPending(user));
    }

}
