package com.infoclinika.mssharing.desktop.upload.service.api.list;

import java.util.List;

/**
 * @author timofey.kasyanov
 *     date:   27.01.14
 */
public interface ObservableList<T> extends List<T> {

    ListObserver<T> getObserver();

    void change(T item, Object params);

}
