package com.infoclinika.mssharing.web.services.upload.cors;

/**
 * @author vladimir.moiseiev.
 */

public class SignedCorsUploadSeeTokenResponse extends SignedCorsUploadSseResponse {
    public final boolean useAmazonToken;
    public final String amazonToken;

    public SignedCorsUploadSeeTokenResponse(
        String authorization, String host, String date, boolean serverSideEncryption,
        boolean useAmazonToken, String amazonToken
    ) {
        super(authorization, host, date, serverSideEncryption);
        this.useAmazonToken = useAmazonToken;
        this.amazonToken = amazonToken;
    }
}
