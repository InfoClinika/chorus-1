package com.infoclinika.mssharing.autoimporter.service.impl;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.infoclinika.msdata.dataimport.thermo.ThermoRawFileChecker;
import com.infoclinika.mssharing.autoimporter.model.Context;
import com.infoclinika.mssharing.autoimporter.model.Session;
import com.infoclinika.mssharing.autoimporter.model.bean.ContextInfo;
import com.infoclinika.mssharing.autoimporter.model.bean.DuplicateItem;
import com.infoclinika.mssharing.autoimporter.model.bean.UploadItem;
import com.infoclinika.mssharing.autoimporter.model.bean.WaitItem;
import com.infoclinika.mssharing.autoimporter.model.util.UploadTransformer;
import com.infoclinika.mssharing.autoimporter.service.api.AppCredentialsService;
import com.infoclinika.mssharing.autoimporter.service.api.UploadConfigurationService;
import com.infoclinika.mssharing.autoimporter.service.api.internal.UploadService;
import com.infoclinika.mssharing.autoimporter.service.util.Configuration;
import com.infoclinika.mssharing.autoimporter.service.util.MonitorFactory;
import com.infoclinika.mssharing.autoimporter.service.util.MonitorHelper;
import com.infoclinika.mssharing.clients.common.web.api.WebService;
import com.infoclinika.mssharing.dto.request.UserNamePassDTO;
import com.infoclinika.mssharing.dto.response.AuthenticateDTO;
import com.infoclinika.mssharing.dto.response.DictionaryDTO;
import com.infoclinika.mssharing.dto.response.InstrumentDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.FileFilter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.infoclinika.mssharing.autoimporter.service.api.UploadConfigurationService.UploadConfiguration;

/**
 * author Ruslan Duboveckij
 */
@Component
public class UploadServiceImpl implements UploadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadServiceImpl.class);

    private Session session;
    private WebService webService;
    private MonitorFactory monitorFactory;
    private Configuration configuration;
    private AppCredentialsService appCredentialsService;
    private UploadConfigurationService uploadConfigurationService;

    private boolean thermoFileCheckerEnabled = true;

    @Inject
    public UploadServiceImpl(Session session, WebService webService,
                             UploadConfigurationService uploadConfigurationService,
                             @Named("monitorFactory") MonitorFactory monitorFactory,
                             Configuration configuration,
                             AppCredentialsService appCredentialsService) {
        this.session = session;
        this.webService = webService;
        this.monitorFactory = monitorFactory;
        this.configuration = configuration;
        this.appCredentialsService = appCredentialsService;
        this.uploadConfigurationService = uploadConfigurationService;
    }

    @Override
    public String getUserName() {
        return session.getAuthenticate().getUserEmail();
    }

    @Override
    public void authorization(String email, String password) {

        final UserNamePassDTO credentials = new UserNamePassDTO(email, password);
        final AuthenticateDTO authenticate =
            webService.authenticate(credentials);

        session.setAuthenticate(authenticate);
        session.setAppCredentials(credentials);

        configuration.initConfigFile();
        appCredentialsService.save();
        readContext();
    }

    @Override
    public void authorization(String token) {

        final AuthenticateDTO authenticate =
            webService.authenticate(token);

        session.setAuthenticate(authenticate);
        session.setClientToken(token);

        configuration.initConfigFile();
        appCredentialsService.save();
        readContext();
    }

    @Override
    public boolean readAuthorization() {
        if (!configuration.getConfigFile().exists()) {
            return false;
        }

        final AppCredentialsService.AppCredentials appCredentials = appCredentialsService.read();

        final AuthenticateDTO authenticate;

        if (configuration.isClientTokenEnabled()) {
            final String token = appCredentials.getToken();

            if (Strings.isNullOrEmpty(token)) {
                return false;
            }
            authenticate = webService.authenticate(token);

            session.setClientToken(token);
        } else {
            final UserNamePassDTO namePass = appCredentials.getUserNamePass();
            if (namePass == null || Strings.isNullOrEmpty(namePass.getUsername()) ||
                Strings.isNullOrEmpty(namePass.getPassword())) {
                return false;
            }
            authenticate = webService.authenticate(namePass);
            session.setAppCredentials(namePass);
        }

        session.setAuthenticate(authenticate);

        readContext();
        return true;
    }

    private void readContext() {

        LOGGER.info("Read context");

        appCredentialsService.readContext(getInstruments(), getSpecies());

        LOGGER.info("Session contexts: {}", session.getContexts().keySet());

        final FluentIterable<Context> contexts = FluentIterable
            .from(session.getContexts().values())
            .filter(UploadTransformer.IS_STARTED);

        final List<MonitorHelper> monitors = monitorFactory.getMonitors();
        final FluentIterable<MonitorHelper> fluentMonitors = FluentIterable.from(monitors);

        for (final Context context : contexts) {

            boolean wasStarted = fluentMonitors.anyMatch(new Predicate<MonitorHelper>() {
                @Override
                public boolean apply(MonitorHelper input) {
                    return input.folder.equals(context.getInfo().getFolder());
                }
            });

            if (!wasStarted) {
                startWatch(context.getInfo().getFolder());
            } else {
                LOGGER.info("Context for folder: {} is already started", context.getInfo().getFolder());
            }

        }
    }

    @Override
    public void clearConfig() {
        session.clear();
        appCredentialsService.save();
        LOGGER.info("Clear session");
    }

    @Override
    public final List<InstrumentDTO> getInstruments() {
        return webService.getInstruments();
    }

    @Override
    public List<InstrumentDTO> getInstruments(long instrumentModel) {
        return webService.getInstruments(instrumentModel);
    }

    @Override
    public List<DictionaryDTO> getTechnologyTypes() {
        return webService.getTechnologyTypes();
    }

    @Override
    public List<DictionaryDTO> getVendors() {
        return webService.getVendors();
    }

    @Override
    public List<DictionaryDTO> getLabs() {
        return webService.getLabs();
    }

    @Override
    public List<DictionaryDTO> getInstrumentModels(long technologyType, long vendor) {
        return webService.getInstrumentModels(technologyType, vendor);
    }

    @Override
    public InstrumentDTO getInstrument(long instrument) {
        return webService.getInstrument(instrument);
    }

    @Override
    public InstrumentDTO createDefaultInstrument(long lab, long instrumentModel) {
        return webService.createDefaultInstrument(lab, instrumentModel);
    }

    @Override
    public final List<DictionaryDTO> getSpecies() {
        return webService.getSpecies();
    }

    @Override
    public DictionaryDTO getDefaultSpecie() {
        return webService.getDefaultSpecie();
    }

    @Override
    public void addContext(String name,
                           String folder,
                           String labels,
                           long instrumentId,
                           long specieId,
                           UploadConfigurationService.CompleteAction completeAction,
                           String folderToMoveFiles) {

        final ContextInfo info = new ContextInfo(
            0,
            name,
            folder,
            false,
            labels,
            UploadTransformer.tryFindInstrument(instrumentId, getInstruments()).get(),
            UploadTransformer.tryFindSpecie(specieId, getSpecies()).get(),
            new Date(),
            completeAction,
            folderToMoveFiles
        );

        final UploadConfiguration created = uploadConfigurationService.create(info.toDto());

        info.setId(created.getId());

        session.addContext(info);

        LOGGER.info("Add context folder: {}", info.getName());
    }

    @Override
    public void startWatch(String folder) {
        final Context context = session.getContext(folder);
        monitorFactory.createMonitor(context);
        context.start();
        uploadConfigurationService.change(context.getInfo().toDto());
        LOGGER.info("Monitor started watching folder: {}", folder);
    }

    @Override
    public List<ContextInfo> getContexts() {
        return session.getContexts().values().stream()
            .map(UploadTransformer.TO_CONTEXT_INFO::apply)
            .collect(Collectors.toList());
    }

    @Override
    public void removeContext(String folder) {

        final ContextInfo info = session.getContext(folder).getInfo();
        if (info.isStarted()) {
            stopWatch(folder);
        }

        session.removeContext(folder);

        uploadConfigurationService.remove(info.getId());

        LOGGER.info("Removing context: {}", folder);

    }

    @Override
    public void stopWatch(String folder) {
        monitorFactory.removeMonitor(folder);
        final Context context = session.getContext(folder);
        context.stop();
        uploadConfigurationService.change(context.getInfo().toDto());
        LOGGER.info("Monitor stopped watching folder: {}", folder);
    }

    @Override
    public int getFolderListLength(String folder) {
        final FileFilter fileFilter = session.getContext(folder).newFileFilter();
        final File[] files = new File(folder).listFiles(fileFilter);
        return files != null ? files.length : 0;
    }

    @Override
    public List<WaitItem> getWaitItem(String folder) {
        return session.getContext(folder).getWaitList().getList();
    }

    @Override
    public List<UploadItem> getUploadItem(String folder) {
        return session.getContext(folder).getUploadList().getList();
    }

    @Override
    public List<DuplicateItem> getDuplicateItems(String folder) {
        return session.getContext(folder).getDuplicateList().getList();
    }

    @Override
    public boolean isThermoFileCheckingAvailable() {
        return thermoFileCheckerEnabled && ThermoRawFileChecker.isCheckingAvailable();
    }

    @Override
    public boolean isThermoFileCheckerEnabled() {
        return thermoFileCheckerEnabled;
    }

    @Override
    public void disableFileChecking() {
        this.thermoFileCheckerEnabled = false;
    }
}
