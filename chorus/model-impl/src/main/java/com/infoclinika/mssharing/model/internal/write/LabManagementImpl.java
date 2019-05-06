/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika,
 * Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use,
 * duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.internal.write;

import com.google.common.base.Function;
import com.infoclinika.mssharing.model.internal.entity.Lab;
import com.infoclinika.mssharing.model.internal.entity.payment.AccountBillingData;
import com.infoclinika.mssharing.model.internal.entity.payment.AccountChargeableItemData;
import com.infoclinika.mssharing.model.internal.entity.payment.ChargeableItem;
import com.infoclinika.mssharing.model.internal.entity.payment.LabPaymentAccount;
import com.infoclinika.mssharing.model.internal.repository.ChargeableItemRepository;
import com.infoclinika.mssharing.model.internal.repository.LabPaymentAccountRepository;
import com.infoclinika.mssharing.model.write.LabManagement;
import com.infoclinika.mssharing.platform.model.impl.write.DefaultLabManagement;
import com.infoclinika.mssharing.platform.model.write.LabManagementTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Date;

import static com.google.common.collect.FluentIterable.from;

/**
 * @author Stanislav Kurilin
 */
@Service("labManagement")
@Transactional
public class LabManagementImpl extends DefaultLabManagement<Lab, LabManagementTemplate.LabInfoTemplate>
    implements LabManagement {

    @Inject
    private LabPaymentAccountRepository paymentAccountRepository;

    @Inject
    private ChargeableItemRepository chargeableItemRepository;


    @Override
    protected Lab afterCreateLab(Long actor, Lab lab, LabInfoTemplate labInfoTemplate) {
        final LabPaymentAccount entity = new LabPaymentAccount(lab);
        final Iterable<ChargeableItem> chargeableItems = chargeableItemRepository.findEnabledByDefault();
        entity.setBillingData(new AccountBillingData(from(chargeableItems)
            .transform(new Function<ChargeableItem, AccountChargeableItemData>() {
                @Override
                public AccountChargeableItemData apply(ChargeableItem input) {
                    return new AccountChargeableItemData(true, input, entity);
                }
            })
            .toSet()));
        entity.setAccountCreationDate(new Date());
        entity.setCreditLimit(LabPaymentAccount.DEFAULT_CREDIT_LIMIT);

        paymentAccountRepository.save(entity);
        return lab;
    }

}
