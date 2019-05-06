package com.infoclinika.mssharing.autoimporter.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.infoclinika.mssharing.autoimporter.model.Session;
import com.infoclinika.mssharing.autoimporter.model.bean.ContextInfo;
import com.infoclinika.mssharing.autoimporter.service.api.AppCredentialsService;
import com.infoclinika.mssharing.autoimporter.service.api.UploadConfigurationService;
import com.infoclinika.mssharing.autoimporter.service.exception.ApplicationConfigException;
import com.infoclinika.mssharing.autoimporter.service.util.CipherUtil;
import com.infoclinika.mssharing.autoimporter.service.util.Configuration;
import com.infoclinika.mssharing.clients.common.web.api.exception.JsonConvertException;
import com.infoclinika.mssharing.dto.request.UserNamePassDTO;
import com.infoclinika.mssharing.dto.response.DictionaryDTO;
import com.infoclinika.mssharing.dto.response.InstrumentDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

import static com.infoclinika.mssharing.autoimporter.service.api.UploadConfigurationService.UploadConfiguration;

/**
 * author Ruslan Duboveckij
 */
@Service
public class AppCredentialsServiceImpl implements AppCredentialsService {
    private static final Logger Log = LoggerFactory.getLogger(AppCredentialsServiceImpl.class);
    @Inject
    private Configuration configuration;
    @Inject
    private Session session;
    @Inject
    private UploadConfigurationService uploadConfigurationService;

    @Override
    public AppCredentials read() {
        try {
            final AppCredentials encrypted =
                new ObjectMapper().readValue(configuration.getConfigFile(), AppCredentials.class);
            return decryptConfig(encrypted);
        } catch (Exception e) {
            throw new JsonConvertException(e);
        }
    }

    public void readContext(List<InstrumentDTO> instruments, List<DictionaryDTO> species) {
        for (final UploadConfiguration dto : uploadConfigurationService.list()) {

            final Optional<InstrumentDTO> instrument = Iterables.tryFind(instruments, new Predicate<InstrumentDTO>() {
                @Override
                public boolean apply(InstrumentDTO input) {
                    return input.getId() == dto.getInstrument();
                }
            });

            final Optional<DictionaryDTO> specie = Iterables.tryFind(species, new Predicate<DictionaryDTO>() {
                @Override
                public boolean apply(DictionaryDTO input) {
                    return input.getId() == dto.getSpecie();
                }
            });

            if (instrument.isPresent() && specie.isPresent()) {

                final ContextInfo contextInfo =
                    new ContextInfo(
                        dto.getId(),
                        dto.getName(),
                        dto.getFolder(),
                        dto.isStarted(),
                        dto.getLabels(),
                        instrument.get(),
                        specie.get(),
                        dto.getCreated(),
                        dto.getCompleteAction(),
                        dto.getFolderToMoveFiles()
                    );

                session.addContext(contextInfo);

            } else {
                Log.info("Context is ignore: {}", dto);
            }
        }
    }

    @Async("AppConfigService")
    public void save() {
        if (configuration.isConfigSave()) {
            try {
                new ObjectMapper().writerWithDefaultPrettyPrinter()
                    .writeValue(configuration.getConfigFile(), encryptConfig());
            } catch (IOException e) {
                throw new ApplicationConfigException("The problem of preserving " +
                    "the application configuration to file", e);
            }
        }
    }

    private AppCredentials encryptConfig() {
        final UserNamePassDTO config =
            session.getAppCredentials() != null ? session.getAppCredentials() : new UserNamePassDTO(null, null);
        final String token = session.getClientToken();
        return new AppCredentials(
            new UserNamePassDTO(CipherUtil.encrypt(config.getUsername()), CipherUtil.encrypt(config.getPassword())),
            CipherUtil.encrypt(token)
        );
    }

    private AppCredentials decryptConfig(AppCredentials config) {
        final UserNamePassDTO namePass =
            config.getUserNamePass() != null ? config.getUserNamePass() : new UserNamePassDTO(null, null);
        return new AppCredentials(
            new UserNamePassDTO(CipherUtil.decrypt(namePass.getUsername()), CipherUtil.decrypt(namePass.getPassword())),
            CipherUtil.decrypt(config.getToken())
        );
    }
}
