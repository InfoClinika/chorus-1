package com.infoclinika.mssharing.web.controller.v2.service;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.infoclinika.mssharing.model.internal.s3client.AWSConfigService;
import com.infoclinika.mssharing.platform.fileserver.StoredObjectPathsTemplate;
import com.infoclinika.mssharing.web.controller.v2.util.FileUtil;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.function.Function;

import static com.amazonaws.services.s3.Headers.S3_CANNED_ACL;

/**
 * @author Vitalii Petlanych
 */
@Service
public class ActiveBucketService {

    @Inject
    private AWSConfigService awsConfigService;

    @Inject
    private StoredObjectPathsTemplate storedObjectPaths;

    public Function<String, String> importKeyGenerator(long userId, long instrumentId) {
        return srcKey -> storedObjectPaths.rawFilePath(userId, instrumentId, FileUtil.extractName(srcKey)).getPath();
    }

    ObjectMetadata createCommonMetadata() {
        final ObjectMetadata metadata = new ObjectMetadata();
        metadata.setHeader(S3_CANNED_ACL, "bucket-owner-full-control");
        if (awsConfigService.isActiveBucketEncrypted()) {
            metadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
        }
        return metadata;
    }

}
