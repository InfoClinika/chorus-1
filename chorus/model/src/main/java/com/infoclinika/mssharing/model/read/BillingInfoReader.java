package com.infoclinika.mssharing.model.read;

import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.services.billing.rest.api.model.LabAccountFeatureInfo;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

/**
 * @author Herman Zamula
 */
@Transactional(readOnly = true)
public interface BillingInfoReader {

    ImmutableSet<LabDebtDetails> readBillingInfo(long actor);

    Map<String, String> readBillingProperties(long actor);

    Set<LabAccountFeatureInfo> readLabAccountFeatures(long lab);

    class LabDebtDetails {
        public final long lab;
        public final String labName;
        public final long balance;
        public final long debt;
        public final long head;
        public final long creditLimit;

        public LabDebtDetails(long lab, String labName, long balance, long hasDebt, long head, long creditLimit) {
            this.lab = lab;
            this.labName = labName;
            this.balance = balance;
            this.debt = hasDebt;
            this.head = head;
            this.creditLimit = creditLimit;
        }
    }


}
