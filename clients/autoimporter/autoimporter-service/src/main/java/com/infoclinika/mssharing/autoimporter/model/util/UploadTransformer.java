package com.infoclinika.mssharing.autoimporter.model.util;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.infoclinika.mssharing.autoimporter.model.Context;
import com.infoclinika.mssharing.autoimporter.model.bean.ContextInfo;
import com.infoclinika.mssharing.autoimporter.model.bean.UploadItem;
import com.infoclinika.mssharing.dto.FunctionTransformerAbstract;
import com.infoclinika.mssharing.dto.request.UploadFilesDTORequest;
import com.infoclinika.mssharing.dto.response.DictionaryDTO;
import com.infoclinika.mssharing.dto.response.FileDTO;
import com.infoclinika.mssharing.dto.response.InstrumentDTO;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Iterator;
import java.util.List;

/**
 * author Ruslan Duboveckij
 */
public final class UploadTransformer extends FunctionTransformerAbstract {
    public static final Function<File, Long> GET_FILE_SIZE = new Function<File, Long>() {
        @Nullable
        @Override
        public Long apply(@Nullable File input) {
            return FileUtils.sizeOf(input);
        }
    };
    public static final Function<Context, ContextInfo> TO_CONTEXT_INFO = new Function<Context, ContextInfo>() {
        @Nullable
        @Override
        public ContextInfo apply(@Nullable Context input) {
            return input.getInfo();
        }
    };
    public static final Predicate<Context> IS_STARTED = input -> input.getInfo().isStarted();
    public static final Function<ContextInfo, String> CONTEXT_TO_STRING = new Function<ContextInfo, String>() {
        @Nullable
        @Override
        public String apply(@Nullable ContextInfo input) {
            return input.getName();
        }
    };
    public static final Function<ContextInfo, String> CONTEXT_TO_FOLDER = new Function<ContextInfo, String>() {
        @Nullable
        @Override
        public String apply(@Nullable ContextInfo input) {
            return input.getFolder();
        }
    };
    public static final Function<File, String> FILE_TO_STRING = new Function<File, String>() {
        @Nullable
        @Override
        public String apply(@Nullable File file) {
            return file.getName();
        }
    };

    public static final Function<FileDTO, String> FILE_DTO_TO_STRING = new Function<FileDTO, String>() {
        @Nullable
        @Override
        public String apply(@Nullable FileDTO file) {
            return file.getName();
        }
    };
    public static final ReduceFunction<Long, Long, Long> SUM = (a, b) -> a + b;
    public static final Function<File, UploadItem> TO_UPLOAD_ITEM = new Function<File, UploadItem>() {
        @Nullable
        @Override
        public UploadItem apply(@Nullable File input) {
            return new UploadItem(input);
        }
    };

    public static List<UploadFilesDTORequest.UploadFile> toUploadFilesDTORequestFile(
        final DictionaryDTO dictionary, FluentIterable<UploadItem> files, final String labels) {
        return toListDto(files, new Function<UploadItem, UploadFilesDTORequest.UploadFile>() {
            @Nullable
            @Override
            public UploadFilesDTORequest.UploadFile apply(@Nullable UploadItem file) {
                return new UploadFilesDTORequest.UploadFile(
                    file.getName(), labels, FileUtils.sizeOf(file.getFile()), dictionary.getId(), false);
            }
        });
    }

    public static <F, S> S reduce(Iterable<F> iterable, ReduceFunction<S, F, S> reduce, S start) {
        return reduce(iterable.iterator(), reduce, start);
    }

    public static <F, S> S reduce(Iterator<F> iterator, ReduceFunction<S, F, S> reduce, S start) {
        return iterator.hasNext() ? reduce(iterator, reduce, reduce.apply(start, iterator.next())) : start;
    }

    public static Optional<InstrumentDTO> tryFindInstrument(final long id, List<InstrumentDTO> instruments) {
        return Iterables.tryFind(instruments, input -> input.getId() == id);
    }

    public static Optional<DictionaryDTO> tryFindSpecie(final long id, List<DictionaryDTO> species) {
        return Iterables.tryFind(species, input -> input.getId() == id);
    }

    public static interface ReduceFunction<A, B, R> {
        R apply(A a, B b);
    }

    public static class CombineIterators<A, B, R> implements Iterator<R> {
        private final Iterator<A> iteratorA;
        private final Iterator<B> iteratorB;
        private final ReduceFunction<A, B, R> reduceFunction;

        private CombineIterators(Iterable<A> iterableA, Iterable<B> iterableB,
                                 ReduceFunction<A, B, R> reduceFunction) {
            this.reduceFunction = reduceFunction;
            this.iteratorA = iterableA.iterator();
            this.iteratorB = iterableB.iterator();
        }

        public static <T, F, V> Iterable<V> combine(final Iterable<T> iterableA, final Iterable<F> iterableB,
                                                    final ReduceFunction<T, F, V> reduceFunction) {
            return () -> new CombineIterators<>(iterableA, iterableB, reduceFunction);
        }

        @Override
        public boolean hasNext() {
            return iteratorA.hasNext() && iteratorB.hasNext();
        }

        @Override
        public R next() {
            return reduceFunction.apply(iteratorA.next(), iteratorB.next());
        }

        @Override
        public void remove() {
            iteratorA.remove();
            iteratorB.remove();
        }
    }
}
