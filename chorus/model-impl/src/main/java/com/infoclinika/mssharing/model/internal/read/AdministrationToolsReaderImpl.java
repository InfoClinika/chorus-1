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
package com.infoclinika.mssharing.model.internal.read;

import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.model.internal.repository.NewsRepository;
import com.infoclinika.mssharing.model.read.AdministrationToolsReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import static com.google.common.collect.FluentIterable.from;
import static com.infoclinika.mssharing.model.internal.read.Transformers.NEWS_BY_DATE;
import static com.infoclinika.mssharing.model.internal.read.Transformers.TO_NEWS_LINE;

/**
 * @author Stanislav Kurilin
 */
@Service
public class AdministrationToolsReaderImpl implements AdministrationToolsReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdministrationToolsReaderImpl.class);

    @Inject
    private NewsRepository newsRepository;

    @Override
    public ImmutableSortedSet<NewsLine> readNewsItems(long actor) {
        return from(newsRepository.findAll())
            .transform(TO_NEWS_LINE)
            .toSortedSet(NEWS_BY_DATE);
    }
}
