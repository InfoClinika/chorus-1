/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.internal.write;

import com.google.common.base.Strings;
import com.infoclinika.analysis.storage.cloud.CloudStorageFactory;
import com.infoclinika.analysis.storage.cloud.CloudStorageItemReference;
import com.infoclinika.analysis.storage.cloud.CloudStorageService;
import com.infoclinika.common.io.FileOperations;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.entity.ProteinDatabase;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.entity.Util;
import com.infoclinika.mssharing.model.internal.repository.ApplicationSettingsRepository;
import com.infoclinika.mssharing.model.internal.repository.ProteinDatabaseRepository;
import com.infoclinika.mssharing.model.internal.repository.UserRepository;
import com.infoclinika.mssharing.model.write.ExperimentCategory;
import com.infoclinika.mssharing.model.write.ProteinDatabaseManagement;
import com.infoclinika.mssharing.platform.entity.Species;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.repository.SpeciesRepositoryTemplate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.infoclinika.mssharing.model.internal.entity.ProteinDatabase.ProteinDatabaseStatus.IN_PROGRESS;
import static com.infoclinika.mssharing.model.internal.entity.ProteinDatabase.ProteinDatabaseStatus.NOT_PERSISTED;

/**
 * @author Oleksii Tymchenko
 */
@Service
public class ProteinDatabaseManagementImpl implements ProteinDatabaseManagement {

    private static final Logger LOG = Logger.getLogger(ProteinDatabaseManagementImpl.class);
    private static final CloudStorageService CLOUD = CloudStorageFactory.service();
    @Inject
    private RuleValidator ruleValidator;
    @Inject
    private Provider<Date> current;
    @Inject
    private ProteinDatabaseRepository proteinDatabaseRepository;
    @Inject
    private ApplicationSettingsRepository applicationSettingsRepository;
    @Inject
    private SpeciesRepositoryTemplate<Species> specieRepository;
    @Inject
    private UserRepository userRepository;
    @Value("${amazon.active.bucket}")
    private String targetBucket;
    @Value("${protein.dbs.target.folder}")
    private String proteinDatabasesPrefix;

    @Override
    public long createDatabase(long actor, String name, long specie, long sizeInBytes, boolean bPublic, boolean bReversed, ExperimentCategory category) {
        final Species specieEntity = specieRepository.findOne(specie);
        User user = Util.USER_FROM_ID.apply(actor);
        final ProteinDatabase db = new ProteinDatabase(name, specieEntity, current.get(), sizeInBytes, bPublic, user, bReversed, category);
        return proteinDatabaseRepository.save(db).getId();
    }

    @Override
    public long updateDatabaseDetails(long actor, long databaseId, String databaseName, long databaseType) {
        if (!ruleValidator.canModifyProteinDatabase(actor, databaseId)) {
            throw new AccessDenied("User cannot modify the Protein Search Database. User ID = " + actor + ". Database ID= " + databaseId);
        }
        checkArgument(!Strings.isNullOrEmpty(databaseName));
        final Species specie = checkNotNull(specieRepository.findOne(databaseType));
        final ProteinDatabase toUpdate = proteinDatabaseRepository.findOne(databaseId);
        checkPermissionToUpdateDatabase(databaseId, databaseName, specie, toUpdate);
        toUpdate.setName(databaseName);
        toUpdate.setSpecie(specie);
        proteinDatabaseRepository.save(toUpdate);
        return toUpdate.getId();
    }

    private void checkPermissionToUpdateDatabase(long databaseId, String databaseName, Species specie, ProteinDatabase toUpdate) {
        if (!proteinDatabaseRepository.hasUniqueName(databaseId, databaseName)) {
            throw new IllegalArgumentException("Protein Search Database name is not unique");
        }
    }

    @Override
    public void deleteDatabase(long actor, long db) {

        if (!ruleValidator.canModifyProteinDatabase(actor, db)) {
            throw new AccessDenied("User cannot delete the Protein Search Database. User ID = " + actor + ". Database ID= " + db);
        }
        //TODO:2015-04-01:andrii.loboda: delete all protein descriptions - resolve it until 23 August 2015
        final ProteinDatabase toDelete = proteinDatabaseRepository.findOne(db);

        proteinDatabaseRepository.delete(toDelete);

        try {
            CLOUD.deleteFromCloud(new CloudStorageItemReference(targetBucket, toDelete.getContentId()));
        } catch (RuntimeException e) {
            LOG.error("File is not deleted on S3 by key: " + toDelete.getContentId(), e);
        }
    }

    @Override
    public long getMaxDatabaseSize() {
        return applicationSettingsRepository.findProteinDBMaxSize().value;
    }

    @Override
    public void specifyProteinDatabaseContent(long actor, long proteinDbId, String contentUrl) {
        if (!ruleValidator.canModifyProteinDatabase(actor, proteinDbId)) {
            throw new AccessDenied("User cannot edit the protein database for the experiment. User ID = " + actor);
        }
        final ProteinDatabase db = proteinDatabaseRepository.findOne(proteinDbId);
        db.setContentId(contentUrl);
        proteinDatabaseRepository.save(db);
    }


    private File downloadProteinDatabase(ProteinDatabase db) {
        final CloudStorageItemReference referenceToDB = new CloudStorageItemReference(targetBucket, db.getContentId());
        final File downloadedDB = FileOperations.createTempFile("db" + db.getName() + System.currentTimeMillis());
        LOG.info("Downloading the DB file  from " + referenceToDB + ", to: " + downloadedDB);
        CLOUD.readFromCloud(referenceToDB, downloadedDB);
        LOG.info("Download complete for file: " + referenceToDB);
        return downloadedDB;
    }

    @Override
    public void markDatabasesInProgress(Iterable<Long> dbs) {
        for (Long dbId : dbs) {
            final ProteinDatabase db = proteinDatabaseRepository.findOne(dbId);
            db.setStatus(IN_PROGRESS);
            proteinDatabaseRepository.save(db);// mark to avoid several re-persists for one database
        }
    }

    @Override
    public void updateDatabaseSharing(long actor, boolean bPublic, long databaseId) {
        if (!ruleValidator.canModifyProteinDatabase(actor, databaseId)) {
            throw new AccessDenied("User cannot update Protein Search Database sharing. User ID = " + actor + ". Database ID= " + databaseId);
        }
        final ProteinDatabase database = proteinDatabaseRepository.findOne(databaseId);
        database.setbPublic(bPublic);
        proteinDatabaseRepository.save(database);
    }

    @Override
    public DuplicateResponse duplicateDatabase(long actor, long database) {
        if (!ruleValidator.canReadProteinDatabase(actor, database)) {
            throw new AccessDenied("User cannot duplicate the Protein Search Database. User ID = " + actor + ". Database ID= " + database);
        }

        final User user = userRepository.findOne(actor);
        final ProteinDatabase origin = proteinDatabaseRepository.findOne(database);

        final Date creationDate = new Date();
        final ProteinDatabase newDatabase = new ProteinDatabase(duplicateName(origin.getName(), creationDate),
                origin.getSpecie(),
                origin.getUploadDate(),
                origin.getSizeInBytes(),
                false,
                user,
                origin.isReversed(),
                origin.getCategory());

        final String newContentId = origin.getContentId() != null ? duplicateName(origin.getContentId(), creationDate) : null;
        newDatabase.setStatus(NOT_PERSISTED);
        newDatabase.setContentId(newContentId);

        if (!CLOUD.existsAtCloud(new CloudStorageItemReference(targetBucket, origin.getContentId()))) {
            LOG.warn("Error. File with id " + origin.getContentId() + " not exists.");
            return new DuplicateResponse(null, "Error: File does not exist.");
        }

        try {
            CLOUD.copy(new CloudStorageItemReference(targetBucket, origin.getContentId()),
                    new CloudStorageItemReference(targetBucket, newContentId));
        } catch (Exception e) {
            LOG.warn("Error. Copy file with id " + origin.getContentId(), e);
            return new DuplicateResponse(null, "Database duplication failed.");
        }
        proteinDatabaseRepository.save(newDatabase);

        return new DuplicateResponse(newDatabase.getId(), null);
    }

    private String duplicateName(String name, Date creationDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd HH_mm_ss");
        return String.format("Copy of %s %s", name, dateFormat.format(creationDate));
    }

}
