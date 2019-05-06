package com.infoclinika.mssharing.desktop.upload.service.impl.list;

import com.infoclinika.mssharing.desktop.upload.service.api.list.ListObserver;
import com.infoclinika.mssharing.desktop.upload.service.api.list.ObservableList;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author timofey.kasyanov
 *     date:   27.01.14
 */
public class DefaultObservableList<T> extends ArrayList<T> implements ObservableList<T> {

    private ListObserver<T> observer = new DefaultListObserver<T>();

    @Override
    public ListObserver<T> getObserver() {
        return observer;
    }

    @Override
    public void change(T item, Object params) {
        observeOnChange(item, params);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {

        if (c.size() == 0) {
            return super.addAll(c);
        }

        boolean result = super.addAll(c);

        observeOnAdd(c.iterator().next());

        return result;

    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {

        if (c.size() == 0) {
            return super.addAll(index, c);
        }

        boolean result = super.addAll(index, c);

        observeOnAdd(c.iterator().next());

        return result;

    }

    @Override
    public boolean add(T t) {

        boolean result = super.add(t);

        if (result) {

            observeOnAdd(t);

        }

        return result;
    }

    @Override
    public void add(int index, T element) {

        super.add(index, element);

        observeOnAdd(element);

    }

    @Override
    public T remove(int index) {

        final T item = get(index);

        observeOnRemove(item);

        return super.remove(index);
    }

    @Override
    public boolean remove(Object o) {

        boolean result = super.remove(o);

        if (result) {

            observeOnRemove((T) o);

        }

        return result;
    }

    @Override
    public void clear() {

        super.clear();

        observeOnClear();
    }

    private void observeOnAdd(T item) {

        if (observer != null) {
            observer.notifyAdd(item);
        }

    }

    private void observeOnRemove(T item) {

        if (observer != null) {
            observer.notifyRemove(item);
        }

    }

    private void observeOnChange(T item, Object params) {

        if (observer != null) {
            observer.notifyChange(item, params);
        }

    }

    private void observeOnClear() {

        if (observer != null) {
            observer.notifyClear();
        }

    }

}
