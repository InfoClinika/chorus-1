package com.infoclinika.mssharing.dto.response;

/**
 * author: Ruslan Duboveckij
 */
public class AuthenticateDTO {

    private String restToken;
    private String userEmail;
    private UploadConfigDTO uploadConfig;

    public AuthenticateDTO(){}

    public AuthenticateDTO(String restToken, String email, UploadConfigDTO uploadConfig) {
        this.restToken = restToken;
        this.userEmail = email;
        this.uploadConfig = uploadConfig;
    }

    public String getRestToken() {
        return restToken;
    }

    public void setRestToken(String restToken) {
        this.restToken = restToken;
    }

    public UploadConfigDTO getUploadConfig() {
        return uploadConfig;
    }

    public void setUploadConfig(UploadConfigDTO uploadConfig) {
        this.uploadConfig = uploadConfig;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
