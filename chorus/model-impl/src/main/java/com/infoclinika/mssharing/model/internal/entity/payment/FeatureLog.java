package com.infoclinika.mssharing.model.internal.entity.payment;


import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author Elena Kurilina
 */
@Entity
@Table(name = "billing_feature_log")
public class FeatureLog extends Log {

    public enum FeatureAction {
        CANCEL_UPDATE_PLAN("Change billing plan was canceled"),
        REQUEST_CHANGE_BILLING_PLAN("Requested a change the billing plan"),
        UPDATE_BILLING_PLAN("Billing plan was changed");

        public String value;

        FeatureAction(String value) {
            this.value = value;
        }
    }

    public FeatureAction action;

    public String message;

    public FeatureLog() {
    }


    public FeatureLog(Long lab, Date timestamp, long totalToPay, long storeBalance, FeatureAction action, String message) {
        super(lab, timestamp, totalToPay, storeBalance);
        this.action = action;
        this.message = message;
    }
}
