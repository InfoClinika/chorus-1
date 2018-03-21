package com.infoclinika.mssharing.dto.response;

/**
 * author Ruslan Duboveckij
 */
public class UploadConfigDTO {

    private String amazonKey;
    private String amazonSecret;
    private String activeBucket;

    public UploadConfigDTO(){}

    public UploadConfigDTO(String amazonKey,
                           String amazonSecret,
                           String activeBucket) {
        this.amazonKey = amazonKey;
        this.amazonSecret = amazonSecret;
        this.activeBucket = activeBucket;
    }

    public String getAmazonKey() {
        return amazonKey;
    }

    public void setAmazonKey(String amazonKey) {
        this.amazonKey = amazonKey;
    }

    public String getAmazonSecret() {
        return amazonSecret;
    }

    public void setAmazonSecret(String amazonSecret) {
        this.amazonSecret = amazonSecret;
    }

    public String getActiveBucket() {
        return activeBucket;
    }

    public void setActiveBucket(String activeBucket) {
        this.activeBucket = activeBucket;
    }
}
