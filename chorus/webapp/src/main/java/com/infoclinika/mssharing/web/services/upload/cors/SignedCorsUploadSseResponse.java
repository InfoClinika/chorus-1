package com.infoclinika.mssharing.web.services.upload.cors;

/**
 * @author Oleksii Tymchenko
 */
public class SignedCorsUploadSseResponse extends SignedCorsUploadResponse {
    public final boolean serverSideEncryption;

    public SignedCorsUploadSseResponse(String authorization, String host, String date, boolean serverSideEncryption) {
        super(authorization, host, date);
        this.serverSideEncryption = serverSideEncryption;
    }
}
