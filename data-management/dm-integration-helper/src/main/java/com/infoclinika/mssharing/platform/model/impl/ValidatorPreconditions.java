package com.infoclinika.mssharing.platform.model.impl;

import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.ObjectNotFoundException;

/**
 * @author Herman Zamula
 */
public final class ValidatorPreconditions {

    public static <T> T checkPresence(T reference) {
        if (reference == null) {
            throw new ObjectNotFoundException();
        }
        return reference;
    }

    public static <T> T checkPresence(T reference, String comment) {
        if (reference == null) {
            throw new ObjectNotFoundException(comment);
        }
        return reference;
    }

    public static void checkAccess(boolean condition, String comment) {
        if (!condition) {
            throw new AccessDenied(comment);
        }
    }
}
