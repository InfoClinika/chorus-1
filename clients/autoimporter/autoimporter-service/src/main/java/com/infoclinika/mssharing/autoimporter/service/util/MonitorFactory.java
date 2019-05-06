package com.infoclinika.mssharing.autoimporter.service.util;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.infoclinika.mssharing.autoimporter.model.Context;
import com.infoclinika.mssharing.autoimporter.service.api.FolderListener;
import com.infoclinika.mssharing.autoimporter.service.exception.MonitorException;
import com.infoclinika.mssharing.autoimporter.service.impl.FileFilterImpl;
import org.apache.commons.io.monitor.FileAlterationMonitor;

import java.util.List;

/**
 * author Ruslan Duboveckij
 */
public abstract class MonitorFactory {
    private List<MonitorHelper> monitors = Lists.newArrayList();

    protected abstract FileAlterationMonitor createMonitor();

    public void createMonitor(Context context) {
        FolderListener listener = createListener();
        listener.init(context);
        MonitorHelper monitorHelper = new MonitorHelper(context.getInfo().getFolder(),
            context.getFileFilter(), createMonitor(), listener
        );
        monitors.add(monitorHelper);
        monitorHelper.start();
    }

    protected abstract FileFilterImpl createFileFilter();

    public FileFilterImpl createFileFilter(Context context) {
        FileFilterImpl filter = createFileFilter();
        filter.init(context);
        return filter;
    }

    protected abstract FolderListener createListener();

    public void removeMonitor(String folder) {
        final MonitorHelper monitor = getMonitor(folder);
        monitor.stop();
        monitors.remove(monitor);
    }

    private MonitorHelper getMonitor(final String folder) {
        try {
            return Iterables.find(monitors, monitor -> monitor.folder.equalsIgnoreCase(folder));
        } catch (Exception e) {
            throw new MonitorException("Not such this monitor - " + folder, e);
        }
    }

    public List<MonitorHelper> getMonitors() {
        return monitors;
    }
}
