package com.infoclinika.mssharing.platform.model.helper.read;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;

/**
 * @author Herman Zamula
 */
public class SingleResultBuilder<ENTITY, ENTITY_LINE> extends AbstractResultBuilder<ENTITY, ENTITY_LINE> {

    private SingleResultBuilder(Iterable<ENTITY> entities, Function<ENTITY, ENTITY_LINE> defaultTransformer) {
        super(entities, defaultTransformer);
    }

    public static <ENTITY, ENTITY_LINE> SingleResultBuilder<ENTITY, ENTITY_LINE> builder(ENTITY entity,
                                                                                         Function<ENTITY,
                                                                                             ENTITY_LINE> transformer) {
        return new SingleResultBuilder<>(ImmutableSet.of(entity), transformer);
    }

    public ENTITY getEntity() {
        return entities.iterator().next();
    }

    public ENTITY_LINE transform() {
        return defaultTransformer.apply(getEntity());
    }

    public ENTITY_LINE transform(Function<ENTITY, ENTITY_LINE> customTransformer) {
        return customTransformer.apply(getEntity());
    }


}
