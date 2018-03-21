package com.infoclinika.mssharing.platform.model;

/**
 * @author Herman Zamula
 */
public interface InboxNotifierTemplate {
    void notify(long from, long to, String message);
}
