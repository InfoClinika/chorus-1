package com.infoclinika.mssharing.model.internal.write;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.infoclinika.mssharing.model.write.LogUploader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URL;
import java.util.Calendar;

/**
 * @author Elena Kurilina
 */

@Service
public class LogUploaderImpl implements LogUploader {

    public static final String LOGS = "logs/";
    @Value("${amazon.key}")
    private String accessKey;
    @Value("${amazon.secret}")
    private String secretKey;
    @Value("${amazon.active.bucket}")
    private String bucket;
    @Value("${amazon.expiration.months}")
    private String months;

    @Override
    public URL uploadFile(File log) {
        TransferManager transferManager = new TransferManager(new BasicAWSCredentials(accessKey, secretKey));
        transferManager.upload(bucket, LOGS + log.getName(), log);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, Integer.valueOf(months));
        return transferManager.getAmazonS3Client().generatePresignedUrl(bucket, LOGS + log.getName(), calendar.getTime());

    }


}
