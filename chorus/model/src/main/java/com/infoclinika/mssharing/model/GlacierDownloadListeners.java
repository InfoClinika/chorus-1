package com.infoclinika.mssharing.model;

/**
 * @author Elena Kurilina
 */
public interface GlacierDownloadListeners<T> {

    public String addListener(GlacierDownloadListener<T> listener);

    public GlacierDownloadListener<T> getListener(String id);
}
