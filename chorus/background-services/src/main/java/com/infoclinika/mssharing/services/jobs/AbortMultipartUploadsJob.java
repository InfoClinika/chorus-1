package com.infoclinika.mssharing.services.jobs;

import com.infoclinika.mssharing.model.internal.helper.AbortMultipartHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Date;

/**
 * @author timofey.kasyanov
 *         date: 27.02.14.
 */
@Service
public class AbortMultipartUploadsJob {

    @Inject
    private AbortMultipartHelper abortMultipartHelper;

    @Scheduled(fixedRate = 7 * 24 * 60 * 60 * 1000)
    public void abortMultipartUploads(){

        final long week = 7 * 24 * 60 * 60 * 1000;
        final long currentTime = System.currentTimeMillis();
        final Date weekAgo = new Date(currentTime - week);

        abortMultipartHelper.abortMultipartUploads(weekAgo);

    }

}
