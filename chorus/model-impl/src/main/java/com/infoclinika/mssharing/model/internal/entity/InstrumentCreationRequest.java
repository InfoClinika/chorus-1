package com.infoclinika.mssharing.model.internal.entity;

import com.infoclinika.mssharing.platform.entity.InstrumentCreationRequestTemplate;
import com.infoclinika.mssharing.platform.entity.InstrumentModel;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * @author timofey.kasyanov
 *     date: 12.05.2014
 */
@Entity
@Table(name = "instrument_creation_request")
public class InstrumentCreationRequest extends InstrumentCreationRequestTemplate<User, Lab> {

    private String hplc;

    @ElementCollection
    @CollectionTable(
        name = "instrument_creation_request_lock_masses",
        joinColumns = {@JoinColumn(name = "request_id")}
    )
    private Set<LockMz> lockMasses = newHashSet();

    public InstrumentCreationRequest() {
    }

    public InstrumentCreationRequest(String name, User requester, InstrumentModel model,
                                     String serialNumber, String hplc, String peripherals,
                                     Lab lab, Date requestDate) {
        this.setName(name);
        this.setRequester(requester);
        this.setModel(model);
        this.setSerialNumber(serialNumber);
        this.setPeripherals(peripherals);
        this.setLab(lab);
        this.setRequestDate(requestDate);
        this.hplc = hplc;
    }

    public String getHplc() {
        return hplc;
    }

    public void setHplc(String hplc) {
        this.hplc = hplc;
    }

    public Set<LockMz> getLockMasses() {
        return lockMasses;
    }

}
