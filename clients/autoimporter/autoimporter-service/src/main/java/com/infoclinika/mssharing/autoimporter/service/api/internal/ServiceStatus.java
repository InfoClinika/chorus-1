package com.infoclinika.mssharing.autoimporter.service.api.internal;

/**
 * @author Ruslan Duboveckij
 */
public interface ServiceStatus {
    public boolean isStarted();

    public void setStarted(boolean started);
}
