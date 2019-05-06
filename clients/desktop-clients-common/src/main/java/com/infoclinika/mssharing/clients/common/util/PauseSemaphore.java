package com.infoclinika.mssharing.clients.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;

/**
 * @author Vladislav Kovchug
 */
public class PauseSemaphore {

    private static final Logger LOGGER = LoggerFactory.getLogger(PauseSemaphore.class);

    private final Semaphore pauseSemaphore = new Semaphore(1);
    private boolean paused = false;

    public void waitIfPaused() {
        if (paused) {
            try {
                LOGGER.info("Wait before for pause release.");
                pauseSemaphore.acquire();
                pauseSemaphore.release();
            } catch (InterruptedException e) {
                throw new RuntimeException("Interruped exeption while waiting for pause.");
            }
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPause(boolean paused) {
        if (this.paused == paused) {
            return;
        }
        this.paused = paused;
        if (paused) {
            try {
                LOGGER.info("Pausing.");
                pauseSemaphore.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException("Interruped exeption while is paused.");
            }
        } else {
            LOGGER.info("Releasing pause.");
            pauseSemaphore.release();
        }
    }
}
