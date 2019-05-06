package com.infoclinika.mssharing.autoimporter.service.util;

import com.infoclinika.mssharing.autoimporter.service.exception.MonitorException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;

/**
 * author Ruslan Duboveckij
 */
public class MonitorHelper {
    public final String folder;
    public final FileAlterationMonitor monitor;
    public final FileFilter fileFilter;
    public final FileAlterationListener listener;

    public MonitorHelper(String folder, FileFilter fileFilter,
                         FileAlterationMonitor monitor, FileAlterationListener listener) {
        this.folder = folder;
        this.fileFilter = fileFilter;
        this.monitor = monitor;
        this.listener = listener;
    }

    private static void startCreateFoundFile(FileAlterationObserver observer) {
        for (FileAlterationListener listener : observer.getListeners()) {
            listener.onStart(observer);

            final Collection<File> files = FileUtils.listFiles(observer.getDirectory(), null, true);

            for (File file : files) {
                if (observer.getFileFilter().accept(file)) {
                    listener.onFileCreate(file);
                }
            }
        }
    }

    public void start() {
        FileAlterationObserver observer = newObserver(fileFilter);
        monitor.addObserver(observer);
        try {
            monitor.start();
        } catch (Exception e) {
            throw new MonitorException("Monitor is already running", e);
        }
        startCreateFoundFile(observer);
    }

    private FileAlterationObserver newObserver(FileFilter fileFilter) {
        File folder = new File(this.folder);
        if (!folder.exists()) {
            throw new MonitorException("Directory isn't found - " + this.folder, new IOException());
        }
        FileAlterationObserver observer = new FileAlterationObserver(folder, fileFilter, IOCase.INSENSITIVE);
        observer.addListener(listener);
        return observer;
    }

    public void stop() {
        try {
            monitor.stop();
        } catch (Exception e) {
            throw new MonitorException("Monitor is already stopping", e);
        }
    }
}
