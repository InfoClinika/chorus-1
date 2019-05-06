package com.infoclinika.mssharing.platform.model.helper.read;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

import static com.google.common.collect.FluentIterable.from;

/**
 * @author Herman Zamula
 */
public class ResultBuilder<ENTITY, CUSTOM> extends AbstractResultBuilder<ENTITY, CUSTOM> {

    private ResultBuilder(Iterable<ENTITY> entities, Function<ENTITY, CUSTOM> defaultTransformer) {
        super(entities, defaultTransformer);
    }

    public static <E, C> ResultBuilder<E, C> builder(Iterable<E> entities, Function<E, C> defaultTransformer) {
        return new ResultBuilder<>(entities, defaultTransformer);
    }

    public FluentIterable<CUSTOM> transform() {
        return from(entities).transform(defaultTransformer);
    }

    public <T> FluentIterable<T> transform(Function<ENTITY, T> custom) {
        return from(entities).transform(custom);
    }

}
