package com.infoclinika.mssharing.platform.entity;

import com.google.common.base.Function;

/**
 * @author Herman Zamula
 */
public final class EntityUtil {

    public static final Function<AbstractPersistable, Long> ENTITY_TO_ID = new Function<AbstractPersistable, Long>() {
        @Override
        public Long apply(AbstractPersistable input) {
            return input.getId();
        }
    };

    private EntityUtil() {
    }
}
