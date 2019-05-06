package com.infoclinika.mssharing.platform.model.helper.read;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.infoclinika.mssharing.platform.model.PagedItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static com.google.common.collect.FluentIterable.from;

/**
 * @author Herman Zamula
 */
/*package*/ class ResultBuilderTransformers {

    public static <F, T> PagedItem<T> transformToPagedItem(Page<F> paged, Function<F, T> transformerFn) {
        return toPagedItem(paged, from(paged).transform(transformerFn).toList());
    }

    public static <F> PagedItem<F> transformToPagedItem(Page<F> paged) {
        return toPagedItem(paged, from(paged).toList());
    }

    public static <F, T> Page<T> transformToPage(PagedItem<F> entityPage, Function<F, T> transformerFn) {
        return new PageImpl<T>(
            from(entityPage.items).transform(transformerFn).toList(),
            new PageRequest(entityPage.pageNumber, entityPage.pageSize), entityPage.itemsCount
        );
    }

    public static <F> Page<F> transformToPage(PagedItem<F> entityPage) {
        return new PageImpl<>(
            from(entityPage.items).toList(), new PageRequest(entityPage.pageNumber, entityPage.pageSize),
            entityPage.itemsCount
        );
    }

    public static <F, T> PagedItem<T> toPagedItem(Page<F> entityPage, Iterable<T> pagedContent) {
        return new PagedItem<>(
            entityPage.getTotalPages(),
            entityPage.getTotalElements(),
            entityPage.getNumber(),
            entityPage.getSize(),
            from(pagedContent).toList()
        );
    }

    public static <F, T> FluentIterable<T> transformToIterable(Iterable<F> items, Function<F, T> transformerFn) {
        return from(items).transform(transformerFn);
    }
}
