package com.infoclinika.mssharing.model;

/**
 * @author Elena Kurilina
 */

public abstract class GlacierDownloadListener<T> {

    public abstract void onFileDownloaded(T t);

}
