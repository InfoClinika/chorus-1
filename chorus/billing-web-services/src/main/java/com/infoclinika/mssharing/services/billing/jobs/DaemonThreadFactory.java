package com.infoclinika.mssharing.services.billing.jobs;

import java.util.concurrent.ThreadFactory;

/**
 * @author timofey 21.03.16.
 */
public class DaemonThreadFactory implements ThreadFactory {

    private static final String THREAD_NAME_PREFIX = "daemon-thread-factory-";
    private static long count = 0;

    @Override
    public Thread newThread(Runnable runnable) {
        final Thread thread = new Thread(runnable);
        thread.setName(String.format("%s-%s", THREAD_NAME_PREFIX, ++count));
        thread.setDaemon(true);
        return thread;
    }
}
