package com.infoclinika.mssharing.web.controller.request;

/**
 * @author Alexander Orlov
 */
public class CompleteAdvertisementImageUploadRequest {

    public long advertisementId;
    public String contentUrl;

    @Override
    public String toString() {
        return "CompleteAdvertisementImageUploadRequest{" +
            "advertisementId=" + advertisementId +
            ", contentUrl='" + contentUrl + '\'' +
            '}';
    }
}
