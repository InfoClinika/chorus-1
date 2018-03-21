/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.write;

import org.springframework.transaction.annotation.Transactional;


/**
 * Management of the fasta databases for protein ID Search, its creation, generating dataCubes with theirs sequences
 * <p>
 *
 * @author Andrii Loboda
 */
@Transactional
public interface ProteinDatabaseManagement {
    /**
     * Persists protein Id search database. This database could be available for everyone in case experiment param is null
     **/
    long createDatabase(long actor, String nameToDisplay, long proteinDBType, long sizeInBytes, boolean bPublic, boolean bReversed, ExperimentCategory category);

    long updateDatabaseDetails(long actor, long database, String databaseName, long databaseType);


    void deleteDatabase(long actor, long db);

    long getMaxDatabaseSize();

    void specifyProteinDatabaseContent(long userId, long proteinDb, String contentUrl);

    void markDatabasesInProgress(Iterable<Long> dbs);

    void updateDatabaseSharing(long actor, boolean bPublic, long databaseId);

    DuplicateResponse duplicateDatabase(long actor, long database);

    class DuplicateResponse{
        public final Long id;
        public final String errorMessage;

        public DuplicateResponse(Long id, String errorMessage) {
            this.id = id;
            this.errorMessage = errorMessage;
        }
    }
}
