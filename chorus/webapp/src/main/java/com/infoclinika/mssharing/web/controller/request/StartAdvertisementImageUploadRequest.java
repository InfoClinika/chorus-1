package com.infoclinika.mssharing.web.controller.request;

import java.util.Date;

/**
 * @author Alexander Orlov
 */
public class StartAdvertisementImageUploadRequest {

    public long id;
    public String title;
    public Date startDate;
    public Date endDate;
    public String redirectLink;
    public Date currentDate;
    public String filename;
    public long sizeInBytes;
    public boolean isEnabled;

    @Override
    public String toString() {
        return "StartAdvertisementImageUploadRequest{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", redirectLink='" + redirectLink + '\'' +
                ", currentDate=" + currentDate +
                ", filename='" + filename + '\'' +
                ", sizeInBytes=" + sizeInBytes +
                ", isEnabled=" + isEnabled +
                '}';
    }
}
