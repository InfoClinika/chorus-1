package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.platform.repository.QueryTemplates;

public abstract class ChorusQueries extends QueryTemplates {

    public static final String STORAGE_STATUS_ARCHIVING = "com.infoclinika.mssharing.model.internal.entity.restorable.StorageData$Status.ARCHIVING_REQUESTED";
    public static final String STORAGE_STATUS_ARCHIVED = "com.infoclinika.mssharing.model.internal.entity.restorable.StorageData$Status.ARCHIVED";
    public static final String STORAGE_STATUS_UNARCHIVED = "com.infoclinika.mssharing.model.internal.entity.restorable.StorageData$Status.UNARCHIVED";
    public static final String STORAGE_STATUS_UNARCHIVING = "com.infoclinika.mssharing.model.internal.entity.restorable.StorageData$Status.UNARCHIVING_REQUESTED";

    /* Search Experiments */
    public static final String FILTER_EXPERIMENTS_BY_QUERY =
            " (lower(e.name) like :query or lower(e.description) like :query)";
    public static final String ALL_AVAILABLE_EXPERIMENTS_WITH_QUERY =
            " select distinct e from ExperimentDashboardRecord e left join e.project p" +
                    " where e.deleted = 0 and " + FILTER_EXPERIMENTS_BY_QUERY +
                    " and (p.creator.id = :user " +
                    " or e.creator.id = :user " +
                    " or p.sharing.type = " + PUBLIC_PROJECT +
                    " or (p.sharing.type = " + SHARED_PROJECT + " and " + HAVE_ACCESS_TO_PROJECT + ")) ";


}
