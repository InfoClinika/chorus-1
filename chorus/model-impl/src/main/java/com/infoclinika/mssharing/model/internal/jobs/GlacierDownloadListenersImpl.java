package com.infoclinika.mssharing.model.internal.jobs;

import com.infoclinika.mssharing.model.GlacierDownloadListener;
import com.infoclinika.mssharing.model.GlacierDownloadListeners;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Elena Kurilina
 */
@Component
public class GlacierDownloadListenersImpl implements GlacierDownloadListeners<ActiveFileMetaData> {
    private static final Map<String, GlacierDownloadListener<ActiveFileMetaData>> listeners =
        new HashMap<String, GlacierDownloadListener<ActiveFileMetaData>>();

    public String addListener(GlacierDownloadListener<ActiveFileMetaData> listener) {
        final String id = UUID.randomUUID().toString();
        listeners.put(id, listener);
        return id;
    }

    public GlacierDownloadListener<ActiveFileMetaData> getListener(String id) {
        return listeners.get(id);
    }
}
