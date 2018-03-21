package com.infoclinika.mssharing.wizard.upload.service.impl.list;

import com.infoclinika.mssharing.wizard.upload.service.api.list.ListListener;
import com.infoclinika.mssharing.wizard.upload.service.api.list.ListObserver;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

/**
 * @author timofey.kasyanov
 *         date:   27.01.14
 */
public class DefaultListObserver<T> implements ListObserver<T> {

    private final List<ListListener<T>> listeners = newArrayList();

    @Override
    public void addListener(ListListener<T> listener) {

        checkNotNull(listener);

        listeners.add(listener);
    }

    @Override
    public void removeListener(ListListener<T> listener) {

        checkNotNull(listener);

        listeners.remove(listener);
    }

    @Override
    public void notifyAdd(T item) {

        for(ListListener<T> listener : listeners){
            listener.onAdd(item);
        }

    }

    @Override
    public void notifyRemove(T item) {

        for(ListListener<T> listener : listeners){
            listener.onRemove(item);
        }

    }

    @Override
    public void notifyChange(T item , Object params) {

        for(ListListener<T> listener : listeners){
            listener.onChange(item, params);
        }

    }

    @Override
    public void notifyClear() {

        for(ListListener<T> listener : listeners){
            listener.onClear();
        }

    }
}
