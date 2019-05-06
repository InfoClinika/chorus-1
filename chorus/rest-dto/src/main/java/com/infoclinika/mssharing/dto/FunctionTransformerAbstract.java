package com.infoclinika.mssharing.dto;

import com.google.common.base.Function;
import com.google.common.collect.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * author Ruslan Duboveckij
 */
public abstract class FunctionTransformerAbstract {
    protected FunctionTransformerAbstract() {
        throw new RuntimeException("This class is util");
    }

    public static <T> FluentIterable<T> from(Iterable<T> iterable) {
        return FluentIterable.from(iterable);
    }

    public static <DTO, TO_DTO> List<DTO> toListDto(Iterable<TO_DTO> collection,
                                                   Function<TO_DTO, DTO> toDto) {
        return Lists.newArrayList(toCollectionDto(collection, toDto));
    }

    private static <DTO, TO_DTO> Collection<DTO> toCollectionDto(Iterable<TO_DTO> collection,
                                                                Function<TO_DTO, DTO> toDto) {
        return transform(collection, toDto);
    }

    public static <DTO, TO_DTO> Set<DTO> toSetDto(Iterable<TO_DTO> collection,
                                                 Function<TO_DTO, DTO> toDto) {
        return Sets.newHashSet(toCollectionDto(collection, toDto));
    }

    public static <DTO, KEY, TO_DTO> Map<KEY, DTO> toMapDto(Map<KEY, TO_DTO> map,
                                                            Maps.EntryTransformer<KEY, TO_DTO, DTO> toDto) {
        return Maps.transformEntries(map, toDto);
    }

    public static <FromDTO, DTO> List<FromDTO> fromListDto(Iterable<DTO> collection,
                                                           Function<DTO, FromDTO> fromDto) {
        return Lists.newArrayList(fromCollectionDto(collection, fromDto));
    }

    private static <FromDTO, DTO> Collection<FromDTO> fromCollectionDto(Iterable<DTO> collection,
                                                                        Function<DTO, FromDTO> fromDto) {
        return transform(collection, fromDto);
    }

    private static <Input, Output> Collection<Output> transform(Iterable<Input> iterable,
                                                                Function<Input, Output> function) {
        return Lists.newArrayList(Iterables.transform(iterable, function));
    }
}
