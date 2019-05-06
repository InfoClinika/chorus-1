package com.infoclinika.mssharing.platform.model.helper.read;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.model.PagedItem;
import org.springframework.data.domain.Page;

import static com.infoclinika.mssharing.platform.model.helper.read.ResultBuilderTransformers.toPagedItem;

/**
 * @author Herman Zamula
 */
public class PagedResultBuilder<F, T> extends AbstractResultBuilder<F, T> {

    protected PagedResultBuilder(Page<F> entities, Function<F, T> defaultTransformer) {
        super(entities, defaultTransformer);
    }

    public static <F, T> PagedResultBuilder<F, T> builder(Page<F> entities, Function<F, T> defaultTransformer) {
        return new PagedResultBuilder<>(entities, defaultTransformer);
    }

    public static <F, T> PagedResultBuilder<F, T> builder(PagedItem<F> entities, Function<F, T> defaultTransformer) {
        return new PagedResultBuilder<>(ResultBuilderTransformers.transformToPage(entities), defaultTransformer);
    }

    public PagedItem<T> transform() {
        return ResultBuilderTransformers.transformToPagedItem((Page<F>) entities, defaultTransformer);
    }

    public PagedItem<T> transform(Function<F, T> customTransformer) {
        return ResultBuilderTransformers.transformToPagedItem((Page<F>) entities, customTransformer);
    }


    public PagedItem<F> getPagedItem() {
        return toPagedItem((Page<F>) entities, entities);
    }

    public Page<F> getPage() {
        return (Page<F>) entities;
    }
}
