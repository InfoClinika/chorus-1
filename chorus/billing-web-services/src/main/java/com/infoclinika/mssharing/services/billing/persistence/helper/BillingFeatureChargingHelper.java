package com.infoclinika.mssharing.services.billing.persistence.helper;

import com.infoclinika.mssharing.model.internal.entity.payment.*;
import com.infoclinika.mssharing.model.internal.repository.ChargeableItemRepository;
import com.infoclinika.mssharing.model.internal.repository.LabPaymentAccountRepository;
import com.infoclinika.mssharing.model.internal.repository.LabRepository;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Date;

import static com.google.common.collect.FluentIterable.from;
import static java.lang.Math.abs;

/**
 * @author Herman Zamula
 */
@Component
public class BillingFeatureChargingHelper {
    private static final Logger LOG = Logger.getLogger(BillingFeatureChargingHelper.class);

    @Inject
    private LabPaymentAccountRepository labPaymentAccountRepository;
    @Inject
    private LabRepository labRepository;
    @Inject
    private ChargeableItemRepository chargeableItemRepository;
    @Inject
    private PaymentCalculationsHelper paymentCalculations;

    public LabPaymentAccount findOrCreate(long lab) {
        LabPaymentAccount resultAccount = labPaymentAccountRepository.findByLab(lab);
        if (resultAccount == null) {
            final LabPaymentAccount account = new LabPaymentAccount(labRepository.findOne(lab));
            account.setBillingData(
                    new AccountBillingData(from(chargeableItemRepository.findEnabledByDefault())
                            .transform(input -> new AccountChargeableItemData(true, input, account))
                            .toSet()));
            account.setAccountCreationDate(new Date());
            account.setCreditLimit(LabPaymentAccount.DEFAULT_CREDIT_LIMIT);
            resultAccount = labPaymentAccountRepository.save(account);
        }
        return resultAccount;
    }

    private void setChargeValues(long unscaledPrice, LabPaymentAccount account) {
        account.setStoreBalance(account.getStoreBalance() - unscaledPrice);
        if (account.getStoreBalance() < 0) {
            account.setPayByStore(abs(account.getStoreBalance()));
        } else {
            account.setCalculationDate(new Date());
        }
    }

    public synchronized ChargedInfo charge(long lab, long scaledFeaturePrice) {

        final LabPaymentAccount account = findOrCreate(lab);

        final long oldScaledValue = account.getScaledToPayValue();
        account.setScaledToPayValue(oldScaledValue + scaledFeaturePrice);

        final long unscaled = paymentCalculations.unscalePriceNotRound(account.getScaledToPayValue());

        if (unscaled > 0) {
            setChargeValues(unscaled, account);
            account.setScaledToPayValue(account.getScaledToPayValue() - paymentCalculations.scalePrice(unscaled));
        }
        LOG.debug("*** Account of lab {" + lab + "} charged. Balance: {" + account.getStoreBalance() + "}");

        labPaymentAccountRepository.save(account);

        return new ChargedInfo(account.getStoreBalance(), account.getScaledToPayValue());
    }

    public static class ChargedInfo {
        public final long balance;
        public final long scaledToPayValue;

        public ChargedInfo(long balance, long scaledToPayValue) {
            this.balance = balance;
            this.scaledToPayValue = scaledToPayValue;
        }
    }

}
