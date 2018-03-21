package com.infoclinika.mssharing.model.internal.entity;

import com.google.common.collect.Iterables;
import com.infoclinika.mssharing.platform.entity.InstrumentModel;
import com.infoclinika.mssharing.platform.entity.InstrumentTemplate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.search.annotations.Field;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author Stanislav Kurilin
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "serialNumber"))
//@Indexed
public class Instrument extends InstrumentTemplate<User, Lab> {


    @Field
    String hplc;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "instrument_lock_masses")
    @Fetch(FetchMode.SELECT)
    private List<LockMz> lockMasses = newArrayList();

    public Instrument() {
    }

    public Instrument(String name, User creator, InstrumentModel model, String serialNumber, String hplc, String peripherals, Lab lab) {
        setName(name);
        setCreator(creator);
        setModel(model);
        setSerialNumber(serialNumber);
        getOperators().add(creator);
        setPeripherals(peripherals);
        setLab(lab);
        this.hplc = hplc;
    }

    public Instrument(long id) {
        setId(id);
    }


    public String getHplc() {
        return hplc;
    }

    public void setHplc(String hplc) {
        this.hplc = hplc;
    }

    public Lab getAssociatedLab() {
        return getLab();
    }

    public Date startPending(final User user) {
        return Iterables.find(getPending(), findUserInPending(user)).getStartPending();
    }

    public List<LockMz> getLockMasses() {
        return lockMasses;
    }
}
