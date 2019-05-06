package com.infoclinika.mssharing.platform.model.helper;

/**
 * @author Herman Zamula
 */
public interface CorsRequestSignerTemplate {

    /**
     * @param userId     actor that performs an action
     * @param objectName stored object key
     * @return Signed url
     * @see #signForSingleFileUploadRequest(long, String)
     */
    @Deprecated
    String signSingleFileUploadRequest(long userId, String objectName);

    /**
     * @return Signed url in {@code SignedRequest.host} property
     */
    SignedRequest signForSingleFileUploadRequest(long userId, String objectName);

    SignedRequest signInitialUploadRequest(long userId, String objectName);

    SignedRequest signUploadPartRequest(long userId, String objectName, long partNumber, String uploadId);

    SignedRequest signListPartsRequest(long userId, String objectName, String uploadId);

    SignedRequest signAbortUploadRequest(long userId, String objectName, String uploadId);

    SignedRequest signCompleteUploadRequest(long userId,
                                            String objectName,
                                            String uploadId,
                                            boolean addCharsetToContentType);

    boolean useServerSideEncryption();

    boolean useSessionToken();

    String getAmazonToken();

    class SignedRequest {
        public final String dateAsString;
        public final String host;
        public final String authorization;
        public final String amazonToken;

        public SignedRequest(String dateAsString, String host, String authorization, String amazonToken) {
            this.dateAsString = dateAsString;
            this.host = host;
            this.authorization = authorization;
            this.amazonToken = amazonToken;
        }
    }
}
