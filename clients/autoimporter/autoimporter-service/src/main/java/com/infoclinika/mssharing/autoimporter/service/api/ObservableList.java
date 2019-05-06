package com.infoclinika.mssharing.autoimporter.service.api;

import com.google.common.collect.FluentIterable;
import com.infoclinika.mssharing.autoimporter.service.api.internal.ObserverList;

import java.util.List;

/**
 * @author Ruslan Duboveckij
 */
public interface ObservableList<T> extends DefaultInitUtil<String>, Iterable<T> {
    void add(T o);

    void remove(T o);

    List<T> getList();

    FluentIterable<T> getFluent();

    void setObserver(ObserverList<T> observer);

    ObserverList<T> getObserver();

    String getWatchFolder();

    void clear();
}
