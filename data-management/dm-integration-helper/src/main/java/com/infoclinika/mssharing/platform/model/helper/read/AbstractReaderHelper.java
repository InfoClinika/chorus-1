package com.infoclinika.mssharing.platform.model.helper.read;

import com.google.common.base.Function;
import org.springframework.context.annotation.Scope;

/**
 * @author : Alexander Serebriyan, Herman Zamula
 */
@Scope(value = "prototype")
public abstract class AbstractReaderHelper<ENTITY, ENTITY_LINE extends ENTITY_LINE_TEMPLATE, ENTITY_LINE_TEMPLATE> {

    protected Function<ENTITY, ENTITY_LINE> activeTransformer;

    public AbstractReaderHelper() {
        //noinspection unchecked
        this.activeTransformer = (Function<ENTITY, ENTITY_LINE>) getDefaultTransformer();
    }

    /**
     * Returns DTO template implementation transformer function or {@code defaultTransformer}
     * ({@link #getDefaultTransformer()} method)
     * if new transformer function wasn't set (via {@link #setTransformer(com.google.common.base.Function)} method)
     *
     * @return current active DTO transformer function
     */
    public Function<ENTITY, ENTITY_LINE> getTransformer() {
        return this.activeTransformer;
    }

    /**
     * Use this method to override default transformer function.
     * It will be applied by all read methods by all {@code AbstractReaderHelper} implementations for DTO transforming
     *
     * @param transformer new {@link com.google.common.base.Function} to override default one
     */
    public void setTransformer(Function<ENTITY, ENTITY_LINE> transformer) {
        this.activeTransformer = transformer;
    }

    /**
     * Default implementation of DTO transformer function. Doesn't support any additional parameters except
     * {@link ENTITY} object.
     * Fields that required some additional parameters will be set to {@code null} or empty (for collections)
     *
     * @return default transformer function of template DTO
     */
    public abstract Function<ENTITY, ENTITY_LINE_TEMPLATE> getDefaultTransformer();


}
