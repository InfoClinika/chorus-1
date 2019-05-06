package com.infoclinika.mssharing.platform.model.helper.read;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.FluentIterable.from;

/**
 * @author Herman Zamula
 */
public abstract class AbstractResultBuilder<E, T> {

    protected final Iterable<E> entities;
    protected final Function<E, T> defaultTransformer;

    protected AbstractResultBuilder(Iterable<E> entities, Function<E, T> defaultTransformer) {
        this.entities = checkNotNull(entities);
        this.defaultTransformer = checkNotNull(defaultTransformer);
    }

    public FluentIterable<E> getEntities() {
        return from(entities);
    }

    public Function<E, T> getDefaultTransformer() {
        return defaultTransformer;
    }

}
