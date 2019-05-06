package com.infoclinika.mssharing.autoimporter.service.impl;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.infoclinika.mssharing.autoimporter.service.api.ObservableList;
import com.infoclinika.mssharing.autoimporter.service.api.internal.ObserverList;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;

/**
 * author Ruslan Duboveckij
 */
@Service
@Scope("prototype")
public class ObservableListImpl<T> implements ObservableList<T> {
    private final List<T> list = Lists.newLinkedList();
    private String folder;
    private Optional<ObserverList<T>> observer = Optional.absent();

    public void add(T t) {
        list.add(0, t);
        doNotify(NotificationType.ADD_ITEM, t);
    }

    public void remove(T t) {
        list.remove(t);
        doNotify(NotificationType.REMOVE_ITEM, t);
    }

    public List<T> getList() {
        return list;
    }

    public FluentIterable<T> getFluent() {
        return FluentIterable.from(ImmutableList.copyOf(list));
    }

    @Override
    public ObserverList<T> getObserver() {
        return observer.get();
    }

    @Override
    public String getWatchFolder() {
        return folder;
    }

    @Override
    public void setObserver(ObserverList<T> observer) {
        this.observer = Optional.of(observer);
    }

    public Iterator<T> iterator() {
        return new Iterator<T>() {
            Iterator<T> iterator = list.iterator();
            T current;

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                current = iterator.next();
                return current;
            }

            @Override
            public void remove() {
                iterator.remove();
                doNotify(NotificationType.REMOVE_ITEM, current);
            }
        };
    }

    public void clear() {
        list.clear();
        doNotify(NotificationType.CLEAR_ITEMS, null);
    }

    @Override
    public void init(String folder) {
        this.folder = folder;
    }

    private void doNotify(NotificationType type, T item) {
        if (observer.isPresent()) {

            observer.get().notify(type, folder, item);

        }
    }

}
