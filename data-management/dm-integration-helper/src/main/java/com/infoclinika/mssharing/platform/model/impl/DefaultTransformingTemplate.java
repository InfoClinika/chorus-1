package com.infoclinika.mssharing.platform.model.impl;

import org.springframework.transaction.annotation.Transactional;

/**
 * @author : Alexander Serebriyan
 */
@Transactional(readOnly = true)
public interface DefaultTransformingTemplate<ENTITY, ENTITY_LINE> {
    ENTITY_LINE transform(ENTITY entity);
}
