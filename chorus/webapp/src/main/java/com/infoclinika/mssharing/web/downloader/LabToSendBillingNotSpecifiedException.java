package com.infoclinika.mssharing.web.downloader;


import com.infoclinika.mssharing.model.helper.items.ChorusExperimentDownloadData;

/**
 * @author Herman Zamula
 */
public class LabToSendBillingNotSpecifiedException extends RuntimeException {

    public final ChorusExperimentDownloadData experimentDownloadData;

    public LabToSendBillingNotSpecifiedException(String message, ChorusExperimentDownloadData experimentDownloadData) {
        super(message);
        this.experimentDownloadData = experimentDownloadData;
    }
}
