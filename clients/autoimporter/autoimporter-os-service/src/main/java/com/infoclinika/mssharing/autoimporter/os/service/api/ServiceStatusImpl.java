package com.infoclinika.mssharing.autoimporter.os.service.api;

import com.google.common.base.MoreObjects;
import com.infoclinika.mssharing.autoimporter.service.api.internal.ServiceStatus;
import org.springframework.stereotype.Service;

/**
 * @author Ruslan Duboveckij
 */
@Service
public class ServiceStatusImpl implements ServiceStatus {
    private boolean started;

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("started", started)
            .toString();
    }
}
