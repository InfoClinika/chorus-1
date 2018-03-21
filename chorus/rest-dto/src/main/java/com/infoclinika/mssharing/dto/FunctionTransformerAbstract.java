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

    public static <DTO, ToDTO> List<DTO> toListDto(Iterable<ToDTO> collection,
                                                   Function<ToDTO, DTO> toDto) {
        return Lists.newArrayList(toCollectionDto(collection, toDto));
    }

    private static <DTO, ToDTO> Collection<DTO> toCollectionDto(Iterable<ToDTO> collection,
                                                                Function<ToDTO, DTO> toDto) {
        return transform(collection, toDto);
    }

    public static <DTO, ToDTO> Set<DTO> toSetDto(Iterable<ToDTO> collection,
                                                 Function<ToDTO, DTO> toDto) {
        return Sets.newHashSet(toCollectionDto(collection, toDto));
    }

    public static <DTO, Key, ToDTO> Map<Key, DTO> toMapDto(Map<Key, ToDTO> map,
                                                           Maps.EntryTransformer<Key, ToDTO, DTO> toDto) {
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
