package com.infoclinika.mssharing.model.internal.helper.billing;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.model.helper.BillingFeaturesHelper;
import com.infoclinika.mssharing.model.internal.entity.payment.AccountBillingData;
import com.infoclinika.mssharing.model.internal.entity.payment.AccountChargeableItemData;
import com.infoclinika.mssharing.model.internal.entity.payment.ChargeableItem;
import com.infoclinika.mssharing.model.internal.entity.payment.LabPaymentAccount;
import com.infoclinika.mssharing.model.features.ApplicationFeature;
import com.infoclinika.mssharing.model.internal.repository.*;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeature;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Date;
import java.util.Set;

import static com.google.common.base.Functions.compose;
import static com.google.common.collect.FluentIterable.from;
import static com.infoclinika.mssharing.model.internal.entity.Feature.FeatureState.DISABLED;
import static com.infoclinika.mssharing.model.internal.read.Transformers.BILLING_FEATURE_TRANSFORMER;
import static com.infoclinika.mssharing.model.internal.read.Transformers.CHARGEABLE_ITEM_FROM_ACCOUNT_TRANSFORMER;

/**
 * @author Herman Zamula
 */
@Service
public class BillingFeatureHelperImpl implements BillingFeaturesHelper {

    private final Predicate<AccountChargeableItemData> featureAvailable = new Predicate<AccountChargeableItemData>() {
        @Override
        public boolean apply(AccountChargeableItemData input) {
            return input.isActive();
        }
    };
    @Inject
    private FeaturesRepository featuresRepository;
    @Inject
    private LabPaymentAccountRepository labPaymentAccountRepository;
    @Inject
    private LabRepository labRepository;
    @Inject
    private ChargeableItemRepository chargeableItemRepository;

    @Override
    public boolean isFeatureEnabled(long lab, final BillingFeature feature) {
        //If billing feature DISABLED, all application features (eg. file upload, translation etc.) become enabled
        if (featuresRepository.get().get(ApplicationFeature.BILLING.getFeatureName()).getEnabledState().equals(DISABLED)) {
            return true;
        }
        final Set<AccountChargeableItemData> featuresData = findOrCreateAndGetFeatures(lab);
        return from(featuresData)
                .filter(featureAvailable)
                .transform(compose(BILLING_FEATURE_TRANSFORMER, CHARGEABLE_ITEM_FROM_ACCOUNT_TRANSFORMER))
                .anyMatch(new Predicate<BillingFeature>() {
                    @Override
                    public boolean apply(BillingFeature input) {
                        return input.equals(feature);
                    }
                });
    }

    @Override
    public ImmutableSet<BillingFeature> enabledBillingFeatures(long lab) {
        //If billing feature DISABLED, all application features (eg. file upload, translation etc.) become enabled
        if (featuresRepository.get().get(ApplicationFeature.BILLING.getFeatureName()).getEnabledState().equals(DISABLED)) {
            return ImmutableSet.copyOf(BillingFeature.values());
        }
        final Set<AccountChargeableItemData> featuresData = findOrCreateAndGetFeatures(lab);
        return from(featuresData)
                .filter(featureAvailable)
                .transform(compose(BILLING_FEATURE_TRANSFORMER, CHARGEABLE_ITEM_FROM_ACCOUNT_TRANSFORMER))
                .toSet();
    }
    private Set<AccountChargeableItemData> findOrCreateAndGetFeatures(long lab) {
        final Optional<Long> accountId = Optional.fromNullable(labPaymentAccountRepository.accountIdForLab(lab));
        if (!accountId.isPresent()) {
            final LabPaymentAccount account = new LabPaymentAccount(labRepository.findOne(lab));
            account.setBillingData(new AccountBillingData(from(chargeableItemRepository.findEnabledByDefault())
                    .transform(new Function<ChargeableItem, AccountChargeableItemData>() {
                        @Override
                        public AccountChargeableItemData apply(ChargeableItem input) {
                            return new AccountChargeableItemData(true, input, account);
                        }
                    })
                    .toSet()));
            account.setAccountCreationDate(new Date());
            account.setCreditLimit(LabPaymentAccount.DEFAULT_CREDIT_LIMIT);
            final LabPaymentAccount result = labPaymentAccountRepository.save(account);
            return (result.getBillingData().getFeaturesData());
        } else {
            return labPaymentAccountRepository.findFeaturesDataByLab(lab);
        }
    }
}
