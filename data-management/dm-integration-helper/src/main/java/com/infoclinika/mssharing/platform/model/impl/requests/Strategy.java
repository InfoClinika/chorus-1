package com.infoclinika.mssharing.platform.model.impl.requests;

import com.infoclinika.mssharing.platform.model.RequestsTemplate;

import java.util.Collection;

/**
 * @author Pavel Kaplin
 */
public abstract class Strategy {
    abstract public Collection<RequestsTemplate.InboxItem> getInboxItems(long actor);

    abstract public void approve(long actor, String request);

    abstract public void refuse(long actor, String request, String comment);

    abstract public void remove(long actor, String request);

    boolean canHandle(String request) {
        return request.startsWith(getIdPrefix());
    }

    private String getIdPrefix() {
        return this.getClass().getSimpleName();
    }

    protected String buildGlobalId(Object id) {
        return getIdPrefix() + id;
    }

    protected String getInternalId(String globalId) {
        return globalId.substring(getIdPrefix().length());
    }
}
