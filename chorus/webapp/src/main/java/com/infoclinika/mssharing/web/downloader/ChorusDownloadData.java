package com.infoclinika.mssharing.web.downloader;

import com.infoclinika.mssharing.platform.web.downloader.SingleFileDownloadHelperTemplate;

/**
 * @author Herman Zamula
 */
public class ChorusDownloadData extends SingleFileDownloadHelperTemplate.DownloadData {

    public final Long lab;

    public ChorusDownloadData(long file, Long lab) {
        super(file);
        this.lab = lab;
    }
}
