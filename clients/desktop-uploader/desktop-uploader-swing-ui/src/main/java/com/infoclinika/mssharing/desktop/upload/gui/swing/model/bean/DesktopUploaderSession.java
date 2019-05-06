package com.infoclinika.mssharing.desktop.upload.gui.swing.model.bean;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.infoclinika.mssharing.clients.common.transfer.api.Uploader;
import com.infoclinika.mssharing.clients.common.transfer.impl.S3ClientProvider;
import com.infoclinika.mssharing.clients.common.transfer.impl.UploaderConfiguration;
import com.infoclinika.mssharing.clients.common.transfer.impl.UploaderImpl;
import com.infoclinika.mssharing.clients.common.util.PauseSemaphore;
import com.infoclinika.mssharing.desktop.upload.model.ConfigurationInfo;
import com.infoclinika.mssharing.desktop.upload.model.UploadConfig;
import com.infoclinika.mssharing.desktop.upload.model.ZipConfig;
import com.infoclinika.mssharing.dto.request.UserNamePassDTO;
import com.infoclinika.mssharing.dto.response.AuthenticateDTO;
import com.infoclinika.mssharing.dto.response.DictionaryDTO;
import com.infoclinika.mssharing.dto.response.InstrumentDTO;
import com.infoclinika.mssharing.dto.response.UploadConfigDTO;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

/**
 * @author timofey.kasyanov
 *     date:   28.01.14
 */
@Component
public abstract class DesktopUploaderSession {

    @Inject
    private DesktopUploaderContext desktopUploaderContext;

    @Inject
    private ConfigurationInfo configurationInfo;

    private AuthenticateDTO authenticate;
    private AWSCredentials credentials;
    private UserNamePassDTO usernamePassword;
    private UploadConfig uploadConfig;

    private List<DictionaryDTO> technologyTypes;
    private List<InstrumentDTO> instruments;
    private List<DictionaryDTO> species;
    private DictionaryDTO defaultSpecie;
    private boolean hasInternetConnection = true;

    public List<DictionaryDTO> getTechnologyTypes() {
        return technologyTypes;
    }

    public void setTechnologyTypes(List<DictionaryDTO> technologyTypes) {
        this.technologyTypes = technologyTypes;
    }

    public DictionaryDTO getDefaultSpecie() {
        return defaultSpecie;
    }

    public void setDefaultSpecie(DictionaryDTO defaultSpecie) {
        this.defaultSpecie = defaultSpecie;
    }

    public List<InstrumentDTO> getInstruments() {
        return instruments;
    }

    public void setInstruments(List<InstrumentDTO> instruments) {
        this.instruments = instruments;
    }

    public List<DictionaryDTO> getSpecies() {
        return species;
    }

    public void setSpecies(List<DictionaryDTO> species) {
        this.species = species;
    }

    public void shutdownUploading() {
        final UploadConfig uploadConfig = replaceOldUploadConfigWithNew();

        final InstrumentDTO currentInstrument = getCurrentInstrument();
        uploadConfig.setInstrument(currentInstrument);
    }

    public UploadConfig getUploadConfig() {

        final InstrumentDTO currentInstrument = getCurrentInstrument();
        uploadConfig.setInstrument(currentInstrument);

        return uploadConfig;
    }

    public String getUsername() {
        return authenticate.getUserEmail();
    }

    public void setUsername(String username) {
        usernamePassword = new UserNamePassDTO(username, null);
    }

    public DesktopUploaderContext getDesktopUploaderContext() {
        return desktopUploaderContext;
    }

    public void setAuthenticate(AuthenticateDTO authenticate) {

        this.authenticate = authenticate;

        final UploadConfigDTO uploadConfigDto = authenticate.getUploadConfig();
        if (uploadConfigDto.isUseRoles()) {
            credentials = new BasicSessionCredentials(
                uploadConfigDto.getAmazonKey(),
                uploadConfigDto.getAmazonSecret(),
                uploadConfigDto.getSessionToken()
            );
        } else {
            credentials = new BasicAWSCredentials(uploadConfigDto.getAmazonKey(), uploadConfigDto.getAmazonSecret());
        }

        replaceOldUploadConfigWithNew();
    }

    public boolean hasInternetConnection() {
        return hasInternetConnection;
    }

    public void setHasInternetConnection(boolean hasInternetConnection) {
        this.hasInternetConnection = hasInternetConnection;
    }

    @Lookup
    protected abstract S3ClientProvider getS3ClientProvider();

    private InstrumentDTO getCurrentInstrument() {
        return desktopUploaderContext.getInstrument();
    }

    private Uploader createUploader(PauseSemaphore pauseSemaphore) {
        final UploaderConfiguration configuration =
            UploaderConfiguration.getDefaultConfiguration(
                getS3ClientProvider(),
                authenticate.getUploadConfig()
                    .getActiveBucket()
            );

        return new UploaderImpl(configuration, pauseSemaphore);

    }

    private UploadConfig createUploadConfig(Uploader uploader, PauseSemaphore pauseSemaphore) {
        return new UploadConfig(
            authenticate.getUploadConfig().getActiveBucket(),
            new ZipConfig(
                configurationInfo.getZipFolderPath()
            ),
            uploader,
            pauseSemaphore
        );

    }

    private UploadConfig replaceOldUploadConfigWithNew() {
        if (uploadConfig != null && uploadConfig.getUploader() != null) {
            uploadConfig.getUploader().cancel();
        }

        final PauseSemaphore pauseSemaphore = new PauseSemaphore();
        final Uploader uploader = createUploader(pauseSemaphore);

        uploadConfig = createUploadConfig(uploader, pauseSemaphore);
        return uploadConfig;
    }

}
