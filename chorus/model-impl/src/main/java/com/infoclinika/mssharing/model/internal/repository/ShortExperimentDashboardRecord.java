package com.infoclinika.mssharing.model.internal.repository;

import java.util.Date;

/**
 * To be used in REST API.
 *
 * @author Oleksii Tymchenko
 */
public class ShortExperimentDashboardRecord {
    public final long id;
    public final String name;
    public final String creatorEmail;
    public final long owner;
    public final Date modified;
    public final int analysisRunCount;


    public ShortExperimentDashboardRecord(
        long id,
        String name,
        String creatorEmail,
        long owner,
        Date modified,
        int analysisRunCount
    ) {
        this.id = id;
        this.name = name;
        this.creatorEmail = creatorEmail;
        this.owner = owner;
        this.modified = modified;
        this.analysisRunCount = analysisRunCount;
    }
}
