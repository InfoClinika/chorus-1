package com.infoclinika.mssharing.web.controller.v2.service;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.Principal;
import com.amazonaws.auth.policy.Resource;
import com.amazonaws.auth.policy.Statement;
import com.amazonaws.auth.policy.actions.S3Actions;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityResult;
import com.google.common.base.Throwables;
import com.infoclinika.mssharing.model.internal.entity.upload.FileDetails;
import com.infoclinika.mssharing.model.internal.entity.upload.UploadFileStatus;
import com.infoclinika.mssharing.model.internal.s3client.AWSConfigService;
import com.infoclinika.mssharing.web.controller.v2.service.S3FileCopier.CopyFileRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.infoclinika.mssharing.model.internal.s3client.AWSConfigService.DELIMITER;
import static java.util.stream.Collectors.toList;

/**
 * @author Vitalii Petkanych
 */
@Service
public class S3FileImportServiceImpl implements S3FileImportService {

    private static final String AWS_PROVIDER = "AWS";
    private static final Logger LOGGER = LoggerFactory.getLogger(S3FileImportServiceImpl.class);

    @Inject
    private AWSConfigService awsConfigService;
    @Inject
    private S3FileCopierImpl s3FileCopier;
    @Inject
    private TrackingUploadService trackingService;

    @Override
    public List<FileDetails> listFiles(
        String accessKeyId,
        String secretAccessKey,
        String url,
        boolean recursive,
        Predicate<String> filter
    ) {
        final AmazonS3URI uri = new AmazonS3URI(url);
        final List<S3ObjectSummary> files = StringUtils.isBlank(accessKeyId)
            ? list(awsConfigService.s3Client(uri.getBucket()), uri, recursive)
            : list(accessKeyId, secretAccessKey, url, recursive);
        return files
            .stream()
            .filter(s3obj -> filter.test(s3obj.getKey()))
            .map(s3obj -> new FileDetails(s3obj.getKey(), s3obj.getLastModified().getTime(), s3obj.getSize()))
            .collect(toList());
    }

    @Async
    @Override
    public void copyFiles(
        String accessKeyId,
        String secretAccessKey,
        String url,
        List<String> files,
        Function<String, String> dstKeyGenerator
    ) {
        final String srcBucket = new AmazonS3URI(url).getBucket();
        try {
            copyFilesJob(accessKeyId, secretAccessKey, srcBucket, files, dstKeyGenerator);
        } catch (Exception ex) {
            LOGGER.error("Couldn't copy files. Mark files as FAILED", ex);
            setFilesFailed(srcBucket, files);
            Throwables.propagate(ex);
        }
    }

    private List<S3ObjectSummary> list(String accessKeyId, String secretAccessKey, String url, boolean recursive) {
        final AmazonS3URI uri = new AmazonS3URI(url);
        final AWSCredentialsProvider credentialsProvider =
            awsConfigService.awsCredentialsProvider(accessKeyId, secretAccessKey);
        AmazonS3 s3 = awsConfigService.s3Client(credentialsProvider, uri.getBucket());

        return list(s3, uri, recursive);
    }

    private List<S3ObjectSummary> list(AmazonS3 s3, AmazonS3URI uri, boolean recursive) {
        ArrayList<S3ObjectSummary> objectSummaries = new ArrayList<>();

        final String prefix = uri.getKey();
        final ListObjectsV2Request listRequest = new ListObjectsV2Request()
            .withBucketName(uri.getBucket())
            .withPrefix(prefix);

        if (!recursive) {
            listRequest.withDelimiter(DELIMITER);
        }

        boolean allowFolderNameCorrection = !recursive;
        ListObjectsV2Result listObjects;
        do {
            listObjects = s3.listObjectsV2(listRequest);
            final List<String> commonPrefixes = listObjects.getCommonPrefixes();
            if (allowFolderNameCorrection
                && listObjects.getObjectSummaries().isEmpty()
                && filterByPrefix(commonPrefixes, prefix).count() == 1) {
                listRequest.withPrefix(filterByPrefix(commonPrefixes, prefix).findAny().get());
                listObjects = s3.listObjectsV2(listRequest);
                allowFolderNameCorrection = false;
            }
            objectSummaries.addAll(listObjects.getObjectSummaries());
            listRequest.setContinuationToken(listObjects.getNextContinuationToken());
        } while (listObjects.isTruncated());

        return objectSummaries;
    }

    private Stream<String> filterByPrefix(List<String> strings, String prefix) {
        return strings.stream().filter(s -> s.startsWith(prefix));
    }

    private void setFilesFailed(String srcBucket, List<String> files) {
        files.stream()
            .map(file -> trackingService.trackingId(srcBucket, file))
            .forEach(trackingId -> trackingService.updateFileStatus(trackingId, UploadFileStatus.FAILED));
    }

    private void copyFilesJob(
        String accessKeyId,
        String secretAccessKey,
        String srcBucket,
        List<String> files,
        Function<String, String> dstKeyGenerator
    ) {

        // user's s3 credentials provider
        final AWSCredentialsProvider srcCredentialsProvider =
            awsConfigService.awsCredentialsProvider(accessKeyId, secretAccessKey);
        // user's s3 client
        final AmazonS3 srcS3Client = awsConfigService.s3Client(srcCredentialsProvider, srcBucket);
        // chorus credentials provider
        final AWSCredentialsProvider dstCredentialsProvider = awsConfigService.awsCredentialsProvider();
        // set policy to user's bucket to allow chorus to read files
        srcS3Client.setBucketPolicy(srcBucket, getBucketPolicy(srcBucket, getArn(dstCredentialsProvider)));

        final String dstBucket = awsConfigService.getActiveBucket();
        final AmazonS3 dstS3Client = awsConfigService.s3Client(dstBucket);
        final int parallelThreads = awsConfigService.getParallelThreads();
        final int parallelFiles = Math.min(awsConfigService.getParallelFiles(), files.size());
        final ExecutorService filesExecutorService = Executors.newFixedThreadPool(parallelFiles);
        final List<? extends Future<?>> futures = files
            .stream()
            .map(file -> {
                final CopyFileRequest request = new CopyFileRequest(
                    dstS3Client,
                    srcBucket,
                    dstBucket,
                    file,
                    dstKeyGenerator.apply(file),
                    parallelThreads
                );
                return filesExecutorService.submit(() -> s3FileCopier.copyFile(request));
            })
            .collect(toList());

        futures.forEach(f -> {
            try {
                f.get();
            } catch (InterruptedException | ExecutionException ex) {
                LOGGER.warn("Error occurred during file copying.", ex);
            }
        });

        filesExecutorService.shutdown();
        srcS3Client.deleteBucketPolicy(srcBucket);
        srcS3Client.shutdown();
        dstS3Client.shutdown();
    }

    private String getArn(AWSCredentialsProvider credentialsProvider) {
        AWSSecurityTokenService sts = AWSSecurityTokenServiceClientBuilder.standard()
            .withCredentials(credentialsProvider)
            .withRegion(Regions.DEFAULT_REGION)
            .build();
        GetCallerIdentityResult identity = sts.getCallerIdentity(new GetCallerIdentityRequest());
        return identity.getArn();
    }

    private String getBucketPolicy(String bucketName, String arn) {
        return new Policy().withStatements(
            new Statement(Statement.Effect.Allow)
                .withPrincipals(new Principal(AWS_PROVIDER, arn, false))
                .withActions(S3Actions.AllS3Actions)
                .withResources(new Resource(
                    "arn:aws:s3:::" + bucketName + "/*")))
            .toJson();
    }

}
