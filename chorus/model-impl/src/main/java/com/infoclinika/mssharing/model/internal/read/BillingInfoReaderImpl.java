package com.infoclinika.mssharing.model.internal.read;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.model.internal.entity.payment.AccountChargeableItemData;
import com.infoclinika.mssharing.model.internal.entity.payment.LabPaymentAccount;
import com.infoclinika.mssharing.model.internal.helper.billing.DatabaseBillingPropertiesProvider;
import com.infoclinika.mssharing.model.internal.repository.LabPaymentAccountRepository;
import com.infoclinika.mssharing.model.read.BillingInfoReader;
import com.infoclinika.mssharing.services.billing.rest.api.model.LabAccountFeatureInfo;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.FluentIterable.from;
import static com.infoclinika.mssharing.model.internal.entity.payment.BillingProperty.BillingPropertyName;
import static com.infoclinika.mssharing.model.internal.entity.payment.BillingProperty.BillingPropertyName.valueOf;
import static com.infoclinika.mssharing.model.internal.entity.payment.BillingProperty.BillingPropertyName.values;

/**
 * @author Herman Zamula
 */
@Service
public class BillingInfoReaderImpl implements BillingInfoReader {

    @Inject
    private LabPaymentAccountRepository labPaymentAccountRepository;
    @Inject
    private DatabaseBillingPropertiesProvider databaseBillingPropertiesProvider;

    @Override
    public ImmutableSet<LabDebtDetails> readBillingInfo(long actor) {
        return from(labPaymentAccountRepository.findByLabHeadId(actor))
            .transform(new Function<LabPaymentAccount, LabDebtDetails>() {
                @Override
                public LabDebtDetails apply(LabPaymentAccount input) {
                    final long storeBalance = input.getStoreBalance();
                    return new LabDebtDetails(input.getLab().getId(),
                        input.getLab().getName(), storeBalance, storeBalance < 0 ? Math.abs(storeBalance) : 0,
                        input.getLab().getHead().getId(), input.getCreditLimit()
                    );
                }
            }).toSet();
    }

    @Override
    public Map<String, String> readBillingProperties(long actor) {
        return Arrays.asList(values())
            .stream()
            .map(BillingPropertyName::name)
            .collect(
                Collectors.toMap(
                    propName -> propName,
                    propName -> databaseBillingPropertiesProvider.getPropValue(valueOf(
                        propName))
                )
            );
    }

    @Override
    public Set<LabAccountFeatureInfo> readLabAccountFeatures(long lab) {
        final LabPaymentAccount account = labPaymentAccountRepository.findByLab(lab);
        final Set<AccountChargeableItemData> featuresData = account.getBillingData().getFeaturesData();

        return featuresData
            .stream()
            .map(f -> new LabAccountFeatureInfo(
                f.getChargeableItem().getFeature().name(),
                f.isActive(),
                f.getAccount().getId(),
                f.isAutoProlongate(),
                f.getQuantity()
            ))
            .collect(Collectors.toSet());
    }
}
