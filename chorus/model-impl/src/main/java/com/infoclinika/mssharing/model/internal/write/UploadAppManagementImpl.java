package com.infoclinika.mssharing.model.internal.write;

import com.google.common.collect.FluentIterable;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.entity.Instrument;
import com.infoclinika.mssharing.platform.entity.Species;
import com.infoclinika.mssharing.model.internal.entity.UploadAppConfiguration;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.read.Transformers;
import com.infoclinika.mssharing.model.internal.repository.InstrumentRepository;
import com.infoclinika.mssharing.platform.repository.SpeciesRepositoryTemplate;
import com.infoclinika.mssharing.model.internal.repository.UploadAppConfigurationRepository;
import com.infoclinika.mssharing.model.internal.repository.UserRepository;
import com.infoclinika.mssharing.model.write.UploadAppManagement;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Ruslan Duboveckij
 */
@Service
public class UploadAppManagementImpl implements UploadAppManagement {
    @Inject
    private UploadAppConfigurationRepository configurationRepository;
    @Inject
    private RuleValidator ruleValidator;
    @Inject
    private InstrumentRepository instrumentRepository;
    @Inject
    private UserRepository userRepository;
    @Inject
    private SpeciesRepositoryTemplate<Species> speciesRepository;

    @Override
    public void configurationStarted(long actor, long configurationId) {
        changeStartedStatus(actor, configurationId, true);
    }

    private void changeStartedStatus(long actor, long configurationId, boolean status) {
        UploadAppConfiguration uploadAppConfiguration = getUploadAppConfiguration(configurationId);
        if (!ruleValidator.isUploadAppConfigurationOwner(actor, uploadAppConfiguration)) {
            throw new AccessDenied("Couldn't permission to " + (status ? "start" : "stop") + " configuration");
        }
        uploadAppConfiguration.setStarted(status);
        configurationRepository.save(uploadAppConfiguration);
    }

    private UploadAppConfiguration getUploadAppConfiguration(long configurationId) {
        UploadAppConfiguration uploadAppConfiguration = configurationRepository.findOne(configurationId);
        if (uploadAppConfiguration == null) {
            throw new RuntimeException("Couldn't found UploadAppConfiguration with id = " + configurationId);
        }
        return uploadAppConfiguration;
    }

    @Override
    public void configurationStopped(long actor, long configurationId) {
        changeStartedStatus(actor, configurationId, false);
    }

    @Override
    public Configuration createConfiguration(long actor, Configuration configuration) {

        final User user = userRepository.findOne(actor);
        final Instrument instrument = instrumentRepository.findOne(configuration.instrument);
        final Species specie = speciesRepository.findOne(configuration.specie);

        final UploadAppConfiguration uploadAppConfiguration =
                new UploadAppConfiguration(
                        configuration.name,
                        configuration.folder,
                        configuration.started,
                        configuration.labels,
                        instrument,
                        user,
                        specie,
                        UploadAppConfiguration.UploadCompleteAction.valueOf(configuration.completeAction.name()),
                        configuration.folderToMoveFiles
                );

        final UploadAppConfiguration saved = configurationRepository.save(uploadAppConfiguration);

        return Transformers.TO_UPLOAD_APP_CONFIGURATION_DTO.apply(saved);
    }

    @Override
    public void deleteConfiguration(long actor, long configurationId) {
        UploadAppConfiguration uploadAppConfiguration = getUploadAppConfiguration(configurationId);
        if (!ruleValidator.isUploadAppConfigurationOwner(actor, uploadAppConfiguration)) {
            throw new AccessDenied("Couldn't permission to delete configuration");
        }
        uploadAppConfiguration.getUser().removeUploadAppConfiguration(uploadAppConfiguration);
        configurationRepository.delete(uploadAppConfiguration);
    }

    @Override
    public List<Configuration> readConfiguration(long actor) {

        final List<UploadAppConfiguration> configurations = configurationRepository.findByUser(actor);

        return FluentIterable
                .from(configurations)
                .transform(Transformers.TO_UPLOAD_APP_CONFIGURATION_DTO)
                .toList();

    }

}
