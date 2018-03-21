package com.infoclinika.mssharing.model.internal.entity.payment;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Elena Kurilina
 */
@Entity
@Table(name = "billing_transaction_details")
public class TransactionDetails extends AbstractPersistable<Long> {

    @ElementCollection
    @CollectionTable(name = "billing_transaction_details_parameters",
            joinColumns = @JoinColumn(name = "details_id"))
    @MapKeyColumn(name = "param_key")
    @Column(name = "param_value")
    private Map<String, String> parameters = new HashMap<>();

    @ManyToOne
    private LabPaymentAccount account;

    public TransactionDetails() {
    }

    public TransactionDetails(Map<String, String> parameters, LabPaymentAccount account) {
        this.account = account;
        this.parameters = parameters;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public LabPaymentAccount getAccount() {
        return account;
    }

    public void setAccount(LabPaymentAccount account) {
        this.account = account;
    }
}
