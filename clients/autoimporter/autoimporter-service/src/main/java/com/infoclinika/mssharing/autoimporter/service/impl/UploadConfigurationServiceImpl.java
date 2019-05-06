package com.infoclinika.mssharing.autoimporter.service.impl;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.infoclinika.mssharing.autoimporter.model.Session;
import com.infoclinika.mssharing.autoimporter.service.api.UploadConfigurationService;
import com.infoclinika.mssharing.autoimporter.service.util.Configuration;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

/**
 * @author timofey.kasyanov
 *     12.03.14
 */
@Component
public class UploadConfigurationServiceImpl implements UploadConfigurationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadConfigurationServiceImpl.class);
    private static final String CONFIG_FILE_EXTENSION = ".json";

    @Inject
    private Configuration configuration;
    @Inject
    private Session session;

    @Override
    public List<UploadConfiguration> list() {

        final List<UploadConfiguration> list = newArrayList();
        final File folder = checkConfigurationFolder();
        final File[] files = folder.listFiles();

        if (files == null || files.length == 0) {
            return list;
        }

        final Gson gson = new Gson();

        for (File file : files) {

            LOGGER.info("Reading configuration from file: {}", file);

            try {

                final FileReader fileReader = new FileReader(file);
                final JsonReader reader = new JsonReader(fileReader);
                final UploadConfiguration configuration = gson.fromJson(reader, UploadConfiguration.class);

                reader.close();

                list.add(configuration);

                LOGGER.info("Configuration is read successfully from file: {}, configuration: {}", file, configuration);

            } catch (IOException e) {
                LOGGER.info("Cannot read configuration from file: {}", file);
                LOGGER.error(String.valueOf(e));
            }

        }

        return list;
    }

    @Override
    public UploadConfiguration create(UploadConfiguration configuration) {

        final UploadConfiguration toCreate = new UploadConfiguration(
            System.currentTimeMillis(),
            configuration.getName(),
            configuration.getFolder(),
            configuration.getLabels(),
            configuration.getInstrument(),
            configuration.getSpecie(),
            new Date(),
            configuration.getCompleteAction(),
            configuration.getFolderToMoveFiles(),
            false
        );

        final File folder = checkConfigurationFolder();
        final String destinationFilePath = folder + File.separator + toCreate.getId() + CONFIG_FILE_EXTENSION;
        final File file = createFile(destinationFilePath);
        final Gson gson = new Gson();

        try {

            final FileWriter fileWriter = new FileWriter(file);
            final JsonWriter jsonWriter = new JsonWriter(fileWriter);

            gson.toJson(toCreate, UploadConfiguration.class, jsonWriter);

            jsonWriter.close();

            return toCreate;

        } catch (IOException e) {
            removeFile(destinationFilePath);
            LOGGER.error("Cannot create configuration", e);
            throw new RuntimeException("Cannot create configuration");
        }
    }

    @Override
    public void remove(long configurationId) {

        final File folder = checkConfigurationFolder();
        final String configFilePath = folder.getAbsolutePath()
            + File.separator + configurationId + CONFIG_FILE_EXTENSION;

        removeFile(configFilePath);

    }

    @Override
    public UploadConfiguration change(UploadConfiguration configuration) {

        final UploadConfiguration toChange = new UploadConfiguration(
            configuration.getId(),
            configuration.getName(),
            configuration.getFolder(),
            configuration.getLabels(),
            configuration.getInstrument(),
            configuration.getSpecie(),
            configuration.getCreated(),
            configuration.getCompleteAction(),
            configuration.getFolderToMoveFiles(),
            configuration.isStarted()
        );
        final File folder = checkConfigurationFolder();
        final String destinationFilePath = folder + File.separator + toChange.getId() + CONFIG_FILE_EXTENSION;
        final File file = new File(destinationFilePath);
        final Gson gson = new Gson();

        try {

            final FileWriter fileWriter = new FileWriter(file, false);
            final JsonWriter jsonWriter = new JsonWriter(fileWriter);

            gson.toJson(toChange, UploadConfiguration.class, jsonWriter);

            jsonWriter.close();

            return toChange;

        } catch (IOException e) {
            LOGGER.error("Cannot change configuration", e);
            throw new RuntimeException("Cannot change configuration");
        }
    }

    private void removeFile(String path) {

        try {
            FileUtils.forceDelete(new File(path));
        } catch (IOException e) {
            LOGGER.error("Cannot delete configuration file: {}", path);
        }

    }

    private File createFile(String path) {

        final File file = new File(path);

        try {
            if (!file.createNewFile()) {
                throw new RuntimeException("Cannot create file for configuration. File: " + path);
            }
        } catch (IOException e) {
            LOGGER.error("Cannot create file for configuration. File: {}", path, e);
            throw new RuntimeException(e);
        }

        if (!file.exists()) {
            LOGGER.error("Cannot create file for configuration. File: {}", path);
            throw new RuntimeException("Cannot create file for configuration. File: " + path);
        }

        return file;

    }

    private File checkConfigurationFolder() {

        checkNotNull(session.getAuthenticate(), "Application is not authenticated.");
        checkNotNull(session.getAuthenticate().getUserEmail(), "User email is not set in application session");

        final String configFolderPath = configuration.getConfigFolderPath();
        final String destinationFolder = configFolderPath + File.separator + session.getAuthenticate().getUserEmail();
        final File folder = new File(destinationFolder);

        if (!folder.exists()) {

            if (!folder.mkdirs()) {
                throw new RuntimeException("Cannot create configuration folder: " + folder);
            }

        }

        return folder;

    }
}
