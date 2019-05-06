package com.infoclinika.mssharing.dto.response;

/**
 * author Ruslan Duboveckij
 */
public class UploadConfigDTO {

    private boolean useRoles;
    private String amazonKey;
    private String amazonSecret;
    private String sessionToken;
    private String activeBucket;

    public UploadConfigDTO() {
    }

    public UploadConfigDTO(boolean useRoles, String amazonKey, String amazonSecret, String sessionToken,
                           String activeBucket) {
        this.useRoles = useRoles;
        this.amazonKey = amazonKey;
        this.amazonSecret = amazonSecret;
        this.sessionToken = sessionToken;
        this.activeBucket = activeBucket;
    }

    public UploadConfigDTO(String amazonKey, String amazonSecret, String rawFilesBucket) {
        this(false, amazonKey, amazonSecret, "", rawFilesBucket);
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

    public boolean isUseRoles() {
        return useRoles;
    }

    public void setUseRoles(boolean useRoles) {
        this.useRoles = useRoles;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
}
