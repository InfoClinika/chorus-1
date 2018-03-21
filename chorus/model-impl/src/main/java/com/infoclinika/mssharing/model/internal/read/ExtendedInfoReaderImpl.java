/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.internal.read;

import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.entity.Instrument;
import com.infoclinika.mssharing.model.internal.repository.InstrumentRepository;
import com.infoclinika.mssharing.model.read.ExtendedInfoReader;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import static com.infoclinika.mssharing.platform.model.impl.ValidatorPreconditions.checkPresence;
import static java.lang.String.format;

/**
 * @author Stanislav Kurilin
 */
@Service
public class ExtendedInfoReaderImpl implements ExtendedInfoReader {

    @Inject
    private RuleValidator ruleValidator;
    @Inject
    private InstrumentRepository instrumentRepository;
    @Inject
    private Transformers transformers;

    @Override
    public DictionaryItem instrumentModel(long actor, long instrument) {
        final Instrument entity = checkPresence(instrumentRepository.findOne(instrument));
        if (!ruleValidator.isUserCanReadInstrumentPredicate(actor).apply(entity)) {
            throw new AccessDenied(format("User has no permissions to read instrument vendor. User: [%d], Instrument: [%d]", actor, instrument));
        }
        return transformers.dictionaryItemTransformer().apply(entity.getModel());
    }


}
