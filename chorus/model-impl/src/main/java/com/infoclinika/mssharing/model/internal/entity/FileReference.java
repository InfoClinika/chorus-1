package com.infoclinika.mssharing.model.internal.entity;


import com.infoclinika.analysis.storage.cloud.CloudStorageItemReference;

import javax.persistence.Basic;
import javax.persistence.Embeddable;

/**
 * @author Pavel Kaplin
 */
@Embeddable
public class FileReference {

    @Basic(optional = false)
    private String bucket;

    @Basic(optional = false)
    private String key;

    public FileReference() {
    }

    public FileReference(CloudStorageItemReference cloudItemReference) {
        this.bucket = cloudItemReference.getBucket();
        this.key = cloudItemReference.getKey();
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public CloudStorageItemReference toCloudReference() {
        return new CloudStorageItemReference(bucket, key);
    }
}
