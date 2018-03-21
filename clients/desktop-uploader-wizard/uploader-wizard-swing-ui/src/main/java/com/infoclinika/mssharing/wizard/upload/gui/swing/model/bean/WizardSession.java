package com.infoclinika.mssharing.wizard.upload.gui.swing.model.bean;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.infoclinika.mssharing.dto.request.UserNamePassDTO;
import com.infoclinika.mssharing.dto.response.AuthenticateDTO;
import com.infoclinika.mssharing.dto.response.DictionaryDTO;
import com.infoclinika.mssharing.dto.response.InstrumentDTO;
import com.infoclinika.mssharing.upload.common.transfer.api.Uploader;
import com.infoclinika.mssharing.upload.common.transfer.impl.UploaderConfiguration;
import com.infoclinika.mssharing.upload.common.transfer.impl.UploaderImpl;
import com.infoclinika.mssharing.wizard.upload.model.ConfigurationInfo;
import com.infoclinika.mssharing.wizard.upload.model.UploadConfig;
import com.infoclinika.mssharing.wizard.upload.model.ZipConfig;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

/**
 * @author timofey.kasyanov
 *         date:   28.01.14
 */
@Component
public class WizardSession {

    @Inject
    private WizardContext wizardContext;

    @Inject
    private ConfigurationInfo configurationInfo;

    private AuthenticateDTO authenticate;
    private AWSCredentials credentials;
    private UserNamePassDTO usernamePassword;
    private Uploader uploader;
    private UploadConfig uploadConfig;

    private List<DictionaryDTO> technologyTypes;
    private List<InstrumentDTO> instruments;
    private List<DictionaryDTO> species;
    private DictionaryDTO defaultSpecie;

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

    public void shutdownUploading(){

        uploader.cancel();
        uploader = createUploader();

        final InstrumentDTO currentInstrument = getCurrentInstrument();

        uploadConfig = createUploadConfig();
        uploadConfig.setUploader(uploader);
        uploadConfig.setInstrument(currentInstrument);

    }

    public UploadConfig getUploadConfig() {

        final InstrumentDTO currentInstrument = getCurrentInstrument();
        uploadConfig.setInstrument(currentInstrument);

        return uploadConfig;
    }

    public String getUsername(){
        return authenticate.getUserEmail();
    }

    public void setUsername(String username){
        usernamePassword = new UserNamePassDTO(username, null);
    }

    public WizardContext getWizardContext() {
        return wizardContext;
    }

    public void setAuthenticate(AuthenticateDTO authenticate) {

        this.authenticate = authenticate;

        credentials = new BasicAWSCredentials(
                authenticate.getUploadConfig().getAmazonKey(),
                authenticate.getUploadConfig().getAmazonSecret()
        );

        if(uploader != null){
            uploader.cancel();
        }

        uploader = createUploader();

        uploadConfig = createUploadConfig();
        uploadConfig.setUploader(uploader);

    }

    private InstrumentDTO getCurrentInstrument(){
        return wizardContext.getInstrument();
    }

    private Uploader createUploader(){

        final Integer uploadMaxRetryCount = configurationInfo.getUploadMaxRetryCount();
        final ClientConfiguration clientConfiguration = new ClientConfiguration()
                .withMaxErrorRetry(uploadMaxRetryCount)
                .withProtocol(Protocol.HTTPS);
        final AmazonS3Client amazonS3Client = new AmazonS3Client(credentials, clientConfiguration);
        final UploaderConfiguration configuration =
                UploaderConfiguration.getDefaultConfiguration(
                        amazonS3Client,
                        authenticate.getUploadConfig().getActiveBucket()
                );

        return new UploaderImpl(configuration);

    }

    private UploadConfig createUploadConfig(){

        return new UploadConfig(
                authenticate.getUploadConfig().getActiveBucket(),
                new ZipConfig(
                        configurationInfo.getZipFolderPath()
                )
        );

    }

}
