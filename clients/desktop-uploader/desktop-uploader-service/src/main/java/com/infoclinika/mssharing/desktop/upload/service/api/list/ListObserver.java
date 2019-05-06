package com.infoclinika.mssharing.desktop.upload.service.api.list;

/**
 * @author timofey.kasyanov
 *     date:   27.01.14
 */
public interface ListObserver<T> {

    void addListener(ListListener<T> listener);

    void removeListener(ListListener<T> listener);

    void notifyAdd(T item);

    void notifyRemove(T item);

    void notifyChange(T item, Object params);

    void notifyClear();

}
