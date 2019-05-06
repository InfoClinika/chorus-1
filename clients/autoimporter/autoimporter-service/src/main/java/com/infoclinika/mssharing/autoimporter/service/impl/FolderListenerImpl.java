package com.infoclinika.mssharing.autoimporter.service.impl;

import com.infoclinika.mssharing.autoimporter.model.Context;
import com.infoclinika.mssharing.autoimporter.service.api.FolderListener;
import com.infoclinika.mssharing.propertiesprovider.DesktopClientsPropertiesProvider;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.File;

/**
 * author Ruslan Duboveckij
 */
@Service
@Scope("prototype")
public class FolderListenerImpl extends FileAlterationListenerAdaptor implements FolderListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(FolderListenerImpl.class);
    private Context context;

    @Inject
    private DesktopClientsPropertiesProvider desktopClientsPropertiesProvider;

    @Override
    public void init(Context context) {
        this.context = context;
    }

    @Override
    public void onStart(FileAlterationObserver observer) {

        LOGGER.info("Folder listener on start. Folder: {}", context.getInfo().getFolder());

        context.incCurrentCountAndUpload();
    }

    @Override
    public void onDirectoryCreate(File directory) {
        LOGGER.info("Folder listener on directory create. Folder: {}", context.getInfo().getFolder());
        LOGGER.info("Created directory: {}", directory.getAbsolutePath());
        if (!desktopClientsPropertiesProvider.isThermoRecursiveScan()) {
            onFileCreate(directory);
        }
    }

    // Is triggered when a file is created in the monitored folder
    @Override
    public void onFileCreate(File file) {
        LOGGER.info("Folder listener on file create. Folder: {}", context.getInfo().getFolder());
        LOGGER.info("Created file: {}", file.getAbsolutePath());

        if (checkFile(file)) {
            context.addWaitFile(file);
        }
    }


    @Override
    public void onDirectoryChange(File directory) {
        /*LOGGER.info("Folder listener on directory change. Folder: " + context.getInfo().getFolder());
        LOGGER.info("Changed directory: " + directory.getAbsolutePath());
        onFileChange(directory);*/
    }

    @Override
    public void onFileChange(File file) {
        /*LOGGER.info("Folder listener on file change. Folder: " + context.getInfo().getFolder());
        LOGGER.info("Changed file: " + file.getAbsolutePath());

        if(checkFile(file)){
            context.addWaitFile(file);
        }*/
    }

    @Override
    public void onDirectoryDelete(File directory) {
        LOGGER.info("Folder listener on directory delete. Folder: {}", context.getInfo().getFolder());
        LOGGER.info("Deleted directory: {}", directory.getAbsolutePath());
        onFileDelete(directory);
    }

    @Override
    public void onFileDelete(File file) {
        LOGGER.info("Folder listener on file delete. Folder: {}", context.getInfo().getFolder());
        LOGGER.info("Deleted file: {}", file.getAbsolutePath());

        if (checkFile(file)) {
            context.removeWaitFile(file);
        }
    }

    private boolean checkFile(File file) {

        if (desktopClientsPropertiesProvider.isThermoRecursiveScan()) {
            return true;
        }

        final String folder = context.getInfo().getFolder();

        final String parent = file.getParent();

        LOGGER.info("Check file/directory. Watching folder: {} Parent of file/directory: {}", folder, parent);

        return folder.equals(parent);

    }

    @Override
    public String toString() {
        return "FileAlterationListenerImpl{" +
            "context=" + context +
            '}';
    }
}
