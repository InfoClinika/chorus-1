package com.infoclinika.mssharing.propertiesprovider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ChorusPropertiesProvider extends AbstractPropertiesProvider {

    @Value("${base.url}")
    private String baseUrl;

    @Value("${chorus.sso:sso-disabled}")
    private String ssoProfile;

    @Value("${autoimporter.url}")
    private String autoimporterUrl;

    @Value("${desktop.uploader.mac.url}")
    private String desktopUploaderMacUrl;

    @Value("${desktop.uploader.win.url}")
    private String desktopUploaderWinUrl;

    @Value("${mascot.search.url}")
    private String mascotSearchUrl;

    @Value("${amazon.s3.workers.storage}")
    private String workersStorage;

    @Value("${max.login.attempts:3}")
    private int maxLoginAttempts;

    @Value("${private.installation}")
    private boolean privateInstallation;

    @Value("${project.attachments.target.folder}")
    private String projectAttachmentsTargetFolder;

    @Value("${project.title}")
    private String projectTitle;

    @Value("${protein.dbs.target.folder}")
    private String proteinDatabasesTargetFolder;

    @Value("${raw.files.target.folder}")
    private String rawFilesTargetFolder;

    @Value("${raw.files.temp.folder}")
    private String rawFilesTempFolder;

    @Value("${raw.files.temp.ftp.folder}")
    private String rawFilesTempFtpFolder;

    @Value("${session.timeout:86400}")
    private int sessionTimeout;

    @Value("${springfox.documentation.swagger.v2.path}")
    private String swaggerV2ApiDocUrl;

    @Value("${swagger.enabled}")
    private boolean swaggerEnabled;

    @Value("${user.account.removal.delay:86400000}")
    private long userAccountRemovalDelay;

    @Value("${workflow.run.delete_run_results_from_cloud:false}")
    private boolean deleteRunResultsFromCloud;

    @Value("${advertisement.images.target.folder}")
    private String advertisementImagesTargetFolder;

    @Value("${client.credentialsExpirationDuration:1800000}")
    private long credentialsExpirationDuration;

    @Value("${database.data.admin.email}")
    private String databaseAdminEmail;

    @Value("${database.data.admin.password}")
    private String databaseAdminPassword;

    @Value("${database.data.create}")
    private boolean databaseCreateDemoData;

    @Value("${email.verification.crypto.key}")
    private String emailVerificationCryptoKey;

    @Value("${experiment.annotation.target.folder}")
    private String experimentsAnnotationsTargetFolder;

    @Value("${experiment.attachment.target.folder}")
    private String experimentsAttachmentsTargetFolder;

    @Value("${forum.enabled}")
    private boolean forumEnabled;

    @Value("${forum.url}")
    private String forumUrl;

    @Value("${hdf5.files.folder}")
    private String hdf5FilesFolder;

    @Value("${aggregate.statistics.threads:200}")
    private int aggregateStatisticsNumberOfThreads;

    //For storage usage statistics only
    @Value("${first.day.of.month:1}")
    private int firstDayOfMonth;

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getSsoProfile() {
        return ssoProfile;
    }

    public String getAutoimporterUrl() {
        return autoimporterUrl;
    }

    public String getDesktopUploaderMacUrl() {
        return desktopUploaderMacUrl;
    }

    public String getDesktopUploaderWinUrl() {
        return desktopUploaderWinUrl;
    }

    public String getMascotSearchUrl() {
        return mascotSearchUrl;
    }

    public String getWorkersStorage() {
        return workersStorage;
    }

    public int getMaxLoginAttempts() {
        return maxLoginAttempts;
    }

    public boolean isPrivateInstallation() {
        return privateInstallation;
    }

    public String getProjectAttachmentsTargetFolder() {
        return projectAttachmentsTargetFolder;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public String getProteinDatabasesTargetFolder() {
        return proteinDatabasesTargetFolder;
    }

    public String getRawFilesTargetFolder() {
        return rawFilesTargetFolder;
    }

    public String getRawFilesTempFolder() {
        return rawFilesTempFolder;
    }

    public String getRawFilesTempFtpFolder() {
        return rawFilesTempFtpFolder;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public String getSwaggerV2ApiDocUrl() {
        return swaggerV2ApiDocUrl;
    }

    public boolean isSwaggerEnabled() {
        return swaggerEnabled;
    }

    public long getUserAccountRemovalDelay() {
        return userAccountRemovalDelay;
    }

    public boolean isDeleteRunResultsFromCloud() {
        return deleteRunResultsFromCloud;
    }

    public String getAdvertisementImagesTargetFolder() {
        return advertisementImagesTargetFolder;
    }

    public long getCredentialsExpirationDuration() {
        return credentialsExpirationDuration;
    }

    public String getDatabaseAdminEmail() {
        return databaseAdminEmail;
    }

    public String getDatabaseAdminPassword() {
        return databaseAdminPassword;
    }

    public boolean isDatabaseCreateDemoData() {
        return databaseCreateDemoData;
    }

    public String getEmailVerificationCryptoKey() {
        return emailVerificationCryptoKey;
    }

    public String getExperimentsAnnotationsTargetFolder() {
        return experimentsAnnotationsTargetFolder;
    }

    public String getExperimentsAttachmentsTargetFolder() {
        return experimentsAttachmentsTargetFolder;
    }

    public boolean isForumEnabled() {
        return forumEnabled;
    }

    public String getForumUrl() {
        return forumUrl;
    }

    public String getHdf5FilesFolder() {
        return hdf5FilesFolder;
    }

    public int getAggregateStatisticsNumberOfThreads() {
        return aggregateStatisticsNumberOfThreads;
    }

    public int getFirstDayOfMonth() {
        return firstDayOfMonth;
    }
}
